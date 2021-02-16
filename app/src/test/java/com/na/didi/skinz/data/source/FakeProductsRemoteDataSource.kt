package com.na.didi.skinz.data.source

import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.network.ApiResponse

class FakeProductsRemoteDataSource: ProductsRemoteDataSource {

    override suspend fun postProduct(product: Product): ApiResponse<Product> {

        if(product.title.isBlank() || product.subtitle.isBlank()) {
            //simulate  error
            return ApiResponse(401, null, "title or subtitle blank")
        }

        //sim success
        return ApiResponse<Product>(200, product.copy(), null)
    }
}