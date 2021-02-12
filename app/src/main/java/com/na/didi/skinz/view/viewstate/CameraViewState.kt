package com.na.didi.skinz.view.viewstate

import android.graphics.Bitmap
import android.graphics.Rect
import com.na.didi.skinz.data.model.Product


sealed class CameraState {
    object Idle : CameraState()
    //think about
    //workaround - need this as a class, not a singleton object - conf change
    class Detecting : CameraState()
}

data class ViewState(val cameraState: CameraState, val productResult: ProductsResult)

//TODO move this somewhere else
data class ProductsResult(val products: List<Product>, val bitmap: Bitmap?, val boundingBox: Rect?)


sealed class CameraViewEffect {

    object OnNothingDetected : CameraViewEffect()
    data class OnConfirmingDetectedObject(val boundingBox: Rect, val progress: Float) :
        CameraViewEffect()

    data class OnObjectPicked(val boundingBox: Rect) :
        CameraViewEffect()

    data class OnProductAdded(val product: Product) : CameraViewEffect()

}