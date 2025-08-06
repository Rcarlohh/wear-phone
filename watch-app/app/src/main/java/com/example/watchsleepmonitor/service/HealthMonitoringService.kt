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
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.MeasureCallback
import kotlinx.coroutines.*
import com.google.common.util.concurrent.ListenableFuture
import com.example.watchsleepmonitor.utils.HealthDataUtils
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HealthMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var healthServicesClient: HealthServicesClient
    private lateinit var passiveMonitoringClient: PassiveMonitoringClient
    private lateinit var measureClient: MeasureClient
    private var isMonitoring = false
    private var measureCallback: MeasureCallback? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val TAG = "HealthMonitoringService"
    }

    override fun onCreate() {
        super.onCreate()
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
                    checkCapabilitiesAndStart()
                    Log.d(TAG, "Health monitoring started")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting health monitoring", e)
                }
            }
        }
    }

    private fun checkCapabilitiesAndStart() {
        val capabilitiesFuture = passiveMonitoringClient.getCapabilitiesAsync()
        val measureCapabilitiesFuture = measureClient.getCapabilitiesAsync()

        capabilitiesFuture.addListener({
            try {
                val capabilities = capabilitiesFuture.get()
                Log.d(TAG, "Passive monitoring capabilities: ${capabilities.supportedDataTypesPassiveMonitoring}")

                if (capabilities.supportedDataTypesPassiveMonitoring.isNotEmpty()) {
                    startPassiveMonitoring(capabilities.supportedDataTypesPassiveMonitoring)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting passive monitoring capabilities", e)
            }
        }, executor)

        measureCapabilitiesFuture.addListener({
            try {
                val measureCapabilities = measureCapabilitiesFuture.get()
                Log.d(TAG, "Measure capabilities: ${measureCapabilities.supportedDataTypesMeasure}")

                if (DataType.HEART_RATE_BPM in measureCapabilities.supportedDataTypesMeasure) {
                    startActiveHeartRateMonitoring()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting measure capabilities", e)
            }
        }, executor)
    }

    private fun startPassiveMonitoring(supportedTypes: Set<DataType<*, *>>) {
        try {
            val typesToMonitor = mutableListOf<DataType<*, *>>()

            if (DataType.STEPS_DAILY in supportedTypes) {
                typesToMonitor.add(DataType.STEPS_DAILY)
            }

            if (DataType.HEART_RATE_BPM in supportedTypes) {
                typesToMonitor.add(DataType.HEART_RATE_BPM)
            }

            if (typesToMonitor.isNotEmpty()) {
                val future = passiveMonitoringClient.setPassiveListenerServiceAsync(
                    HealthDataListenerService::class.java,
                    typesToMonitor
                )

                future.addListener({
                    try {
                        future.get()
                        Log.d(TAG, "Passive monitoring configured for: $typesToMonitor")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error configuring passive monitoring", e)
                    }
                }, executor)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up passive monitoring", e)
        }
    }

    private fun startActiveHeartRateMonitoring() {
        try {
            measureCallback = object : MeasureCallback {
                override fun onAvailabilityChanged(
                    dataType: DeltaDataType<*, *>,
                    availability: Availability
                ) {
                    Log.d(TAG, "Heart rate availability changed: $availability for $dataType")
                }

                override fun onDataReceived(data: DataPointContainer) {
                    processHeartRateData(data)
                }
            }

            val future = measureClient.registerMeasureCallback(
                DataType.HEART_RATE_BPM,
                measureCallback!!
            )

            future.addListener({
                try {
                    future.get()
                    Log.d(TAG, "Active heart rate monitoring started")
                } catch (e: Exception) {
                    Log.e(TAG, "Error registering heart rate callback", e)
                }
            }, executor)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting active heart rate monitoring", e)
        }
    }

    private fun processHeartRateData(dataPoints: DataPointContainer) {
        val heartRatePoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        heartRatePoints.forEach { dataPoint ->
            if (dataPoint is SampleDataPoint<*>) {
                val heartRate = HealthDataUtils.extractIntValue(dataPoint)

                if (HealthDataUtils.isValidHeartRate(heartRate)) {
                    val timestamp = System.currentTimeMillis()

                    Log.d(TAG, "Heart rate: $heartRate BPM")

                    val intent = Intent("HEALTH_DATA_UPDATE")
                    intent.putExtra("heartRate", heartRate)
                    intent.putExtra("timestamp", timestamp)
                    sendBroadcast(intent)

                    try {
                        BluetoothService.sendDataToPhone("HR:$heartRate")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not send heart rate data via Bluetooth", e)
                    }
                } else {
                    Log.w(TAG, "Invalid heart rate received: $heartRate")
                }
            }
        }
    }

    private fun stopHealthMonitoring() {
        isMonitoring = false

        try {
            val passiveFuture = passiveMonitoringClient.clearPassiveListenerServiceAsync()
            passiveFuture.addListener({
                try {
                    passiveFuture.get()
                    Log.d(TAG, "Passive monitoring stopped")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping passive monitoring", e)
                }
            }, executor)

            measureCallback?.let { callback ->
                val measureFuture = measureClient.unregisterMeasureCallbackAsync(
                    DataType.HEART_RATE_BPM,
                    callback
                )
                measureFuture.addListener({
                    try {
                        measureFuture.get()
                        Log.d(TAG, "Heart rate callback unregistered")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unregistering heart rate callback", e)
                    }
                }, executor)
            }

            Log.d(TAG, "Health monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping health monitoring", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopHealthMonitoring()
        serviceScope.cancel()
        if (executor is ExecutorService) {
            (executor as ExecutorService).shutdown()
        }
    }
}
