package com.example.sleepmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmonitor.databinding.ActivityMainBinding
import com.example.sleepmonitor.data.SleepData
import com.example.sleepmonitor.service.BluetoothService
import com.example.sleepmonitor.viewmodel.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothService: BluetoothService? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    // BroadcastReceiver para recibir datos del Galaxy Watch
    private val healthDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "HEALTH_DATA_UPDATE" -> {
                    val heartRate = intent.getIntExtra("heartRate", 0)
                    val steps = intent.getIntExtra("steps", 0)
                    val sleepQuality = intent.getIntExtra("sleepQuality", 85)
                    val sleepDuration = intent.getFloatExtra("sleepDuration", 7.0f)

                    Log.d(
                        TAG,
                        "healthDataReceiver: Datos recibidos - HR: $heartRate, Steps: $steps"
                    )

                    // Actualizar UI con datos reales
                    runOnUiThread {
                        binding.tvHeartRate.text = "Ritmo Cardíaco: $heartRate BPM"
                        binding.tvSteps.text = "Pasos: $steps"
                        binding.tvSleepQuality.text = "Calidad del Sueño: $sleepQuality%"
                        binding.tvSleepDuration.text =
                            "Duración: ${String.format("%.1f", sleepDuration)} horas"
                    }

                    // Actualizar ViewModel
                    viewModel.updateHeartRate(heartRate)
                    viewModel.updateSteps(steps)

                    // Crear y guardar datos de sueño
                    val sleepData = SleepData(
                        date = Date(),
                        duration = sleepDuration,
                        quality = sleepQuality,
                        heartRate = heartRate,
                        stepCount = steps,
                        deepSleepDuration = sleepDuration * 0.3f,
                        lightSleepDuration = sleepDuration * 0.5f,
                        remSleepDuration = sleepDuration * 0.2f
                    )
                    viewModel.updateSleepData(sleepData)
                }

                "BLUETOOTH_CONNECTION_STATUS" -> {
                    val isConnected = intent.getBooleanExtra("isConnected", false)
                    val deviceName = intent.getStringExtra("deviceName")

                    runOnUiThread {
                        if (isConnected) {
                            binding.tvConnectionStatus.text =
                                "Estado: Conectado a ${deviceName ?: "Galaxy Watch"}"
                            binding.btnConnect.text = "Desconectar"
                            binding.btnConnect.setBackgroundColor(getColor(R.color.error_color))
                            binding.btnStartMonitoring.isEnabled = true
                            Toast.makeText(
                                this@MainActivity,
                                "Conectado a $deviceName",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            binding.tvConnectionStatus.text = "Estado: Desconectado"
                            binding.btnConnect.text = "Conectar con Galaxy Watch"
                            binding.btnConnect.setBackgroundColor(getColor(R.color.primary_color))
                            binding.btnStartMonitoring.isEnabled = false
                            binding.btnStopMonitoring.isEnabled = false
                            Toast.makeText(this@MainActivity, "Desconectado", Toast.LENGTH_SHORT)
                                .show()
                        }
                        viewModel.updateConnectionStatus(if (isConnected) "Conectado" else "Desconectado")
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d(TAG, "Todos los permisos concedidos")
            setupBluetooth()
        } else {
            val deniedPermissions = permissions.entries.filter { !it.value }.map { it.key }
            Log.e(TAG, "Permisos denegados: $deniedPermissions")
            Toast.makeText(
                this,
                "Permisos necesarios no concedidos: ${deniedPermissions.joinToString()}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Bluetooth habilitado")
            searchForGalaxyWatch()
        } else {
            Toast.makeText(this, "Bluetooth es necesario para la aplicación", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "onCreate: Iniciando MainActivity")

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            viewModel = ViewModelProvider(this)[MainViewModel::class.java]

            setupUI()
            registerHealthDataReceiver()
            checkPermissions()
            observeData()

            Log.d(TAG, "onCreate: MainActivity inicializada correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error al inicializar MainActivity", e)
            Toast.makeText(
                this,
                "Error al inicializar la aplicación: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupUI() {
        try {
            binding.btnConnect.setOnClickListener {
                if (bluetoothService?.isConnected() == true) {
                    disconnect()
                } else {
                    if (checkPermissions()) {
                        setupBluetooth()
                    }
                }
            }

            binding.btnStartMonitoring.setOnClickListener {
                startMonitoring()
            }

            binding.btnStopMonitoring.setOnClickListener {
                stopMonitoring()
            }

            // Estado inicial de los botones
            binding.btnStartMonitoring.isEnabled = false
            binding.btnStopMonitoring.isEnabled = false

            Log.d(TAG, "setupUI: UI configurada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "setupUI: Error al configurar UI", e)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerHealthDataReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("HEALTH_DATA_UPDATE")
                addAction("BLUETOOTH_CONNECTION_STATUS")
            }
            registerReceiver(healthDataReceiver, filter)
            Log.d(TAG, "registerHealthDataReceiver: Receptores registrados")
        } catch (e: Exception) {
            Log.e(TAG, "registerHealthDataReceiver: Error registrando receptores", e)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            // Permisos específicos para Android 12+ (API 31+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        return if (permissionsToRequest.isEmpty()) {
            true
        } else {
            Log.d(TAG, "Solicitando permisos: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest)
            false
        }
    }

    private fun setupBluetooth() {
        try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter == null) {
                Toast.makeText(
                    this,
                    "Bluetooth no está disponible en este dispositivo",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                searchForGalaxyWatch()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupBluetooth: Error al configurar Bluetooth", e)
            Toast.makeText(this, "Error al configurar Bluetooth: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun searchForGalaxyWatch() {
        try {
            Log.d(TAG, "searchForGalaxyWatch: Buscando dispositivos emparejados")

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permiso BLUETOOTH_CONNECT requerido", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val pairedDevices = bluetoothAdapter?.bondedDevices
            var galaxyWatchFound = false

            pairedDevices?.forEach { device ->
                Log.d(
                    TAG,
                    "searchForGalaxyWatch: Dispositivo encontrado: ${device.name} - ${device.address}"
                )

                // Buscar Galaxy Watch por nombre (más específico)
                device.name?.let { name ->
                    if (name.contains("Galaxy Watch", ignoreCase = true) ||
                        name.contains("Samsung Galaxy Watch", ignoreCase = true) ||
                        name.contains("SM-R", ignoreCase = true) || // Modelo típico de Galaxy Watch
                        name.contains("Watch4", ignoreCase = true) ||
                        name.contains("Watch5", ignoreCase = true) ||
                        name.contains("Watch6", ignoreCase = true) ||
                        name.contains("Watch7", ignoreCase = true)
                    ) {

                        Log.d(TAG, "searchForGalaxyWatch: Galaxy Watch encontrado: $name")
                        connectToGalaxyWatch(device)
                        galaxyWatchFound = true
                    }
                }
            }

            if (!galaxyWatchFound) {
                Log.w(TAG, "searchForGalaxyWatch: No se encontró Galaxy Watch")
                Toast.makeText(
                    this,
                    "No se encontró Galaxy Watch emparejado.\nAsegúrate de que esté emparejado en la configuración de Bluetooth.",
                    Toast.LENGTH_LONG
                ).show()

                // Mostrar lista de dispositivos disponibles para debug
                pairedDevices?.forEach { device ->
                    Log.d(TAG, "Dispositivo disponible: ${device.name} - ${device.address}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchForGalaxyWatch: Error buscando Galaxy Watch", e)
            Toast.makeText(this, "Error buscando dispositivos: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToGalaxyWatch(device: BluetoothDevice) {
        try {
            Log.d(TAG, "connectToGalaxyWatch: Intentando conectar a ${device.name}")

            // Crear e iniciar servicio Bluetooth
            val serviceIntent = Intent(this, BluetoothService::class.java)
            startService(serviceIntent)

            // Simular un pequeño delay para que el servicio se inicie
            binding.tvConnectionStatus.text = "Estado: Conectando..."
            binding.btnConnect.isEnabled = false

            // En un hilo separado para no bloquear la UI
            Thread {
                try {
                    Thread.sleep(1000) // Esperar a que el servicio se inicie

                    // Aquí intentarías la conexión real con el dispositivo
                    // Por ahora, simularemos una conexión exitosa
                    val success = simulateConnection(device)

                    runOnUiThread {
                        if (success) {
                            binding.tvConnectionStatus.text = "Estado: Conectado a ${device.name}"
                            binding.btnConnect.text = "Desconectar"
                            binding.btnConnect.setBackgroundColor(getColor(R.color.error_color))
                            binding.btnStartMonitoring.isEnabled = true
                            viewModel.updateConnectionStatus("Conectado")
                            Toast.makeText(
                                this@MainActivity,
                                "Conectado a ${device.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            binding.tvConnectionStatus.text = "Estado: Error de conexión"
                            binding.btnConnect.text = "Reintentar conexión"
                            binding.btnStartMonitoring.isEnabled = false
                            Toast.makeText(
                                this@MainActivity,
                                "Error conectando a ${device.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.btnConnect.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "connectToGalaxyWatch: Error en hilo de conexión", e)
                    runOnUiThread {
                        binding.tvConnectionStatus.text = "Estado: Error de conexión"
                        binding.btnConnect.isEnabled = true
                    }
                }
            }.start()

        } catch (e: Exception) {
            Log.e(TAG, "connectToGalaxyWatch: Error conectando a Galaxy Watch", e)
            Toast.makeText(
                this,
                "Error conectando a Galaxy Watch: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnConnect.isEnabled = true
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun simulateConnection(device: BluetoothDevice): Boolean {
        // En una implementación real, aquí usarías el BluetoothService
        // Para propósitos de demostración, simularemos una conexión exitosa
        return try {
            Log.d(TAG, "simulateConnection: Simulando conexión a ${device.name}")
            // Simular tiempo de conexión
            Thread.sleep(2000)
            true
        } catch (e: Exception) {
            Log.e(TAG, "simulateConnection: Error en simulación", e)
            false
        }
    }

    private fun disconnect() {
        try {
            Log.d(TAG, "disconnect: Desconectando dispositivo")

            // Detener monitoreo si está activo
            stopMonitoring()

            // Actualizar UI
            binding.tvConnectionStatus.text = "Estado: Desconectado"
            binding.btnConnect.text = "Conectar con Galaxy Watch"
            binding.btnConnect.setBackgroundColor(getColor(R.color.primary_color))
            binding.btnStartMonitoring.isEnabled = false
            binding.btnStopMonitoring.isEnabled = false

            // Actualizar ViewModel
            viewModel.updateConnectionStatus("Desconectado")

            // Detener servicio
            val serviceIntent = Intent(this, BluetoothService::class.java)
            stopService(serviceIntent)

            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "disconnect: Error desconectando", e)
        }
    }

    private fun startMonitoring() {
        try {
            Log.d(TAG, "startMonitoring: Iniciando monitoreo")
            viewModel.startMonitoring()
            binding.btnStartMonitoring.isEnabled = false
            binding.btnStopMonitoring.isEnabled = true

            // Solicitar datos al servicio Bluetooth si existe
            bluetoothService?.requestHealthData()

            Toast.makeText(this, "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "startMonitoring: Error iniciando monitoreo", e)
            Toast.makeText(this, "Error iniciando monitoreo: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopMonitoring() {
        try {
            Log.d(TAG, "stopMonitoring: Deteniendo monitoreo")
            viewModel.stopMonitoring()
            binding.btnStartMonitoring.isEnabled = true
            binding.btnStopMonitoring.isEnabled = false
            Toast.makeText(this, "Monitoreo detenido", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "stopMonitoring: Error deteniendo monitoreo", e)
            Toast.makeText(this, "Error deteniendo monitoreo: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun observeData() {
        try {
            viewModel.heartRate.observe(this) { heartRate ->
                binding.tvHeartRate.text = "Ritmo Cardíaco: $heartRate BPM"
            }

            viewModel.stepCount.observe(this) { steps ->
                binding.tvSteps.text = "Pasos: $steps"
            }

            viewModel.connectionStatus.observe(this) { status ->
                if (status == "Conectado") {
                    binding.btnConnect.text = "Desconectar"
                    binding.btnConnect.setBackgroundColor(getColor(R.color.error_color))
                    binding.btnStartMonitoring.isEnabled = true
                } else {
                    binding.btnConnect.text = "Conectar con Galaxy Watch"
                    binding.btnConnect.setBackgroundColor(getColor(R.color.primary_color))
                    binding.btnStartMonitoring.isEnabled = false
                }
            }

            viewModel.sleepData.observe(this) { sleepData ->
                binding.tvSleepQuality.text = "Calidad del Sueño: ${sleepData.quality}%"
                binding.tvSleepDuration.text =
                    "Duración: ${String.format("%.1f", sleepData.duration)} horas"
            }

            Log.d(TAG, "observeData: Observadores configurados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "observeData: Error al configurar observadores", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(healthDataReceiver)
            viewModel.stopMonitoring()

            // Detener servicio
            val serviceIntent = Intent(this, BluetoothService::class.java)
            stopService(serviceIntent)

        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error al limpiar recursos", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar estado de conexión al reanudar
        try {
            if (bluetoothService?.isConnected() == true) {
                binding.tvConnectionStatus.text = "Estado: Conectado"
                binding.btnConnect.text = "Desconectar"
                binding.btnStartMonitoring.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "onResume: Error verificando estado", e)
        }
    }
}