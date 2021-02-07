package com.na.didi.skinz.view.viewintent

import com.na.didi.skinz.data.model.Product

sealed class MyProductsViewIntent() {

    object SwipeToRefresh: MyProductsViewIntent()
    data class OnListItemClicked(val product: Product): MyProductsViewIntent()
}