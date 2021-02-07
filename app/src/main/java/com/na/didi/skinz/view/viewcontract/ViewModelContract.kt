package com.na.didi.skinz.view.viewcontract

/**
 * Internal Contract to be implemented by ViewModel
 * Required to intercept and log ViewEvents
 */
interface ViewModelContract<EVENT> {

    fun processIntent(viewEVENT: EVENT)
}