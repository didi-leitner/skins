package com.na.didi.skinz.view.viewcontract

import com.na.didi.skinz.util.Event
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface MyProductsViewContract {

    fun initState(): Flow<Boolean>

    fun onSwipeToRefresh(): Flow<Boolean>

    @ExperimentalCoroutinesApi
    fun onListEntryClicked(): Flow<Event<MyProductsViewIntent.SelectContent?>>

    fun render(state: MyProductsViewState)
}