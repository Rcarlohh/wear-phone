package com.example.sleepmonitor.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sleepmonitor.data.SleepData

@Dao
interface SleepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepData: SleepData)

    @Query("SELECT * FROM sleep_data ORDER BY date DESC")
    fun getAllSleepData(): LiveData<List<SleepData>>

    @Query("SELECT * FROM sleep_data ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSleepData(): SleepData?

    @Query("SELECT * FROM sleep_data WHERE date >= :startDate ORDER BY date DESC")
    fun getSleepDataFromDate(startDate: java.util.Date): LiveData<List<SleepData>>

    @Delete
    suspend fun delete(sleepData: SleepData)

    @Query("DELETE FROM sleep_data")
    suspend fun deleteAll()
} 