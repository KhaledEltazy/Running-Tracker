package com.android.runningtracker.adapter

import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.runningtracker.databinding.ItemRunBinding
import com.android.runningtracker.model.Run
import com.android.runningtracker.util.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class RunTrackerAdapter : RecyclerView.Adapter<RunTrackerAdapter.RunTrackerViewHolder>() {
    val diffCallBack = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    var differ = AsyncListDiffer(this,diffCallBack)

    fun submitList(list :List<Run>) = differ.submitList(list)

    inner class RunTrackerViewHolder(val binding : ItemRunBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunTrackerViewHolder {
        return RunTrackerViewHolder(ItemRunBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RunTrackerViewHolder, position: Int) {
        val currentRun = differ.currentList[position]

         Glide.with(holder.itemView).load(currentRun.img).into(holder.binding.ivRunImage)

         val calendar = Calendar.getInstance().apply {
                timeInMillis = currentRun.timestamp
        }

        val dateFormat = SimpleDateFormat("dd/MM/yy",Locale.getDefault())
        holder.binding.tvDate.text = dateFormat.format(calendar.time)

        val aveSpeed = "${currentRun.aveSpeedInKMH} km/h"
        holder.binding.tvAvgSpeed.text = aveSpeed

        val distance = "${currentRun.distanceInMeters /1000f} km"
        holder.binding.tvDistance.text = distance

        holder.binding.tvTime.text = TrackingUtility.getFormattedStopMatchTime(currentRun.timeInMillis)

        val caloriesBurned = "${currentRun.caloriesBurned} kcal"
        holder.binding.tvCalories.text = caloriesBurned
    }
}