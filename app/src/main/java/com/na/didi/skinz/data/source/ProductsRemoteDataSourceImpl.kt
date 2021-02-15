package com.na.didi.skinz.data.source

import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.network.ApiResponse
import javax.inject.Singleton

@Singleton
class ProductsRemoteDataSourceImpl: ProductsRemoteDataSource {
    //private val service: ProductsService

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }

    init {
        /*val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            //.addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(ProductsService::class.java)*/
    }

    override suspend fun postProduct(product: Product): ApiResponse<Product> {
        //TODO dummy
        return ApiResponse(200, null, null)
       // return service.postProduct(product)
    }





}