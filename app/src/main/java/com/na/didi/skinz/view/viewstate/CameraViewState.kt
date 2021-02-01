package com.na.didi.skinz.view.viewstate

import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.data.model.Product

/*enum class CameraViewState {
    NOT_STARTED,
    DETECTING,
    DETECTED,
    CONFIRMING,
    CONFIRMED,
    SEARCHING,
    SEARCHED(string:String)
}*/

sealed class CameraViewState {

    class Idle(): CameraViewState()
    class Detecting() : CameraViewState()
    class Detected() : CameraViewState()
    class Confirming(): CameraViewState()
    class Confirmed(): CameraViewState()
    data class Searching(val detectedObjectInfo: DetectedObjectInfo): CameraViewState()
    data class Searched(val searchedObject: SearchedObject) : CameraViewState()
    data class SearchedProductConfirmed(val product: Product) : CameraViewState()
    class ProductAdded() : CameraViewState()


}