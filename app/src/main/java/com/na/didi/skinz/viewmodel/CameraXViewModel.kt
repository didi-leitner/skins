package com.na.didi.skinz.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.data.repository.UploadsRepo
import com.na.didi.skinz.util.onEachEvent
import com.na.didi.skinz.view.viewcontract.CameraXPreviewViewContract
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class CameraXViewModel @ViewModelInject internal constructor(
        uploadsRepo: UploadsRepo
) : ViewModel() {

    private val uploadsRepository: UploadsRepo = uploadsRepo
    private val cameraViewState = MutableStateFlow<CameraViewState>(CameraViewState.Idle())

    var isCameraLive = false
        private set

    private var confirmedObject: DetectedObjectInfo? = null


    fun bindViewIntents(viewContract: CameraXPreviewViewContract) {

        viewContract.initState().onEach {

            cameraViewState.filterNotNull().collect {
                Log.v("TAGGG", "collect state, render " + it)
                viewContract.render(it)

                if (it != CameraViewState.Confirmed()
                        //it != CameraViewState.Searching()
                       // it != CameraViewState.Searched()
                ) {
                    confirmedObject = null
                }
            }
            Log.v("TAGGG", "onEach, after collect call")

        }.launchIn(viewModelScope)

        viewContract.onBottomSheetHidden().onEachEvent {
            cameraViewState.value = CameraViewState.Detecting()
        }.launchIn(viewModelScope)


        //TODO sep cameraContract?

        viewContract.onMovedAwayFromDetectedObject().onEachEvent {
            cameraViewState.value = CameraViewState.Detected()
        }.launchIn(viewModelScope)

        viewContract.onConfirmedDetectedObjectWithCameraHold().onEach {
            confirmedObject = it
            cameraViewState.value = CameraViewState.Searching(it)

        }.launchIn(viewModelScope)

        viewContract.onConfirmingDetectedObject().onEachEvent {
            cameraViewState.value = CameraViewState.Confirming()
        }.launchIn(viewModelScope)

        viewContract.onNothingFoundInFrame().onEachEvent {
            cameraViewState.value = CameraViewState.Detecting()
        }.launchIn(viewModelScope)

        viewContract.onTextDetected().onEach {
            onSearchCompleted(it)

        }.launchIn(viewModelScope)

    }


    /*fun triggerTextDetection(detectedObjectInfo: DetectedObjectInfo) {

        val textProcessor = TextRecognitionProcessor()
        textProcessor.processBitmap(detectedObjectInfo.getBitmap(), null)
        //withContext()
        Log.v("TAGGG", "triggerSearch")
        //TODO text-processor
        //detectedObjectInfo.getBitmap();
        //search text.
        //another thread -> run text recogn on this bitmap


        //uploadsRepository!!.search(detectedObjectInfo) { detectedObject, products ->
        //    onSearchCompleted(detectedObject, products)
        //}


    }*/

    fun markCameraLive() {
        isCameraLive = true
        cameraViewState.value = CameraViewState.Detecting()

    }

    fun markCameraFrozen() {
        isCameraLive = false
        cameraViewState.value = CameraViewState.Idle()

    }

    fun onSearchCompleted(searchedObject: SearchedObject?) {

        Log.v("Check", "onSearchCompleted - ")
        if(searchedObject != null)
            cameraViewState.value = CameraViewState.Searched(searchedObject)
    //. CameraViewState.SEARCHED(lConfirmedObject, products)
    }




}

