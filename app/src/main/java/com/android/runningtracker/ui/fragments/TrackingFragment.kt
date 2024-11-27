package com.android.runningtracker.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.android.runningtracker.R
import com.android.runningtracker.databinding.FragmentTrackingBinding
import com.android.runningtracker.service.TrackingService
import com.android.runningtracker.ui.viewmodels.MainViewModel
import com.android.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var binding : FragmentTrackingBinding
    private val viewModel : MainViewModel by viewModels()
    private var map : GoogleMap? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tracking,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mvMap.onCreate(savedInstanceState)
        binding.mvMap.getMapAsync {
            map = it
        }

        binding.btnToggleRun.setOnClickListener {
            sedCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun sedCommandToService(action :String)=
        Intent(requireContext(), TrackingService::class.java).also { intent ->
            intent.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent) // For API 26 and above
            } else {
                requireContext().startService(intent) // For below API 26
            }
        }



    override fun onResume() {
        super.onResume()
        binding.mvMap?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mvMap?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mvMap?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mvMap?.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mvMap?.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.mvMap?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mvMap.onSaveInstanceState(outState)
    }


}