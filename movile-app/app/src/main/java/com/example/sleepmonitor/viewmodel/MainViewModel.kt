package com.example.sleepmonitor.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmonitor.data.SleepData
import com.example.sleepmonitor.repository.HealthDataRepository
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(private val context: Context) : ViewModel() {

    private val repository = HealthDataRepository(context)

    private val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int> = _heartRate

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private val _connectionStatus = MutableLiveData<String>("Desconectado")
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _sleepData = MutableLiveData<SleepData>()
    val sleepData: LiveData<SleepData> = _sleepData

    private var isMonitoring = false

    fun startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true
            _connectionStatus.value = "Conectado"
            viewModelScope.launch {
                repository.startMonitoring { heartRate, steps ->
                    _heartRate.postValue(heartRate)
                    _stepCount.postValue(steps)
                }
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        _connectionStatus.value = "Desconectado"
        viewModelScope.launch {
            repository.stopMonitoring()
        }
    }

    fun updateSleepData(sleepData: SleepData) {
        _sleepData.value = sleepData
        viewModelScope.launch {
            repository.saveSleepData(sleepData)
        }
    }

    fun getSleepHistory(): LiveData<List<SleepData>> {
        return repository.getSleepHistory()
    }

    fun calculateSleepQuality(heartRate: Int, steps: Int, duration: Float): Int {
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
    }
} 