package com.example.watchsleepmonitor.utils

import android.util.Log
import androidx.health.services.client.data.SampleDataPoint

object HealthDataUtils {

    private const val TAG = "HealthDataUtils"

    /**
     * Convierte de manera segura un valor de DataPoint a Int
     */
    fun extractIntValue(dataPoint: SampleDataPoint<*>): Int {
        return try {
            when (val value = dataPoint.value) {
                is Int -> value
                is Long -> {
                    if (value <= Int.MAX_VALUE) {
                        value.toInt()
                    } else {
                        Log.w(TAG, "Long value too large for Int: $value")
                        Int.MAX_VALUE
                    }
                }
                is Double -> {
                    if (value.isFinite() && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) {
                        value.toInt()
                    } else {
                        Log.w(TAG, "Double value out of Int range: $value")
                        0
                    }
                }
                is Float -> {
                    if (value.isFinite() && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) {
                        value.toInt()
                    } else {
                        Log.w(TAG, "Float value out of Int range: $value")
                        0
                    }
                }
                is Number -> {
                    val doubleValue = value.toDouble()
                    if (doubleValue.isFinite() && doubleValue >= Int.MIN_VALUE && doubleValue <= Int.MAX_VALUE) {
                        doubleValue.toInt()
                    } else {
                        Log.w(TAG, "Number value out of Int range: $doubleValue")
                        0
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown data type for Int conversion: ${value?.javaClass?.simpleName} = $value")
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting value to Int: ${dataPoint.value}", e)
            0
        }
    }

    /**
     * Convierte de manera segura un valor de DataPoint a Double
     */
    fun extractDoubleValue(dataPoint: SampleDataPoint<*>): Double {
        return try {
            when (val value = dataPoint.value) {
                is Double -> value
                is Float -> value.toDouble()
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                is Number -> value.toDouble()
                else -> {
                    Log.w(TAG, "Unknown data type for Double conversion: ${value?.javaClass?.simpleName} = $value")
                    0.0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting value to Double: ${dataPoint.value}", e)
            0.0
        }
    }

    /**
     * Valida que un valor de ritmo cardíaco esté en un rango razonable
     */
    fun isValidHeartRate(heartRate: Int): Boolean {
        return heartRate in 30..220
    }

    /**
     * Valida que un valor de pasos esté en un rango razonable
     */
    fun isValidStepCount(steps: Int): Boolean {
        return steps >= 0 && steps <= 100000 // Máximo razonable de pasos por día
    }
}