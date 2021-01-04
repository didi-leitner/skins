package com.na.didi.hangerz.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.na.didi.hangerz.data.db.dao.UploadsDao
import com.na.didi.hangerz.data.model.UploadsModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadsRepo @Inject constructor(private val uploadsDao: UploadsDao) {

    fun getUploads(): Flow<PagingData<UploadsModel>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DB_PAGE_SIZE),
            pagingSourceFactory = { uploadsDao.getUploadsPaged() }
        ).flow
    }

    fun insertUpload(upload : UploadsModel) {
        uploadsDao.addUpload(upload)
    }

    companion object {
        private const val DB_PAGE_SIZE = 10
    }



}