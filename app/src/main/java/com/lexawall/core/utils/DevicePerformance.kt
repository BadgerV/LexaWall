package com.lexawall.core.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object DevicePerformance {
    @Volatile
    private var cachedLowEnd: Boolean? = null

    fun isLowEndDevice(context: Context): Boolean {
        cachedLowEnd?.let { return it }
        return synchronized(this) {
            cachedLowEnd ?: computeLowEnd(context.applicationContext).also { cachedLowEnd = it }
        }
    }

    private fun computeLowEnd(context: Context): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.getMemoryInfo(memoryInfo)
        val totalMem = memoryInfo.totalMem
        val isLowMemoryDevice = totalMem in 1 until (3L * 1024L * 1024L * 1024L)
        return isLowMemoryDevice || Build.VERSION.SDK_INT < 28
    }
}
