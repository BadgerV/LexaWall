package com.lexawall.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CustomImageManagerTest {

    @Test
    fun resizeTargetDimensions_returnsNullWhenImageAlreadyWithinLimit() {
        assertNull(CustomImageManager.resizeTargetDimensions(sourceWidth = 1080, sourceHeight = 1350))
    }

    @Test
    fun resizeTargetDimensions_scalesDownLongestEdgeToLimit() {
        val resized = CustomImageManager.resizeTargetDimensions(sourceWidth = 4000, sourceHeight = 3000)
        assertNotNull(resized)
        assertEquals(1920, resized?.first)
        assertEquals(1440, resized?.second)
    }

    @Test
    fun calculateInSampleSize_returnsPowerOfTwoReduction() {
        val sample = CustomImageManager.calculateInSampleSize(
            sourceWidth = 4000,
            sourceHeight = 3000,
            reqWidth = 1000,
            reqHeight = 1000
        )
        assertEquals(2, sample)
    }

    @Test
    fun decodeForWallpaper_returnsNullWhenFileMissing() {
        val bitmap = CustomImageManager.decodeForWallpaper(
            path = "/definitely/missing/path/image.jpg",
            reqWidth = 1080,
            reqHeight = 1920
        )
        assertNull(bitmap)
    }

    @Test
    fun compressionQualitySelection_findsQualityUnderTwoMbEquivalent() {
        val maxBytes = 2 * 1024 * 1024
        val selected = CustomImageManager.compressQualityForLimit(maxBytes) { quality ->
            // Deterministic fake byte estimator for unit testing quality stepping.
            quality * 30_000
        }
        assertNotNull(selected)
        assertEquals(65, selected)
    }
}
