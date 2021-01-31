package com.na.didi.skinz.camera

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import com.na.didi.skinz.R
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.utils.Utils

/** Hosts the detected object info and its search result.  */
class SearchedObject(
        private val detectedObject: DetectedObjectInfo,
        val productList: List<Product>
) {

    private var objectThumbnail: Bitmap? = null

    val boundingBox: Rect
        get() = detectedObject.boundingBox

    @Synchronized
    fun getObjectThumbnail(resources: Resources): Bitmap = objectThumbnail ?: let {

        val objectThumbnailCornerRadius: Int = resources.getDimensionPixelOffset(R.dimen.bounding_box_corner_radius)

        Utils.getCornerRoundedBitmap(detectedObject.getBitmap(), objectThumbnailCornerRadius)
                .also { objectThumbnail = it }
    }
}

