package com.na.didi.skinz.viewmodel

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<STATE, EFFECT, INTENT>: ViewModel() {

    //alt. MutableStateFlow(DEFAULT_STATE), liveData without default value.
    protected var _state: MutableLiveData<STATE> = MutableLiveData<STATE>()
    var state: Flow<STATE> = _state.asFlow()

    protected var _effect = Channel<EFFECT>(Channel.BUFFERED)
    var effect: Flow<EFFECT> = _effect.receiveAsFlow()

    abstract fun bindViewIntents(lifecycleScope: LifecycleCoroutineScope, viewItents: Flow<INTENT>)


}