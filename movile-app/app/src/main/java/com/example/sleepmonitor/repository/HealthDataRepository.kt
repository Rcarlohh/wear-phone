package com.example.sleepmonitor.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.sleepmonitor.data.SleepData
import com.example.sleepmonitor.database.SleepDatabase
import kotlinx.coroutines.*
import java.util.*

class HealthDataRepository(private val context: Context) {
    
    private val database = SleepDatabase.getDatabase(context)
    private val sleepDao = database.sleepDao()
    private var monitoringJob: Job? = null

    companion object {
        private const val TAG = "HealthDataRepository"
    }

    suspend fun saveSleepData(sleepData: SleepData) {
        try {
            withContext(Dispatchers.IO) {
                sleepDao.insert(sleepData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sleep data", e)
        }
    }

    fun getSleepHistory(): LiveData<List<SleepData>> {
        return sleepDao.getAllSleepData()
    }

    suspend fun startMonitoring(callback: (Int, Int) -> Unit) {
        try {
            // Cancelar monitoreo anterior si existe
            monitoringJob?.cancel()
            
            monitoringJob = CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "startMonitoring: Iniciando monitoreo")
                
                while (isActive) {
                    try {
                        // Simular datos de salud
                        val heartRate = (60..100).random()
                        val steps = (0..100).random()
                        
                        Log.d(TAG, "startMonitoring: Datos simulados - HR: $heartRate, Steps: $steps")
                        
                        callback(heartRate, steps)
                        delay(5000) // Actualizar cada 5 segundos
                    } catch (e: Exception) {
                        Log.e(TAG, "startMonitoring: Error en monitoreo", e)
                        delay(1000) // Esperar antes de reintentar
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "startMonitoring: Error al iniciar monitoreo", e)
        }
    }

    suspend fun stopMonitoring() {
        try {
            Log.d(TAG, "stopMonitoring: Deteniendo monitoreo")
            monitoringJob?.cancel()
            monitoringJob = null
        } catch (e: Exception) {
            Log.e(TAG, "stopMonitoring: Error al detener monitoreo", e)
        }
    }

    suspend fun getLatestSleepData(): SleepData? {
        return try {
            withContext(Dispatchers.IO) {
                sleepDao.getLatestSleepData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest sleep data", e)
            null
        }
    }

    suspend fun deleteSleepData(sleepData: SleepData) {
        try {
            withContext(Dispatchers.IO) {
                sleepDao.delete(sleepData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting sleep data", e)
        }
    }
} 