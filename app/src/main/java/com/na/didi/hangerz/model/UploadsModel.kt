package com.na.didi.hangerz.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploads_table")
data class UploadsModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "local_url")
    val localImageUrl: String,

    val created: Long,

    val rating: Int){




}