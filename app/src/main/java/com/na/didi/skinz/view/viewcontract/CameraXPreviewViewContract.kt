package com.na.didi.skinz.view.viewcontract

import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.util.Event
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface CameraXPreviewViewContract {

    fun initState(): Flow<Boolean>

    fun render(state: CameraViewState)

    @ExperimentalCoroutinesApi
    fun onBottomSheetHidden(): Flow<Event<Boolean?>>

    fun onMovedAwayFromDetectedObject(): Flow<Event<Boolean?>>

    fun onConfirmedDetectedObjectWithCameraHold(): Flow<DetectedObjectInfo>

    fun onConfirmingDetectedObject(): Flow<Event<Boolean?>>

    fun onNothingFoundInFrame(): Flow<Event<Boolean?>>

    fun onTextDetected(): Flow<SearchedObject?>

    fun onProductConfirmedWithClick(): Flow<Product>
}