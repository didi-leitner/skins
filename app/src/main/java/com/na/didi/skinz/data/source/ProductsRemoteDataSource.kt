package com.na.didi.skinz.data.source

import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.network.ApiResponse

interface ProductsRemoteDataSource {

    suspend fun postProduct(product: Product): ApiResponse<Product>



}