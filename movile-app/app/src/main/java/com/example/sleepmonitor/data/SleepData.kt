package com.example.sleepmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val duration: Float, // hours
    val quality: Int, // percentage
    val heartRate: Int,
    val stepCount: Int,
    val deepSleepDuration: Float,
    val lightSleepDuration: Float,
    val remSleepDuration: Float
) 