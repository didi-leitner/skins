package com.na.didi.skinz.view.viewstate

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product

sealed class MyProductsViewState {

    data class ProductList(
            val pagingData: PagingData<Product>?,
            val error: String? = null
    ) : MyProductsViewState()

    data class OpenContent(
            val position: Int,
            val content: Product
    ) : MyProductsViewState()

    class Loading(): MyProductsViewState()

    class Error(): MyProductsViewState()

}