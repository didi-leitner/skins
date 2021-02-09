package com.na.didi.skinz.view.viewintent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.na.didi.skinz.data.model.Product


sealed class CameraViewIntent {

    object StartDetecting: CameraViewIntent()
    data class OnMovedAwayFromDetectedObject(val boundingBox: Rect): CameraViewIntent()
    data class OnConfirmingDetectedObject(val boundingBox: Rect, val progress: Float): CameraViewIntent()
    data class OnConfirmedDetectedObject(val detectedObjectBitmap: Bitmap, val boundingBox: Rect): CameraViewIntent()
    data class OnProductClickedInBottomSheet(val context: Context, val bitmap: Bitmap?, val product: Product): CameraViewIntent()
    data class OnProductsFound(val products: List<Product>): CameraViewIntent()


}

