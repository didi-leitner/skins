package com.na.didi.skinz.data.source

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProductsLocalDataSource(val products: MutableList<Product>) : ProductsLocalDataSource {

    override suspend fun addProduct(product: Product) {
        products.add(product)
        //emit Pd.from(products)
    }

    override fun getProductsPaged(): Flow<PagingData<Product>> {
        return flowOf(PagingData.from(products))
    }
}