package com.android.runningtracker.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.runningtracker.model.Run
import com.android.runningtracker.repository.MainRepository
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

}