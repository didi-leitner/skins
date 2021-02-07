package com.na.didi.skinz.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.UploadsModel
import com.na.didi.skinz.data.repository.UploadsRepo
import com.na.didi.skinz.view.viewintent.UploadsViewIntent
import com.na.didi.skinz.view.viewstate.UploadsViewEffect
import com.na.didi.skinz.view.viewstate.UploadsViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class UploadsViewModel @ViewModelInject internal constructor(
        val uploadsRepository: UploadsRepo
) : BaseViewModel<UploadsViewState, UploadsViewEffect, UploadsViewIntent>() {


    init {
        viewModelScope.launch {
            initUploads(this)
        }
    }


    override fun bindViewIntents(coroutineScope: LifecycleCoroutineScope, viewIntentFlow: Flow<UploadsViewIntent>) {
        coroutineScope.launchWhenStarted {
            viewIntentFlow.collect {
                Log.v("TAGGG", "viewIntent received " + it)
                when (it) {
                    UploadsViewIntent.SwipeToRefresh -> loadFromNetwork()
                    is UploadsViewIntent.SelectContent -> selectContent(it.content)

                }
            }
        }
    }

    private suspend fun initUploads(coroutineScope: CoroutineScope) {

        Log.v("TAGGG", "initUploads")

        _state.value = UploadsViewState.Error

        try {
            uploadsRepository.getUploads(coroutineScope).collect { pagingData ->
                Log.v("TAGGG", "collected data " + pagingData)
                _state.value = UploadsViewState.UploadsList(pagingData, null)
            }

            Log.v("TAGGG", "after repo collect")
        } catch (exception: Exception) {
            _state.value = UploadsViewState.UploadsList(null, exception.localizedMessage!!)
        }

    }


    private fun loadFromNetwork() {
       //TODO
    }

    private fun selectContent(upload: UploadsModel) {
        viewModelScope.launch {
            _effect.send(UploadsViewEffect.OpenContent(upload))
        }
    }


}
