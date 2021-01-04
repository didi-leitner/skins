package com.na.didi.hangerz.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.na.didi.hangerz.data.model.UploadsModel
import com.na.didi.hangerz.data.repository.UploadsRepo

class CameraViewModel @ViewModelInject internal constructor(
        uploadsRepo: UploadsRepo
) : ViewModel() {

    private val uploadsRepository: UploadsRepo = uploadsRepo

    fun addPicToDB(upload: UploadsModel) {
        uploadsRepository.insertUpload(upload)
    }
}