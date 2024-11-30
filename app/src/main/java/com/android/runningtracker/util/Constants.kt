package com.android.runningtracker.util



object Constants {
    //name of the database
    const val RUNNING_DATABASE_NAME = "Running.db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    //Actions path to TrackingService
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    //Notification channel components
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_CHANNEL_NAME = "tracking"

    //interval time of getting coordinates
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    //color, width and zooming of polyline in map
    const val POLYLINE_COLOR = android.graphics.Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

}