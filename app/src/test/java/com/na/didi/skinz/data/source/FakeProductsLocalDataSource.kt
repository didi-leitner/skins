package com.na.didi.skinz.data.source

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeProductsLocalDataSource(var products: MutableList<Product>) : ProductsLocalDataSource {

    private val _pagingDataFlow = MutableStateFlow(PagingData.from(products))
    private val pagingDataFlow: Flow<PagingData<Product>> = _pagingDataFlow.filterNotNull()

    override suspend fun insertProduct(product: Product) {
        //simmulate immutable
        val newProducts = mutableListOf<Product>()
        newProducts.addAll(products)
        newProducts.add(product)

        products = newProducts

        _pagingDataFlow.value = PagingData.from(newProducts)
    }

    override fun getProductsPaged(): Flow<PagingData<Product>> {
        return pagingDataFlow
    }
}