package com.na.didi.hangerz.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.na.didi.hangerz.db.dao.UploadsDao
import com.na.didi.hangerz.model.UploadsModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadsRepo @Inject constructor(private val uploadsDao: UploadsDao) {


    fun getUploads(): Flow<PagingData<UploadsModel>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DB_PAGE_SIZE),
            pagingSourceFactory = { uploadsDao.getUploadsPaged() }
        ).flow


    }

    companion object {
        private const val DB_PAGE_SIZE = 10
    }


}