package com.android.runningtracker.ui.viewmodels


import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.runningtracker.model.Run
import com.android.runningtracker.repository.MainRepository
import com.android.runningtracker.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    fun insert(run : Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }


    private val runsSortedByDate = mainRepository.getAllRunSortedByDate()

    private val runsSortedByTimeInMillis = mainRepository.getAllRUnSortedByTimeInMillis()

    private val runsSortedByCaloriesBurned = mainRepository.getAllRUnSortedByCaloriesBurned()

    private val runsSortedByDistanceInMeters = mainRepository.getAllRUnSortedByDistanceInMeters()

    private val runsSortedByAveSpeedInKMH = mainRepository.getAllRUnSortedByAveSpeedInKMH()

    //selecting sorting type
    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {

        runs.addSource(runsSortedByDate){result->
            if(sortType == SortType.DATE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByTimeInMillis){result->
            if(sortType == SortType.RUNNING_TIME){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByDistanceInMeters){result->
            if(sortType == SortType.DISTANCE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByAveSpeedInKMH){result->
            if(sortType == SortType.AVE_SPEED){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByCaloriesBurned){result->
            if(sortType == SortType.CALORIES_BURNED){
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRun(sortType : SortType) = when(sortType){
        SortType.DATE ->runsSortedByDate.value?.let { runs.value=it }
        SortType.DISTANCE ->runsSortedByDistanceInMeters.value?.let { runs.value=it }
        SortType.AVE_SPEED ->runsSortedByAveSpeedInKMH.value?.let { runs.value=it }
        SortType.CALORIES_BURNED ->runsSortedByCaloriesBurned.value?.let { runs.value=it }
        SortType.RUNNING_TIME ->runsSortedByTimeInMillis.value?.let { runs.value=it }
    }.also {
        this.sortType = sortType
    }
}