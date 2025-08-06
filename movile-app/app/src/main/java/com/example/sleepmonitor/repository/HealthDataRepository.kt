package com.example.sleepmonitor.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.sleepmonitor.data.SleepData
import com.example.sleepmonitor.database.SleepDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class HealthDataRepository(private val context: Context) {
    
    private val database = SleepDatabase.getDatabase(context)
    private val sleepDao = database.sleepDao()

    suspend fun saveSleepData(sleepData: SleepData) {
        withContext(Dispatchers.IO) {
            sleepDao.insert(sleepData)
        }
    }

    fun getSleepHistory(): LiveData<List<SleepData>> {
        return sleepDao.getAllSleepData()
    }

    suspend fun startMonitoring(callback: (Int, Int) -> Unit) {
        // Simulación de monitoreo continuo
        // En una implementación real, esto se conectaría con el servicio Bluetooth
        withContext(Dispatchers.IO) {
            while (true) {
                // Simular datos de salud
                val heartRate = (60..100).random()
                val steps = (0..50).random()
                callback(heartRate, steps)
                kotlinx.coroutines.delay(5000) // Actualizar cada 5 segundos
            }
        }
    }

    suspend fun stopMonitoring() {
        // Detener el monitoreo
    }

    suspend fun getLatestSleepData(): SleepData? {
        return withContext(Dispatchers.IO) {
            sleepDao.getLatestSleepData()
        }
    }

    suspend fun deleteSleepData(sleepData: SleepData) {
        withContext(Dispatchers.IO) {
            sleepDao.delete(sleepData)
        }
    }
} 