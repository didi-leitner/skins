package com.na.didi.skinz.view.viewstate

import android.graphics.Bitmap
import android.graphics.Rect
import com.na.didi.skinz.data.model.Product

sealed class CameraViewState {

    object Idle: CameraViewState()
    object Detecting : CameraViewState()
    object ProductAdded : CameraViewState()
    data class OnProductsFound(val products: List<Product>): CameraViewState()


}

sealed class CameraViewEffect{

    object OnNothingDetected: CameraViewEffect()
    data class OnMovedAwayFromDetectedObject(val boundingBox: Rect): CameraViewEffect()
    data class OnConfirmingDetectedObject(val boundingBox: Rect, val progress: Float): CameraViewEffect()
    data class OnConfirmedDetectedObject(val detectedObjectBitmap: Bitmap, val boundingBox: Rect): CameraViewEffect()
}