package com.na.didi.skinz.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.na.didi.skinz.data.model.Product

@Dao
interface ProductsDao {

    @Query("SELECT * FROM my_products_table ORDER BY timeAdded DESC")
    fun getProductsPaged(): PagingSource<Int, Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product)
}