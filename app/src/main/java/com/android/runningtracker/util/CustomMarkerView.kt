package com.android.runningtracker.util

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.runningtracker.databinding.MarkerViewBinding
import com.android.runningtracker.model.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Locale

class CustomMarkerView(
    val runs : List<Run>,
    c : Context,
    layoutId : Int
    ) : MarkerView(c,layoutId) {
        lateinit var binding : MarkerViewBinding
    @RequiresApi(Build.VERSION_CODES.N)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null){
            return
        }
        val curRunId = e.x.toInt()
        val currentRun = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentRun.timestamp
        }

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        val aveSpeed = "${currentRun.aveSpeedInKMH} km/h"
        binding.tvAveSpeed.text = aveSpeed

        val distance = "${currentRun.distanceInMeters /1000f} km"
        binding.tvDistance.text = distance

        binding.tvDuration.text = TrackingUtility.getFormattedStopMatchTime(currentRun.timeInMillis)

        val caloriesBurned = "${currentRun.caloriesBurned} kcal"
        binding.tvCaloriesBurned.text = caloriesBurned
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())

    }
}