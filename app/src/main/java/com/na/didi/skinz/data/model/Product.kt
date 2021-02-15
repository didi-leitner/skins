package com.na.didi.skinz.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_products_table")
data class Product(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Int,

        val title: String,
        val subtitle: String,

        @ColumnInfo(name = "local_url")

        var imagePath: String?,

        val timeAdded: Long
)

