package com.android.runningtracker.ui.viewmodels



import androidx.lifecycle.ViewModel
import com.android.runningtracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
    val totalTimeRun = mainRepository.getAllTimeInMillis()
    val totalDistance = mainRepository.getAllDistanceInMeters()
    val totalCaloriesBurned = mainRepository.getAllCaloriesBurned()
    val totalAveSpeed = mainRepository.getAllAveSpeedInKMH()

    val runsSortedByDate = mainRepository.getAllRunSortedByDate()

}