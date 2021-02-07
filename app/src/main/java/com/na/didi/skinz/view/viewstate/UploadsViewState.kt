package com.na.didi.skinz.view.viewstate

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.UploadsModel

sealed class UploadsViewState {

    data class UploadsList(
            val pagingData: PagingData<UploadsModel>?,
            val error: String? = null
    ) : UploadsViewState()



    object Loading: UploadsViewState()

    object Error: UploadsViewState()

}

sealed class UploadsViewEffect {
    data class OpenContent(
            val content: UploadsModel
    ) : UploadsViewEffect()
}