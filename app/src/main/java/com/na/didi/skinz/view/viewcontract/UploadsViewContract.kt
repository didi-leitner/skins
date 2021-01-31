package com.na.didi.skinz.view.viewcontract

import com.na.didi.skinz.util.Event
import com.na.didi.skinz.view.viewintent.UploadsViewIntent
import com.na.didi.skinz.view.viewstate.UploadsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface UploadsViewContract {

    fun initState(): Flow<Boolean>

    fun onSwipeToRefresh(): Flow<Boolean>

    @ExperimentalCoroutinesApi
    fun onListEntryClicked(): Flow<Event<UploadsViewIntent.SelectContent?>>

    fun render(state: UploadsViewState)
}