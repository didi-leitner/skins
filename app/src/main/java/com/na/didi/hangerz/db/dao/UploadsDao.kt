package com.na.didi.hangerz.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.na.didi.hangerz.model.UploadsModel

@Dao
interface UploadsDao {

    @Query("SELECT * FROM uploads_table")
    fun getUploadsPaged(): PagingSource<Int, UploadsModel>



}