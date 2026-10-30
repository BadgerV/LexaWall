package com.lexawall.wallpaper

import android.app.WallpaperManager
import android.content.Context
import com.lexawall.utils.LexaWallLogger

object WallpaperSetupUtils {
    fun isLexaWallpaperActive(context: Context): Boolean {
        val info = WallpaperManager.getInstance(context).wallpaperInfo
        if (info == null) {
            LexaWallLogger.d("LexaWallSetup", "wallpaperInfo is null")
            return false
        }
        val expected = LexaWallWallpaperService::class.java.name
        val serviceName = info.serviceName.orEmpty()
        val packageName = info.packageName.orEmpty()

        val isActive = serviceName == expected ||
            serviceName.endsWith(".LexaWallWallpaperService") ||
            serviceName.contains("LexaWallWallpaperService") ||
            (packageName == context.packageName && serviceName.isNotBlank())
        LexaWallLogger.d(
            "LexaWallSetup",
            "isActive=$isActive pkg=$packageName service=$serviceName expected=$expected"
        )
        return isActive
    }

    fun currentComponentDebug(context: Context): String {
        val info = WallpaperManager.getInstance(context).wallpaperInfo ?: return "null"
        return "pkg=${info.packageName.orEmpty()} service=${info.serviceName.orEmpty()}"
    }
}
