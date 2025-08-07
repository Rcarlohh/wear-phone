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

    private val _heartRate = MutableLiveData<Int>().apply { value = 0 }
    val heartRate: LiveData<Int> = _heartRate

    private val _stepCount = MutableLiveData<Int>().apply { value = 0 }
    val stepCount: LiveData<Int> = _stepCount

    private val _connectionStatus = MutableLiveData<String>().apply { value = "Desconectado" }
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _sleepData = MutableLiveData<SleepData>().apply {
        value = SleepData(
            date = Date(),
            duration = 0.0f,
            quality = 0,
            heartRate = 0,
            stepCount = 0,
            deepSleepDuration = 0.0f,
            lightSleepDuration = 0.0f,
            remSleepDuration = 0.0f
        )
    }
    val sleepData: LiveData<SleepData> = _sleepData

    private val _isMonitoring = MutableLiveData<Boolean>().apply { value = false }
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun updateHeartRate(heartRate: Int) {
        try {
            Log.d(TAG, "updateHeartRate: Actualizando ritmo cardíaco a $heartRate")
            if (heartRate in 30..250) { // Validación de rango razonable
                _heartRate.postValue(heartRate)
            } else {
                Log.w(TAG, "updateHeartRate: Valor fuera de rango: $heartRate")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateHeartRate: Error actualizando ritmo cardíaco", e)
            _errorMessage.postValue("Error actualizando ritmo cardíaco")
        }
    }

    fun updateSteps(steps: Int) {
        try {
            Log.d(TAG, "updateSteps: Actualizando pasos a $steps")
            if (steps >= 0) { // Los pasos no pueden ser negativos
                _stepCount.postValue(steps)
            } else {
                Log.w(TAG, "updateSteps: Valor negativo de pasos: $steps")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateSteps: Error actualizando pasos", e)
            _errorMessage.postValue("Error actualizando pasos")
        }
    }

    fun updateConnectionStatus(status: String) {
        try {
            Log.d(TAG, "updateConnectionStatus: Actualizando estado de conexión a $status")
            _connectionStatus.postValue(status)
        } catch (e: Exception) {
            Log.e(TAG, "updateConnectionStatus: Error actualizando estado de conexión", e)
        }
    }

    fun startMonitoring() {
        try {
            if (_isMonitoring.value != true) {
                Log.d(TAG, "startMonitoring: Iniciando monitoreo")
                _isMonitoring.postValue(true)

                viewModelScope.launch {
                    try {
                        repository.startMonitoring { heartRate, steps ->
                            try {
                                updateHeartRate(heartRate)
                                updateSteps(steps)

                                // Crear datos de sueño basados en los valores actuales
                                val currentSleepData = _sleepData.value
                                val updatedSleepData = currentSleepData?.copy(
                                    heartRate = heartRate,
                                    stepCount = steps,
                                    quality = calculateSleepQuality(heartRate, steps, currentSleepData.duration)
                                ) ?: createDefaultSleepData(heartRate, steps)

                                _sleepData.postValue(updatedSleepData)

                                Log.d(TAG, "startMonitoring: Datos actualizados - HR: $heartRate, Steps: $steps")
                            } catch (e: Exception) {
                                Log.e(TAG, "startMonitoring: Error en callback de datos", e)
                                _errorMessage.postValue("Error procesando datos de salud")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "startMonitoring: Error iniciando repositorio", e)
                        _errorMessage.postValue("Error iniciando monitoreo")
                        _isMonitoring.postValue(false)
                    }
                }
            } else {
                Log.w(TAG, "startMonitoring: El monitoreo ya está activo")
            }
        } catch (e: Exception) {
            Log.e(TAG, "startMonitoring: Error al iniciar monitoreo", e)
            _errorMessage.postValue("Error al iniciar monitoreo")
            _isMonitoring.postValue(false)
        }
    }

    fun stopMonitoring() {
        try {
            Log.d(TAG, "stopMonitoring: Deteniendo monitoreo")
            _isMonitoring.postValue(false)

            viewModelScope.launch {
                try {
                    repository.stopMonitoring()
                    Log.d(TAG, "stopMonitoring: Monitoreo detenido exitosamente")
                } catch (e: Exception) {
                    Log.e(TAG, "stopMonitoring: Error deteniendo repositorio", e)
                    _errorMessage.postValue("Error deteniendo monitoreo")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopMonitoring: Error al detener monitoreo", e)
            _errorMessage.postValue("Error al detener monitoreo")
        }
    }

    fun updateSleepData(sleepData: SleepData) {
        try {
            Log.d(TAG, "updateSleepData: Actualizando datos de sueño - Calidad: ${sleepData.quality}%, Duración: ${sleepData.duration}h")
            _sleepData.postValue(sleepData)

            viewModelScope.launch {
                try {
                    repository.saveSleepData(sleepData)
                    Log.d(TAG, "updateSleepData: Datos de sueño guardados en base de datos")
                } catch (e: Exception) {
                    Log.e(TAG, "updateSleepData: Error guardando datos de sueño", e)
                    _errorMessage.postValue("Error guardando datos de sueño")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateSleepData: Error al actualizar datos de sueño", e)
            _errorMessage.postValue("Error actualizando datos de sueño")
        }
    }

    fun getSleepHistory(): LiveData<List<SleepData>> {
        return try {
            repository.getSleepHistory()
        } catch (e: Exception) {
            Log.e(TAG, "getSleepHistory: Error obteniendo historial", e)
            _errorMessage.postValue("Error obteniendo historial de sueño")
            MutableLiveData<List<SleepData>>().apply { value = emptyList() }
        }
    }

    fun calculateSleepQuality(heartRate: Int, steps: Int, duration: Float): Int {
        return try {
            Log.d(TAG, "calculateSleepQuality: Calculando calidad - HR: $heartRate, Steps: $steps, Duration: $duration")

            // Algoritmo mejorado para calcular calidad del sueño
            var quality = 100

            // Factor de ritmo cardíaco (peso: 30%)
            val hrScore = when {
                heartRate in 50..70 -> 100 // Rango ideal durante el sueño
                heartRate in 45..49 || heartRate in 71..80 -> 85
                heartRate in 40..44 || heartRate in 81..90 -> 70
                heartRate in 35..39 || heartRate in 91..100 -> 50
                else -> 30
            } * 0.3f

            // Factor de movimiento/pasos (peso: 25%)
            val stepScore = when {
                steps <= 10 -> 100 // Muy poco movimiento es ideal
                steps <= 25 -> 85
                steps <= 50 -> 70
                steps <= 100 -> 50
                else -> 25
            } * 0.25f

            // Factor de duración (peso: 45%)
            val durationScore = when {
                duration in 7.0f..9.0f -> 100 // Duración ideal
                duration in 6.0f..6.9f || duration in 9.1f..10.0f -> 85
                duration in 5.0f..5.9f || duration in 10.1f..11.0f -> 70
                duration in 4.0f..4.9f || duration in 11.1f..12.0f -> 50
                else -> 25
            } * 0.45f

            val finalQuality = (hrScore + stepScore + durationScore).toInt()

            Log.d(TAG, "calculateSleepQuality: Calidad calculada: $finalQuality (HR: $hrScore, Steps: $stepScore, Duration: $durationScore)")

            finalQuality.coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "calculateSleepQuality: Error al calcular calidad del sueño", e)
            75 // Valor por defecto en caso de error
        }
    }

    private fun createDefaultSleepData(heartRate: Int = 0, steps: Int = 0): SleepData {
        val duration = 7.5f
        return SleepData(
            date = Date(),
            duration = duration,
            quality = calculateSleepQuality(heartRate, steps, duration),
            heartRate = heartRate,
            stepCount = steps,
            deepSleepDuration = duration * 0.25f, // 25% sueño profundo
            lightSleepDuration = duration * 0.55f, // 55% sueño ligero
            remSleepDuration = duration * 0.20f    // 20% sueño REM
        )
    }

    fun resetData() {
        try {
            Log.d(TAG, "resetData: Reiniciando todos los datos")
            _heartRate.postValue(0)
            _stepCount.postValue(0)
            _sleepData.postValue(createDefaultSleepData())
            _connectionStatus.postValue("Desconectado")
            _isMonitoring.postValue(false)
        } catch (e: Exception) {
            Log.e(TAG, "resetData: Error reiniciando datos", e)
        }
    }

    fun getLatestSleepData() {
        viewModelScope.launch {
            try {
                val latestData = repository.getLatestSleepData()
                latestData?.let {
                    _sleepData.postValue(it)
                    Log.d(TAG, "getLatestSleepData: Datos más recientes cargados")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getLatestSleepData: Error obteniendo datos más recientes", e)
                _errorMessage.postValue("Error cargando datos recientes")
            }
        }
    }

    fun deleteSleepData(sleepData: SleepData) {
        viewModelScope.launch {
            try {
                repository.deleteSleepData(sleepData)
                Log.d(TAG, "deleteSleepData: Datos eliminados exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "deleteSleepData: Error eliminando datos", e)
                _errorMessage.postValue("Error eliminando datos")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            Log.d(TAG, "onCleared: Limpiando ViewModel")
            viewModelScope.launch {
                repository.stopMonitoring()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCleared: Error en limpieza", e)
        }
    }
}