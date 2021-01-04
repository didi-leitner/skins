package com.na.didi.hangerz.view.viewintent

import com.na.didi.hangerz.data.model.UploadsModel
import com.na.didi.hangerz.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class UploadsViewIntent(
        val loadFromNetwork: MutableStateFlow<Boolean?> = MutableStateFlow(null),
        val selectContent: MutableStateFlow<Event<SelectContent?>> = MutableStateFlow(Event(null)),
) {

    class SelectContent(val content: UploadsModel, val position: Int)
}