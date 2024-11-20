package com.android.runningtracker.repository

import com.android.runningtracker.db.RunDAO
import com.android.runningtracker.model.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO
) {
    suspend fun insertRun(run : Run) = runDAO.insert(run)

    suspend fun deleteRun (run : Run) = runDAO.delete(run)

    fun deleteAllRunning () = runDAO.deleteAllRunning()

    fun getAllRunSortedByDate() = runDAO.getAllRunSortedByDate()

    fun getAllRUnSortedByTimeInMillis() = runDAO.getAllRunSortedByTimeInMillis()

    fun getAllRUnSortedByCaloriesBurned() = runDAO.getAllRunSortedByCaloriesBurned()

    fun getAllRUnSortedByDistanceInMeters() = runDAO.getAllRunSortedByDistanceInMeters()

    fun getAllRUnSortedByAveSpeedInKMH() = runDAO.getAllRunSortedByAveSpeedInKMH()

    fun getAllDates() = runDAO.getAllTimestamp()

    fun getAllCaloriesBurned() = runDAO.getAllCaloriesBurned()

    fun getAllTimeInMillis() = runDAO.getAllTimeInMillis()

    fun getAllAveSpeedInKMH() = runDAO.getAllAveSpeedInKMH()

    fun getAllDistanceInMeters() = runDAO.getAllDistanceInMeters()

}