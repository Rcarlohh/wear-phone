package com.example.watchsleepmonitor.service

import android.content.Intent
import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.SampleDataPoint

class HealthDataListenerService : PassiveListenerService() {

    companion object {
        private const val TAG = "HealthDataListener"
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        Log.d(TAG, "New data points received")

        // Procesar datos de pasos diarios
        val stepPoints = dataPoints.getData(DataType.STEPS_DAILY)
        stepPoints.forEach { dataPoint ->
            if (dataPoint is SampleDataPoint<*>) {
                val steps = (dataPoint.value as? Long)?.toInt() ?: 0
                val timestamp = System.currentTimeMillis()

                Log.d(TAG, "Steps: $steps at $timestamp")

                // Enviar datos al ViewModel
                val intent = Intent("HEALTH_DATA_UPDATE")
                intent.putExtra("stepCount", steps)
                intent.putExtra("timestamp", timestamp)
                sendBroadcast(intent)

                // Enviar vía Bluetooth
                try {
                    BluetoothService.sendDataToPhone("STEPS:$steps")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not send step data via Bluetooth", e)
                }
            }
        }

        // Procesar datos de ritmo cardíaco (si vienen por monitoreo pasivo)
        val heartRatePoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        heartRatePoints.forEach { dataPoint ->
            if (dataPoint is SampleDataPoint<*>) {
                val heartRate = (dataPoint.value as? Double)?.toInt() ?: 0
                val timestamp = System.currentTimeMillis()

                Log.d(TAG, "Heart Rate: $heartRate BPM at $timestamp")

                // Enviar datos al ViewModel
                val intent = Intent("HEALTH_DATA_UPDATE")
                intent.putExtra("heartRate", heartRate)
                intent.putExtra("timestamp", timestamp)
                sendBroadcast(intent)

                // Enviar vía Bluetooth
                try {
                    BluetoothService.sendDataToPhone("HR:$heartRate")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not send heart rate data via Bluetooth", e)
                }
            }
        }
    }

    override fun onUserActivityInfoReceived(info: androidx.health.services.client.data.UserActivityInfo) {
        Log.d(TAG, "User activity info: ${info.userActivityState}")

        // Enviar información de actividad
        val intent = Intent("HEALTH_DATA_UPDATE")
        intent.putExtra("activityState", info.userActivityState.toString())
        intent.putExtra("exerciseInfo", info.exerciseInfo?.exerciseType?.toString() ?: "NONE")
        sendBroadcast(intent)
    }
}