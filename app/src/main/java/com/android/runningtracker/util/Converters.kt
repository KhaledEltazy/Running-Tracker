package com.android.runningtracker.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap (bmp : Bitmap) : ByteArray?{
        val outPutStream  = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,outPutStream)
        return outPutStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byte : ByteArray) : Bitmap{
        return BitmapFactory.decodeByteArray(byte, 0, byte.size)
    }
}