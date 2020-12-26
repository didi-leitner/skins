package com.na.didi.hangerz.ui.viewintent

import com.na.didi.hangerz.model.UploadsModel
import com.na.didi.hangerz.ui.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class UploadsViewIntent(
        val loadFromNetwork: MutableStateFlow<Boolean?> = MutableStateFlow(null),
        val selectContent: MutableStateFlow<Event<SelectContent?>> = MutableStateFlow(Event(null)),
) {

    class SelectContent(val content: UploadsModel, val position: Int)
}