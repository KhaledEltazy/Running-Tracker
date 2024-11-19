package com.android.runningtracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.runningtracker.model.Run

@Dao
interface RunDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(run : Run)

    @Delete
    suspend fun delete(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunSortedByDate() : LiveData<List<Run>>


    @Query("SELECT * FROM running_table ORDER BY aveSpeedInKMH DESC")
    fun getAllRunSortedByAveSpeedInKMH() : LiveData<List<Run>>


    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunSortedByDistanceInMeters() : LiveData<List<Run>>


    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunSortedByTimeInMillis() : LiveData<List<Run>>


    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunSortedByCaloriesBurned() : LiveData<List<Run>>

    @Query("DELETE FROM running_table")
    fun deleteAllRunning()

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getAllTimeInMillis() : LiveData<Long>

    @Query("SELECT SUM(timestamp) FROM running_table")
    fun getAllTimestamp() : LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getAllCaloriesBurned() : LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getAllDistanceInMeters() : LiveData<Int>

    @Query("SELECT SUM(aveSpeedInKMH) FROM running_table")
    fun getAllAveSpeedInKMH() : LiveData<Float>
}