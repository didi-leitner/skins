package com.na.didi.skinz.view.viewstate

import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product

sealed class MyProductsViewState {

    object Loading : MyProductsViewState()
    data class ProductList(val pagingData: PagingData<Product>?, val error: String? = null) : MyProductsViewState()
    object Error : MyProductsViewState()

}

sealed class MyProductsViewEffect {

    data class OpenContent(val position: Int, val content: Product) : MyProductsViewEffect()

}