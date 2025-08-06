package com.example.watchsleepmonitor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.watchsleepmonitor.databinding.ActivityMainBinding
import com.example.watchsleepmonitor.service.HealthMonitoringService
import com.example.watchsleepmonitor.viewmodel.WatchViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WatchViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startHealthMonitoring()
        } else {
            Toast.makeText(this, "Permisos necesarios no concedidos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WatchViewModel::class.java]

        setupUI()
        checkPermissions()
        observeData()
    }

    private fun setupUI() {
        binding.btnStartMonitoring.setOnClickListener {
            if (checkPermissions()) {
                startHealthMonitoring()
            }
        }

        binding.btnStopMonitoring.setOnClickListener {
            stopHealthMonitoring()
        }

        binding.btnSendData.setOnClickListener {
            viewModel.sendDataToPhone()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.BODY_SENSORS_BACKGROUND,
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

    private fun startHealthMonitoring() {
        val serviceIntent = Intent(this, HealthMonitoringService::class.java)
        // Corregir el Intent putExtra - usar String específicamente
        serviceIntent.putExtra("action", "START_MONITORING")
        serviceIntent.action = "START_MONITORING"
        startService(serviceIntent)
        viewModel.startMonitoring()
        Toast.makeText(this, "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun stopHealthMonitoring() {
        val serviceIntent = Intent(this, HealthMonitoringService::class.java)
        // Corregir el Intent putExtra - usar String específicamente
        serviceIntent.putExtra("action", "STOP_MONITORING")
        serviceIntent.action = "STOP_MONITORING"
        stopService(serviceIntent)
        viewModel.stopMonitoring()
        Toast.makeText(this, "Monitoreo detenido", Toast.LENGTH_SHORT).show()
    }

    private fun observeData() {
        viewModel.heartRate.observe(this) { heartRate ->
            binding.tvHeartRate.text = "$heartRate BPM"
        }

        viewModel.stepCount.observe(this) { steps ->
            binding.tvSteps.text = "$steps pasos"
        }

        viewModel.isMonitoring.observe(this) { isMonitoring ->
            binding.btnStartMonitoring.isEnabled = !isMonitoring
            binding.btnStopMonitoring.isEnabled = isMonitoring
            binding.btnSendData.isEnabled = isMonitoring
        }

        viewModel.connectionStatus.observe(this) { status ->
            binding.tvConnectionStatus.text = status
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMonitoring()
    }
}