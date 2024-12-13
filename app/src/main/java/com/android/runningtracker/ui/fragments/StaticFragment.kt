package com.android.runningtracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.android.runningtracker.R
import com.android.runningtracker.databinding.FragmentStaticBinding
import com.android.runningtracker.ui.viewmodels.StatisticsViewModel
import com.android.runningtracker.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StaticFragment : Fragment() {
    private lateinit var binding : FragmentStaticBinding
    private val viewModel : StatisticsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_static,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopMatchTime(it)
                binding.tvTimer.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it/1000f
                val totalDistance = round(km*10f)/10f
                val totalDistanceString = "${totalDistance} km"
                binding.tvDistance.text = totalDistanceString
            }
        })
        viewModel.totalAveSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalAveSpeed = round(it*10f)/10f
                val aveSpeed = "${totalAveSpeed} km/h"
                binding.tvAveSpeed.text = aveSpeed
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCal = "${it} kcal"
                binding.tvCalories.text = totalCal
            }
        })
    }

}