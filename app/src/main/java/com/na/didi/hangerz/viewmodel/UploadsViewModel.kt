package com.na.didi.hangerz.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.na.didi.hangerz.model.UploadsModel
import com.na.didi.hangerz.repo.UploadsRepo
import kotlinx.coroutines.flow.Flow

class UploadsViewModel @ViewModelInject internal constructor(
uploadsRepository: UploadsRepo
) : ViewModel() {

   val uploads: Flow<PagingData<UploadsModel>> =
       uploadsRepository.getUploads().cachedIn(viewModelScope)
}
