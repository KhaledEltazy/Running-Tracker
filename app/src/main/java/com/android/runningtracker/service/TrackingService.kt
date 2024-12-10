package com.android.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import com.android.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient



    //implementing timer variables
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    private val timeRunInSecond = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var curNotificationBuilder : NotificationCompat.Builder

    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<Polylines>()
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
                timeRunInMillis.postValue(timeRun + lapTime)
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
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)


        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
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
                   killService()
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

    //stopping service
    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    //updating Notification with timer and buttons
    @SuppressLint("NotificationPermission")
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.baseline_pause_24, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
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
    @SuppressLint("NotificationPermission")
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Creation notification channel only for API 26+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManger)
        }


        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunInSecond.observe(this, Observer {
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopMatchTime(it * 1000L))
                notificationManger.notify(NOTIFICATION_ID, notification.build())
            }
        })

    }



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