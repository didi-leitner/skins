package com.na.didi.skinz.view.viewintent

import android.content.Context
import android.graphics.Bitmap
import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.data.model.Product


sealed class CameraXViewIntent {

    //view interractions to be observed
    object OnBottomSheetHidden: CameraXViewIntent()

    object OnNothingFoundInFrame: CameraXViewIntent()

    object OnMovedAwayFromDetectedObject: CameraXViewIntent()

    data class OnConfirmedDetectedObjec(val detectedObjectInfo: DetectedObjectInfo?): CameraXViewIntent()

    object OnConfirmingDetectedObject: CameraXViewIntent()

    data class OnTextDetected(val searchedObject: SearchedObject): CameraXViewIntent()

    data class OnProductClicked(val product: Product): CameraXViewIntent()

    data class AddProduct(val context: Context, val bitmap: Bitmap?, val product: Product): CameraXViewIntent()

}