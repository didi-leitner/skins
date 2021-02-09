package com.na.didi.skinz.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
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

    abstract val viewModel: BaseViewModel<S, E, I>

    protected val viewIntentChannel = Channel<I>(Channel.CONFLATED)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.v("UUU","onActivityCreated " + viewModel + " " + viewModel.state)

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {

                Log.v("UUUU","collcted state " + it)
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


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.bindViewIntents(viewIntentChannel.receiveAsFlow().filterNotNull())
        }
    }

    abstract fun renderState(state: S)

    abstract fun renderEffect(effect: E)
}