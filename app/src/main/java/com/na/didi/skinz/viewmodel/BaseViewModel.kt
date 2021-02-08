package com.na.didi.skinz.viewmodel

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<STATE, EFFECT, INTENT>: ViewModel() {

    //can't use MutableLiveData (construct without default value)
    //because and livedata.asFlow won't provide a 'StateFlow' just a Flow
    //protected var _state: MutableLiveData<STATE> = MutableLiveData<STATE>()
    protected var _state = MutableStateFlow<STATE?>(null)//defaultState)
    var state: Flow<STATE> = _state.filterNotNull()

    protected var _effect = Channel<EFFECT>(Channel.BUFFERED)
    var effect: Flow<EFFECT> = _effect.receiveAsFlow()

    abstract fun bindViewIntents(lifecycleScope: LifecycleCoroutineScope, viewItents: Flow<INTENT>)


}