package com.example.watchsleepmonitor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchsleepmonitor.service.BluetoothService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class WatchViewModel : ViewModel() {

    private val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int> = _heartRate

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private val _isMonitoring = MutableLiveData<Boolean>(false)
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    private val _connectionStatus = MutableLiveData<String>("Desconectado")
    val connectionStatus: LiveData<String> = _connectionStatus

    private var monitoringJob: kotlinx.coroutines.Job? = null

    fun startMonitoring() {
        if (!_isMonitoring.value!!) {
            _isMonitoring.value = true
            _connectionStatus.value = "Monitoreando"
            
            monitoringJob = viewModelScope.launch {
                while (_isMonitoring.value == true) {
                    // Simular datos de salud del Galaxy Watch
                    val simulatedHeartRate = (60..100).random()
                    val simulatedSteps = (0..10).random()
                    
                    _heartRate.postValue(simulatedHeartRate)
                    _stepCount.postValue(simulatedSteps)
                    
                    delay(2000) // Actualizar cada 2 segundos
                }
            }
        }
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        _connectionStatus.value = "Detenido"
        monitoringJob?.cancel()
    }

    fun sendDataToPhone() {
        viewModelScope.launch {
            try {
                val heartRate = _heartRate.value ?: 0
                val steps = _stepCount.value ?: 0
                
                // Enviar datos al teléfono vía Bluetooth
                val data = "$heartRate,$steps"
                BluetoothService.sendDataToPhone(data)
                
                _connectionStatus.postValue("Datos enviados")
            } catch (e: Exception) {
                _connectionStatus.postValue("Error al enviar")
            }
        }
    }

    fun getCurrentHealthData(): Pair<Int, Int> {
        return Pair(_heartRate.value ?: 0, _stepCount.value ?: 0)
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
} 