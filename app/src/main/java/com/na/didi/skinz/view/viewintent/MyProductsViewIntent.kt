package com.na.didi.skinz.view.viewintent

import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class MyProductsViewIntent() {

    //view interractions
    val swipeToRefresh: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val onListItemClicked: MutableStateFlow<Event<SelectContent?>> = MutableStateFlow(Event(null))

    class SelectContent(val content: Product, val position: Int)
}