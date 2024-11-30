package com.android.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.runningtracker.R
import com.android.runningtracker.ui.MainActivity
import com.android.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.android.runningtracker.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.android.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.android.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.android.runningtracker.util.Constants.FASTEST_LOCATION_INTERVAL
import com.android.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.android.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.android.runningtracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.android.runningtracker.util.Constants.NOTIFICATION_ID
import com.android.runningtracker.util.Constants.TIMER_UPDATE_INTERVAL
import com.android.runningtracker.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias polyLine = MutableList<LatLng>
typealias polyLines = MutableList<polyLine>

class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    //implementing timer variables
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    private val timeRunInSecond = MutableLiveData<Long>()

    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<polyLines>()
    }

    //timerFunction
    private fun startTimer(){
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //time difference between current time and started one.
                lapTime = System.currentTimeMillis() - timeStarted
                //post the new lapTime
                timeRunInSecond.postValue(timeRun + lapTime)
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSecond.postValue(timeRunInSecond.value!! +1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    //implementing initial values of isTracking & pathPoint
    private fun postInitialValues (){
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
        timeRunInSecond.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)


        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    //implementing every Action start-resume, pause and stop
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE ->{
                        pauseService()
                }
                ACTION_STOP_SERVICE ->{
                    Timber.d("Stop service")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    //change the value of isTracking and isTimerEnabled
    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    //checking isTracking -setting interval time - updatingLocationTracking
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    //location callback implementation -anonymousClass
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let { locations->
                    for(location in locations){
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    //post values of latitude and altitude
    private fun addPathPoint(location : Location?){
        location?.let {
            val pos = LatLng(location.latitude,location.altitude)
            pathPoint.value?.apply {
                last().add(pos)
                pathPoint.postValue(this)
            }
        }
    }

    //initial polyline
    private fun addEmptyPolyLine() = pathPoint.value?.apply {
        add(mutableListOf())
        pathPoint.postValue(this)
    } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    //start ForeGround notification service
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Creation notification channel only for API 26+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManger)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running Tracker")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }


    //moving to trackingFragment when click on notification
    //pendingIntent
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,0,
        Intent(this,MainActivity::class.java).also{
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, flags
    )


    //notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManger : NotificationManager){
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
        notificationManger.createNotificationChannel(channel)
    }
}