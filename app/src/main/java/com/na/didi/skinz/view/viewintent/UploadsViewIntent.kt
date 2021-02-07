package com.na.didi.skinz.view.viewintent

import com.na.didi.skinz.data.model.UploadsModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
sealed class UploadsViewIntent() {

    object SwipeToRefresh: UploadsViewIntent()
    data class SelectContent(val content: UploadsModel) : UploadsViewIntent()
}