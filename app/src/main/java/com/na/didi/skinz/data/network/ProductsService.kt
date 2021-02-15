package com.na.didi.skinz.data.network

import com.na.didi.skinz.data.model.Product
import retrofit2.http.Body
import retrofit2.http.POST

interface ProductsService {

    @POST
    suspend fun postProduct(@Body product: Product): ApiResponse<Product>

}
