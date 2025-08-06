package com.example.watchsleepmonitor.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.PassiveMonitoringConfig
import androidx.health.services.client.data.MeasureCapabilities
import kotlinx.coroutines.*

class HealthMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var healthServicesClient: HealthServicesClient
    private lateinit var passiveMonitoringClient: PassiveMonitoringClient
    private lateinit var measureClient: MeasureClient
    private var isMonitoring = false

    companion object {
        private const val TAG = "HealthMonitoringService"
    }

    override fun onCreate() {
        super.onCreate()
        // Forma correcta de inicializar HealthServicesClient
        healthServicesClient = HealthServices.getClient(this)
        passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
        measureClient = healthServicesClient.measureClient
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_MONITORING" -> startHealthMonitoring()
            "STOP_MONITORING" -> stopHealthMonitoring()
        }
        return START_STICKY
    }

    private fun startHealthMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true
            serviceScope.launch {
                try {
                    // Verificar capacidades del dispositivo
                    val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
                    val measureCapabilities = measureClient.getCapabilitiesAsync().await()

                    Log.d(TAG, "Passive monitoring capabilities: ${capabilities.supportedDataTypesPassiveMonitoring}")
                    Log.d(TAG, "Measure capabilities: ${measureCapabilities.supportedDataTypesMeasure}")

                    // Configurar monitoreo pasivo si está disponible
                    if (capabilities.supportedDataTypesPassiveMonitoring.isNotEmpty()) {
                        startPassiveMonitoring(capabilities)
                    }

                    // Iniciar medición activa para ritmo cardíaco si está disponible
                    if (DataType.HEART_RATE_BPM in measureCapabilities.supportedDataTypesMeasure) {
                        startActiveHeartRateMonitoring()
                    }

                    Log.d(TAG, "Health monitoring started")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting health monitoring", e)
                }
            }
        }
    }

    private suspend fun startPassiveMonitoring(capabilities: androidx.health.services.client.data.PassiveMonitoringCapabilities) {
        try {
            val supportedTypes = mutableSetOf<DataType<*, *>>()

            // Agregar tipos de datos soportados
            if (DataType.STEPS_DAILY in capabilities.supportedDataTypesPassiveMonitoring) {
                supportedTypes.add(DataType.STEPS_DAILY)
            }

            if (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesPassiveMonitoring) {
                supportedTypes.add(DataType.HEART_RATE_BPM)
            }

            if (supportedTypes.isNotEmpty()) {
                val config = PassiveMonitoringConfig.builder()
                    .setDataTypes(supportedTypes)
                    .setShouldUserActivityInfoBeRequested(false)
                    .build()

                passiveMonitoringClient.setPassiveListenerServiceAsync(
                    HealthDataListenerService::class.java,
                    config
                ).await()

                Log.d(TAG, "Passive monitoring configured for: $supportedTypes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring passive monitoring", e)
        }
    }

    private fun startActiveHeartRateMonitoring() {
        serviceScope.launch {
            try {
                // Registrar callback para mediciones de ritmo cardíaco
                measureClient.registerMeasureCallback(
                    DataType.HEART_RATE_BPM,
                    object : androidx.health.services.client.MeasureCallback {
                        override fun onAvailabilityChanged(
                            dataType: DataType<*, *>,
                            availability: androidx.health.services.client.data.Availability
                        ) {
                            Log.d(TAG, "Heart rate availability: $availability")
                        }

                        override fun onDataReceived(data: DataPointContainer) {
                            processHeartRateData(data)
                        }
                    }
                )

                Log.d(TAG, "Active heart rate monitoring started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting active heart rate monitoring", e)
            }
        }
    }

    private fun processHeartRateData(dataPoints: DataPointContainer) {
        val heartRatePoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        heartRatePoints.forEach { dataPoint ->
            when (dataPoint) {
                is SampleDataPoint<Double> -> {
                    val heartRate = dataPoint.value.toInt()
                    val timestamp = dataPoint.timeDurationFromBoot.toEpochMilli()

                    Log.d(TAG, "Heart rate: $heartRate BPM")

                    // Enviar datos al ViewModel
                    val intent = Intent("HEALTH_DATA_UPDATE")
                    intent.putExtra("heartRate", heartRate)
                    intent.putExtra("timestamp", timestamp)
                    sendBroadcast(intent)

                    // Enviar vía Bluetooth si el servicio está disponible
                    try {
                        BluetoothService.sendDataToPhone("HR:$heartRate")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not send heart rate data via Bluetooth", e)
                    }
                }
            }
        }
    }

    private fun stopHealthMonitoring() {
        isMonitoring = false
        serviceScope.launch {
            try {
                // Detener monitoreo pasivo
                passiveMonitoringClient.clearPassiveListenerServiceAsync().await()

                // Desregistrar callback de medición
                measureClient.unregisterMeasureCallbackAsync(
                    DataType.HEART_RATE_BPM,
                    object : androidx.health.services.client.MeasureCallback {
                        override fun onAvailabilityChanged(
                            dataType: DataType<*, *>,
                            availability: androidx.health.services.client.data.Availability
                        ) {}

                        override fun onDataReceived(data: DataPointContainer) {}
                    }
                ).await()

                Log.d(TAG, "Health monitoring stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping health monitoring", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHealthMonitoring()
        serviceScope.cancel()
    }
}