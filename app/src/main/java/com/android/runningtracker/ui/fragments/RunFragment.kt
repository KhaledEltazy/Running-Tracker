package com.android.runningtracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.runningtracker.R
import com.android.runningtracker.adapter.RunTrackerAdapter
import com.android.runningtracker.databinding.FragmentRunBinding
import com.android.runningtracker.ui.viewmodels.MainViewModel
import com.android.runningtracker.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.android.runningtracker.util.SortType
import com.android.runningtracker.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@Suppress("DEPRECATION")
@AndroidEntryPoint
class RunFragment : Fragment() , EasyPermissions.PermissionCallbacks {
    private lateinit var binding : FragmentRunBinding

    private val viewModel : MainViewModel by viewModels()

    private lateinit var runAdapter : RunTrackerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_run,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestLocationPermissions()
        setupRecyclerView()

        //check the value of sortType to select suitable selection from spinner
        when(viewModel.sortType){
            SortType.DATE->binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME ->binding.spFilter.setSelection(1)
            SortType.DISTANCE ->binding.spFilter.setSelection(2)
            SortType.AVE_SPEED->binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED->binding.spFilter.setSelection(4)
        }

        //implementing selection on spinner menu
        binding.spFilter.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adaperView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos){
                    0 ->viewModel.sortRun(SortType.DATE)
                    1 ->viewModel.sortRun(SortType.RUNNING_TIME)
                    2 ->viewModel.sortRun(SortType.DISTANCE)
                    3 ->viewModel.sortRun(SortType.AVE_SPEED)
                    4 ->viewModel.sortRun(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //observe desired sorted menu
        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        binding.fabAdding.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }


    }

    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun requestLocationPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(requireActivity(), "Permission granted", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
         AppSettingsDialog.Builder(this).build().show()
     } else {
         requestLocationPermissions()
     }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int
        , permissions: Array<String>
        , grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setupRecyclerView(){
        binding.rvRun.apply {
            runAdapter = RunTrackerAdapter()
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}