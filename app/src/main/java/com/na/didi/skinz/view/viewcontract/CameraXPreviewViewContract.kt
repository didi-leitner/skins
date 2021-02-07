package com.na.didi.skinz.view.viewcontract

import com.na.didi.skinz.view.viewintent.CameraXViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.flow.Flow

interface CameraXPreviewViewContract {


    fun render(state: CameraViewState)

    fun viewIntentFlow(): Flow<CameraXViewIntent>

}