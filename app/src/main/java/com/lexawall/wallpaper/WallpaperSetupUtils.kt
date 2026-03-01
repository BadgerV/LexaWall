package com.lexawall.wallpaper

import android.app.WallpaperManager
import android.content.Context

object WallpaperSetupUtils {
    fun isLexaWallpaperActive(context: Context): Boolean {
        val info = WallpaperManager.getInstance(context).wallpaperInfo ?: return false
        val packageMatches = info.packageName == context.packageName
        val classMatches = info.serviceName == LexaWallWallpaperService::class.java.name ||
            info.serviceName.endsWith(".LexaWallWallpaperService")
        return packageMatches && classMatches
    }
}
