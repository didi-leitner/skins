package com.na.didi.skinz.view.viewintent

import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.camera.SearchedObject
import com.na.didi.skinz.util.Event
import kotlinx.coroutines.flow.MutableStateFlow


class CameraXViewIntent {

    //view interractions to be observed
    val onBottomSheetHidden: MutableStateFlow<Event<Boolean?>> = MutableStateFlow(Event(null))

    val onNothingFoundInFrame: MutableStateFlow<Event<Boolean?>> = MutableStateFlow(Event(null))

    val onMovedAwayFromDetectedObject: MutableStateFlow<Event<Boolean?>> = MutableStateFlow(Event(null))

    val onConfirmedDetectedObject: MutableStateFlow<DetectedObjectInfo?> = MutableStateFlow(null)

    val onConfirmingDetectedObject: MutableStateFlow<Event<Boolean?>> = MutableStateFlow(Event(null))

    val onTextDetected: MutableStateFlow<SearchedObject?> = MutableStateFlow(null)

}