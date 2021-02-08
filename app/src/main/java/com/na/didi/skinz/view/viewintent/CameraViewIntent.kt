package com.na.didi.skinz.view.viewintent

import android.content.Context
import android.graphics.Bitmap
import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.data.model.Product


sealed class CameraViewIntent {

    object StartDetecting: CameraViewIntent()

    object OnMovedAwayFromDetectedObject: CameraViewIntent()

    object OnPointCameraToDetectedObject: CameraViewIntent()

    data class OnConfirmedDetectedObject(val detectedObjectInfo: DetectedObjectInfo?): CameraViewIntent()

    data class OnTextDetected(val searchedObject: SearchedObject): CameraViewIntent()

    data class OnProductClickedInBottomSheet(val product: Product): CameraViewIntent()

    data class AddProduct(val context: Context, val bitmap: Bitmap?, val product: Product): CameraViewIntent()

}