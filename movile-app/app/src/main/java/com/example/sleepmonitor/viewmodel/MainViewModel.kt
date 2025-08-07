package com.example.sleepmonitor.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepmonitor.data.SleepData
import com.example.sleepmonitor.repository.HealthDataRepository
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HealthDataRepository(application.applicationContext)

    private val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int> = _heartRate

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private val _connectionStatus = MutableLiveData<String>("Desconectado")
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _sleepData = MutableLiveData<SleepData>()
    val sleepData: LiveData<SleepData> = _sleepData

    private var isMonitoring = false

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun updateHeartRate(heartRate: Int) {
        try {
            Log.d(TAG, "updateHeartRate: Actualizando ritmo cardíaco a $heartRate")
            _heartRate.postValue(heartRate)
        } catch (e: Exception) {
            Log.e(TAG, "updateHeartRate: Error actualizando ritmo cardíaco", e)
        }
    }

    fun updateSteps(steps: Int) {
        try {
            Log.d(TAG, "updateSteps: Actualizando pasos a $steps")
            _stepCount.postValue(steps)
        } catch (e: Exception) {
            Log.e(TAG, "updateSteps: Error actualizando pasos", e)
        }
    }

    fun startMonitoring() {
        try {
            if (!isMonitoring) {
                Log.d(TAG, "startMonitoring: Iniciando monitoreo")
                isMonitoring = true
                _connectionStatus.value = "Conectado"
                viewModelScope.launch {
                    repository.startMonitoring { heartRate, steps ->
                        try {
                            _heartRate.postValue(heartRate)
                            _stepCount.postValue(steps)
                            Log.d(TAG, "startMonitoring: Datos actualizados - HR: $heartRate, Steps: $steps")
                        } catch (e: Exception) {
                            Log.e(TAG, "startMonitoring: Error al actualizar datos", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "startMonitoring: Error al iniciar monitoreo", e)
            _connectionStatus.value = "Error"
        }
    }

    fun stopMonitoring() {
        try {
            Log.d(TAG, "stopMonitoring: Deteniendo monitoreo")
            isMonitoring = false
            _connectionStatus.value = "Desconectado"
            viewModelScope.launch {
                repository.stopMonitoring()
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopMonitoring: Error al detener monitoreo", e)
        }
    }

    fun updateSleepData(sleepData: SleepData) {
        try {
            Log.d(TAG, "updateSleepData: Actualizando datos de sueño")
            _sleepData.value = sleepData
            viewModelScope.launch {
                repository.saveSleepData(sleepData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateSleepData: Error al actualizar datos de sueño", e)
        }
    }

    fun getSleepHistory(): LiveData<List<SleepData>> {
        return repository.getSleepHistory()
    }

    fun calculateSleepQuality(heartRate: Int, steps: Int, duration: Float): Int {
        try {
            // Algoritmo simple para calcular calidad del sueño
            var quality = 100
            
            // Reducir calidad si el ritmo cardíaco es muy alto o muy bajo
            when {
                heartRate > 100 -> quality -= 20
                heartRate > 80 -> quality -= 10
                heartRate < 50 -> quality -= 15
            }
            
            // Reducir calidad si hay muchos pasos (indicando movimiento durante el sueño)
            if (steps > 100) {
                quality -= 15
            } else if (steps > 50) {
                quality -= 10
            }
            
            // Ajustar por duración del sueño
            when {
                duration < 6 -> quality -= 20
                duration < 7 -> quality -= 10
                duration > 9 -> quality -= 5
            }
            
            return quality.coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "calculateSleepQuality: Error al calcular calidad del sueño", e)
            return 50 // Valor por defecto en caso de error
        }
    }
} 