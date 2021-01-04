package com.na.didi.hangerz.view.viewstate

import androidx.paging.PagingData
import com.na.didi.hangerz.data.model.UploadsModel

sealed class UploadsViewState {

    data class UploadsList(
            val pagingData: PagingData<UploadsModel>?,
            val error: String? = null
    ) : UploadsViewState()

    data class OpenContent(
            val position: Int,
            val content: UploadsModel
    ) : UploadsViewState()

    class Loading(): UploadsViewState()

    class Error(): UploadsViewState()

}