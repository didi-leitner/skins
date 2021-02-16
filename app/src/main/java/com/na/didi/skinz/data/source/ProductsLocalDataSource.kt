package com.na.didi.skinz.data.source

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsLocalDataSource {

    suspend fun insertProduct(product: Product)

    fun getProductsPaged(): Flow<PagingData<Product>>
}