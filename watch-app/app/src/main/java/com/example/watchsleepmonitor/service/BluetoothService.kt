package com.example.watchsleepmonitor.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothService {

    companion object {
        private const val TAG = "WatchBluetoothService"
        private val UUID_SERVICE = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB")
        
        private var bluetoothSocket: BluetoothSocket? = null
        private var outputStream: OutputStream? = null
        private var isConnected = false
        private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        fun connectToPhone(device: BluetoothDevice): Boolean {
            return try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SERVICE)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                isConnected = true
                Log.d(TAG, "Connected to phone")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to phone", e)
                false
            }
        }

        fun sendDataToPhone(data: String): Boolean {
            return if (isConnected && outputStream != null) {
                try {
                    serviceScope.launch {
                        outputStream?.write(data.toByteArray())
                        outputStream?.flush()
                        Log.d(TAG, "Data sent to phone: $data")
                    }
                    true
                } catch (e: IOException) {
                    Log.e(TAG, "Error sending data to phone", e)
                    false
                }
            } else {
                Log.w(TAG, "Not connected to phone")
                false
            }
        }

        fun disconnect() {
            isConnected = false
            try {
                outputStream?.close()
                bluetoothSocket?.close()
                Log.d(TAG, "Disconnected from phone")
            } catch (e: IOException) {
                Log.e(TAG, "Error closing connections", e)
            }
        }

        fun isConnected(): Boolean = isConnected
    }
} 