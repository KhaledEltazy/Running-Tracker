package com.android.runningtracker.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.runningtracker.R
import com.android.runningtracker.ui.MainActivity
import com.android.runningtracker.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.android.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //injecting FusedLocationProviderClient
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app:Context
    ) = FusedLocationProviderClient(app)

    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    //moving to trackingFragment when click on notification
    //pendingIntent
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app : Context
    ) = PendingIntent.getActivity(
        app,0,
        Intent(app, MainActivity::class.java).also{
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, flags
    )

    //Building Notification and Inject it inside Tracking Service
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL_ID)
    .setAutoCancel(false)
    .setOngoing(true)
    .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
    .setContentTitle("Running Tracker")
    .setContentText("00:00:00")
    .setContentIntent(pendingIntent)
    .setPriority(NotificationCompat.PRIORITY_LOW)



}