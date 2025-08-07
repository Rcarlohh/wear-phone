package com.example.sleepmonitor

import android.Manifest
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
    private var isServiceBound = false

    companion object {
        private const val TAG = "MainActivity"
    }

    // BroadcastReceiver para recibir datos del Galaxy Watch
    private val healthDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "HEALTH_DATA_UPDATE") {
                val heartRate = intent.getIntExtra("heartRate", 0)
                val steps = intent.getIntExtra("steps", 0)
                val sleepQuality = intent.getIntExtra("sleepQuality", 85)
                val sleepDuration = intent.getFloatExtra("sleepDuration", 7.0f)
                
                Log.d(TAG, "healthDataReceiver: Datos recibidos - HR: $heartRate, Steps: $steps")
                
                // Actualizar UI con datos reales
                binding.tvHeartRate.text = "Ritmo Cardíaco: $heartRate BPM"
                binding.tvSteps.text = "Pasos: $steps"
                binding.tvSleepQuality.text = "Calidad del Sueño: $sleepQuality%"
                binding.tvSleepDuration.text = "Duración: $sleepDuration horas"
                
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
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setupBluetooth()
        } else {
            Toast.makeText(this, "Permisos necesarios no concedidos", Toast.LENGTH_LONG).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBluetoothService()
        } else {
            Toast.makeText(this, "Bluetooth es necesario para la aplicación", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "onCreate: Iniciando MainActivity")
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(TAG, "onCreate: ViewBinding configurado")

            viewModel = ViewModelProvider(this)[MainViewModel::class.java]
            
            Log.d(TAG, "onCreate: ViewModel creado")
            
            setupUI()
            registerHealthDataReceiver()
            checkPermissions()
            observeData()
            
            Log.d(TAG, "onCreate: MainActivity inicializada correctamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error al inicializar MainActivity", e)
            Toast.makeText(this, "Error al inicializar la aplicación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        try {
            binding.btnConnect.setOnClickListener {
                if (checkPermissions()) {
                    setupBluetooth()
                }
            }

            binding.btnStartMonitoring.setOnClickListener {
                startMonitoring()
            }

            binding.btnStopMonitoring.setOnClickListener {
                stopMonitoring()
            }
            
            Log.d(TAG, "setupUI: UI configurada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "setupUI: Error al configurar UI", e)
        }
    }

    private fun registerHealthDataReceiver() {
        try {
            val filter = IntentFilter("HEALTH_DATA_UPDATE")
            registerReceiver(healthDataReceiver, filter)
            Log.d(TAG, "registerHealthDataReceiver: Receptor registrado")
        } catch (e: Exception) {
            Log.e(TAG, "registerHealthDataReceiver: Error registrando receptor", e)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        return if (permissionsToRequest.isEmpty()) {
            true
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
            false
        }
    }

    private fun setupBluetooth() {
        try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth no está disponible", Toast.LENGTH_LONG).show()
                return
            }

            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                startBluetoothService()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupBluetooth: Error al configurar Bluetooth", e)
            Toast.makeText(this, "Error al configurar Bluetooth: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startBluetoothService() {
        try {
            val serviceIntent = Intent(this, BluetoothService::class.java)
            startService(serviceIntent)
            
            // Buscar dispositivos Galaxy Watch
            searchForGalaxyWatch()
            
            Toast.makeText(this, "Servicio Bluetooth iniciado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "startBluetoothService: Error al iniciar servicio Bluetooth", e)
            Toast.makeText(this, "Error al iniciar servicio Bluetooth: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun searchForGalaxyWatch() {
        try {
            val pairedDevices = bluetoothAdapter?.bondedDevices
            var galaxyWatchFound = false
            
            pairedDevices?.forEach { device ->
                Log.d(TAG, "searchForGalaxyWatch: Dispositivo encontrado: ${device.name}")
                
                // Buscar Galaxy Watch por nombre
                if (device.name?.contains("Galaxy", ignoreCase = true) == true ||
                    device.name?.contains("Watch", ignoreCase = true) == true ||
                    device.name?.contains("Samsung", ignoreCase = true) == true) {
                    
                    Log.d(TAG, "searchForGalaxyWatch: Galaxy Watch encontrado: ${device.name}")
                    connectToGalaxyWatch(device)
                    galaxyWatchFound = true
                }
            }
            
            if (!galaxyWatchFound) {
                Toast.makeText(this, "No se encontró Galaxy Watch. Asegúrate de que esté emparejado.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchForGalaxyWatch: Error buscando Galaxy Watch", e)
        }
    }

    private fun connectToGalaxyWatch(device: BluetoothDevice) {
        try {
            Log.d(TAG, "connectToGalaxyWatch: Intentando conectar a ${device.name}")
            
            // Iniciar servicio y conectar
            val serviceIntent = Intent(this, BluetoothService::class.java)
            startService(serviceIntent)
            
            // Simular conexión exitosa (en una implementación real, esto se haría a través del servicio)
            binding.tvConnectionStatus.text = "Estado: Conectado a ${device.name}"
            binding.btnConnect.isEnabled = false
            
            Toast.makeText(this, "Conectado a ${device.name}", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "connectToGalaxyWatch: Error conectando a Galaxy Watch", e)
            Toast.makeText(this, "Error conectando a Galaxy Watch: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startMonitoring() {
        try {
            Log.d(TAG, "startMonitoring: Iniciando monitoreo")
            viewModel.startMonitoring()
            binding.btnStartMonitoring.isEnabled = false
            binding.btnStopMonitoring.isEnabled = true
            Toast.makeText(this, "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "startMonitoring: Error iniciando monitoreo", e)
            Toast.makeText(this, "Error iniciando monitoreo: ${e.message}", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Error deteniendo monitoreo: ${e.message}", Toast.LENGTH_LONG).show()
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
                binding.tvConnectionStatus.text = "Estado: $status"
                binding.btnConnect.isEnabled = status != "Conectado"
            }

            viewModel.sleepData.observe(this) { sleepData ->
                binding.tvSleepQuality.text = "Calidad del Sueño: ${sleepData.quality}%"
                binding.tvSleepDuration.text = "Duración: ${sleepData.duration} horas"
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
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error al limpiar recursos", e)
        }
    }
} 