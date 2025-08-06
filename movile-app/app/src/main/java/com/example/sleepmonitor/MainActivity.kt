package com.example.sleepmonitor

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmonitor.databinding.ActivityMainBinding
import com.example.sleepmonitor.service.BluetoothService
import com.example.sleepmonitor.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var bluetoothAdapter: BluetoothAdapter? = null

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupUI()
        checkPermissions()
        observeData()
    }

    private fun setupUI() {
        binding.btnConnect.setOnClickListener {
            if (checkPermissions()) {
                setupBluetooth()
            }
        }

        binding.btnStartMonitoring.setOnClickListener {
            viewModel.startMonitoring()
        }

        binding.btnStopMonitoring.setOnClickListener {
            viewModel.stopMonitoring()
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
    }

    private fun startBluetoothService() {
        val serviceIntent = Intent(this, BluetoothService::class.java)
        startService(serviceIntent)
        Toast.makeText(this, "Servicio Bluetooth iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun observeData() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMonitoring()
    }
} 