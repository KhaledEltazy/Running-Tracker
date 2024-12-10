package com.android.runningtracker.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.runningtracker.R
import com.android.runningtracker.databinding.FragmentTrackingBinding
import com.android.runningtracker.model.Run
import com.android.runningtracker.service.TrackingService
import com.android.runningtracker.service.Polyline
import com.android.runningtracker.ui.viewmodels.MainViewModel
import com.android.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.android.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.android.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.android.runningtracker.util.Constants.MAP_ZOOM
import com.android.runningtracker.util.Constants.POLYLINE_COLOR
import com.android.runningtracker.util.Constants.POLYLINE_WIDTH
import com.android.runningtracker.util.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var binding : FragmentTrackingBinding
    private val viewModel : MainViewModel by viewModels()

    private var map : GoogleMap? = null

    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()

    private var currentTimeMillis = 0L

    private var menu : Menu? = null

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tracking,container,false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //implementation mapView
        binding.mvMap.onCreate(savedInstanceState)
        binding.mvMap.getMapAsync {
            map = it
            addAllPolyLine()
        }

        //handling start button and sending command to Service and starting notification
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        //handling finish btn and save out run
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        subscribeToObserves()
    }

    //observe of isTracking and pathPoint values (TrackingService)
    //adding timer
    private fun subscribeToObserves(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoint.observe(viewLifecycleOwner, Observer {
            pathPoint = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis = it
            var formattedTime = TrackingUtility.getFormattedStopMatchTime(currentTimeMillis,true)
            binding.tvTimer.text = formattedTime
        })
    }

    //sending start or pause service according to isTracking state
    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //handling buttons of start and finish
    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    //setting camera moving
    private fun moveCameraToUser(){
        if(pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    //making suitable zoom to screenshot image of map
    private fun zoomToSeeWholeTrack(){
        val bounds  = LatLngBounds.Builder()
        for(polyline in pathPoint){
            for (pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mvMap.width,
                binding.mvMap.height,
                (binding.mvMap.height * 0.05f).toInt()
            )
        )
    }

    //saving require data of each run to database
    //screen shots,
    private fun endRunAndSaveToDb(){
        map?.snapshot {bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoint){
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyline).toInt()
            }
            val aveSpeed = round((distanceInMeters / 1000f) / (currentTimeMillis/1000f/60/60)*10)/10f
            val dateTimeStamp =java.util.Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters/1000f) * weight).toInt()
            val run = Run(bmp,dateTimeStamp,aveSpeed,distanceInMeters,currentTimeMillis,caloriesBurned)
            viewModel.insert(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    //adding All polyLines to map (with color and width)
    private fun addAllPolyLine(){
        for(polyline in pathPoint){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //checking if pathPoint is empty + getting last and preLast points and adding them to map
    //setting color,with to polyLine
    private fun addLatestPolyline(){
        if(pathPoint.isNotEmpty() && pathPoint.last().size > 1){
            val preLastLasting = pathPoint.last()[pathPoint.last().size -2]
            val lastLatLing = pathPoint.last().last()
            val polylineOptions= PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLasting)
                .add(lastLatLing)
            map?.addPolyline(polylineOptions)
        }
    }

    //sending command service
    private fun sendCommandToService(action :String)=
        Intent(requireContext(), TrackingService::class.java).also { intent ->
            intent.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent) // For API 26 and above
            } else {
                requireContext().startService(intent) // For below API 26
            }
        }



    //adding lifeCycle of mapView for onResume
    override fun onResume() {
        super.onResume()
        binding.mvMap?.onResume()
    }

    //adding lifeCycle of mapView for onStart
    override fun onStart() {
        super.onStart()
        binding.mvMap?.onStart()
    }

    //adding lifeCycle of mapView for onStop
    override fun onStop() {
        super.onStop()
        binding.mvMap?.onStop()
    }

    //adding lifeCycle of mapView for onDestroy
    override fun onDestroy() {
        super.onDestroy()
        binding.mvMap?.onDestroy()
    }

    //adding lifeCycle of mapView for onLowMemory
    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mvMap?.onLowMemory()
    }

    //adding lifeCycle of mapView for onPause
    override fun onPause() {
        super.onPause()
        binding.mvMap?.onPause()
    }

    //adding lifeCycle of mapView for onSaveInstanceState
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mvMap.onSaveInstanceState(outState)
    }

    //Creating menu with single item to cancel run
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    //showing menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking ->
                showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.baseline_delete_forever_24)
            .setPositiveButton("Yes"){a,b ->
                stopRun()
            }
            .setNegativeButton("No"){dialogInterface,a ->
                dialogInterface.cancel()

            }
            .create()
        dialog.show()
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
}