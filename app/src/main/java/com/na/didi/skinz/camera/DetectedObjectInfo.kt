package com.na.didi.skinz.camera

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.objects.DetectedObject

/**
 * Holds the detected object and its related image info.
 */
class DetectedObjectInfo(
        private val detectedObject: DetectedObject,
        private val originalBitmap: Bitmap
) {

    private var bitmap: Bitmap? = null

    val boundingBox: Rect = detectedObject.boundingBox

    @Synchronized
    fun getBitmap(): Bitmap {
        return bitmap ?: let {
            val boundingBox = detectedObject.boundingBox

            val createdBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    boundingBox.left,
                    boundingBox.top,
                    boundingBox.width(),
                    boundingBox.height()
            )
            if (createdBitmap.width > MAX_IMAGE_WIDTH) {
                val dstHeight = (MAX_IMAGE_WIDTH.toFloat() / createdBitmap.width * createdBitmap.height).toInt()
                bitmap = Bitmap.createScaledBitmap(createdBitmap, MAX_IMAGE_WIDTH, dstHeight, /* filter= */ false)
            }
            createdBitmap
        }
    }

    companion object {
        private const val TAG = "DetectedObject"
        private const val MAX_IMAGE_WIDTH = 640

    }
}

