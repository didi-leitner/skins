package com.na.didi.skinz.view.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.na.didi.skinz.viewmodel.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow

/*
* Base class for a Fragment to enforce the MVI architecture implementation
*
* */
abstract class BaseFragmentMVI<S, E, I> : Fragment() {

    init {
        Log.v("YYY","init block base  frag " + this)
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {

                it?.let {
                    renderState(it)

                }
            }
        }

        //alt way
        /*viewModel.state.onEach {
            renderState(it)
        }.launchIn(lifecycleScope)*/

        lifecycleScope.launchWhenStarted {
            viewModel.effect.collect {
                renderEffect(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.bindViewIntents(viewIntentChannel.receiveAsFlow().filterNotNull())
        }
    }
    abstract val viewModel: BaseViewModel<S, E, I>

    protected val viewIntentChannel = Channel<I>(Channel.CONFLATED)

    abstract fun renderState(state: S)

    abstract fun renderEffect(effect: E)
}