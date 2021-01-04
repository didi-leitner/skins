package com.na.didi.hangerz.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.na.didi.hangerz.data.model.UploadsModel

@Dao
interface UploadsDao {

    @Query("SELECT * FROM uploads_table")
    fun getUploadsPaged(): PagingSource<Int, UploadsModel>

    @Insert
    //TODO suspended fun ?
    fun addUpload(uploadsModel: UploadsModel)

}