package com.na.didi.hangerz.ui.viewcontract

import com.na.didi.hangerz.ui.util.Event
import com.na.didi.hangerz.ui.viewintent.UploadsViewIntent
import com.na.didi.hangerz.ui.viewstate.UploadsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface UploadsViewContract {
    /**
     * Intent to load the current feed state
     *
     * @return A flow that inits the current feed state
     */
    fun initState(): Flow<Boolean>

    /**
     * Intent to load the feed from the network
     *
     * @return A flow that inits loading the feed state from the network
     */
    fun loadFromNetwork(): Flow<Boolean>

    /**
     * Intent to select content from the feed
     *
     * @return A flow that emits the content to select from the feed
     */
    @ExperimentalCoroutinesApi
    fun selectContent(): Flow<Event<UploadsViewIntent.SelectContent?>>

    /**
     * Renders the feed view state
     *
     * @param state The current view state display
     */
    fun render(state: UploadsViewState)
}