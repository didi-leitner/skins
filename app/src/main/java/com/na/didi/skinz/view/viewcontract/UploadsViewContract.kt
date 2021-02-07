package com.na.didi.skinz.view.viewcontract

import kotlinx.coroutines.flow.Flow

interface UploadsViewContract {

    fun initState(): Flow<Boolean>

    fun onSwipeToRefresh(): Flow<Boolean>

    //@ExperimentalCoroutinesApi
    //fun onListEntryClicked(): Flow<Event<UploadsViewIntent.SelectContent?>>

    fun render(state: com.na.didi.skinz.view.viewstate.UploadsViewState)
}