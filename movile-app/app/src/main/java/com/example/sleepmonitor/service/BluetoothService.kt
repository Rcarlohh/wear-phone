package com.example.sleepmonitor.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService : Service() {

    private val binder = LocalBinder()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "BluetoothService"
        private val UUID_SERVICE = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB")
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        return try {
            // Use secure RFCOMM for API 31+
            bluetoothSocket = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                device.createRfcommSocketToServiceRecord(UUID_SERVICE)
            } else {
                @Suppress("DEPRECATION")
                device.createRfcommSocketToServiceRecord(UUID_SERVICE)
            }
            
            bluetoothSocket?.connect()
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            isConnected = true
            startListening()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            false
        }
    }

    private fun startListening() {
        serviceScope.launch {
            val buffer = ByteArray(1024)
            while (isConnected) {
                try {
                    val bytes = inputStream?.read(buffer)
                    if (bytes != null && bytes > 0) {
                        val data = String(buffer, 0, bytes)
                        processReceivedData(data)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading from input stream", e)
                    break
                }
            }
        }
    }

    private fun processReceivedData(data: String) {
        try {
            val parts = data.split(",")
            if (parts.size >= 2) {
                val heartRate = parts[0].toInt()
                val steps = parts[1].toInt()
                
                // Notificar a la actividad principal
                val intent = Intent("HEALTH_DATA_UPDATE")
                intent.putExtra("heartRate", heartRate)
                intent.putExtra("steps", steps)
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received data", e)
        }
    }

    fun sendCommand(command: String): Boolean {
        return try {
            outputStream?.write(command.toByteArray())
            outputStream?.flush()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            false
        }
    }

    fun requestHealthData() {
        sendCommand("GET_HEALTH_DATA")
    }

    fun disconnect() {
        isConnected = false
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connections", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }

    fun isConnected(): Boolean = isConnected
} 