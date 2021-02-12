package com.na.didi.skinz.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.na.didi.skinz.data.model.UploadsModel

@Dao
interface UploadsDao {

    @Query("SELECT * FROM uploads_table")
    fun getUploadsPaged(): PagingSource<Int, UploadsModel>

    @Insert
    suspend fun addUpload(uploadsModel: UploadsModel)

}