package com.android.runningtracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.runningtracker.model.Run
import com.android.runningtracker.util.Converters

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao() : RunDAO
}