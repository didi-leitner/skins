package com.na.didi.skinz.viewmodel


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewEffect
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyProductsViewModel @ViewModelInject internal constructor(
        val productsRepo: ProductsRepo
) : BaseViewModel<MyProductsViewState, MyProductsViewEffect, MyProductsViewIntent>() {

    init {
        viewModelScope.launch {
            initProductsPaged(this)

        }
    }

    override fun bindViewIntents(coroutineScope: LifecycleCoroutineScope, viewIntentFlow: Flow<MyProductsViewIntent>) {
        //need lifecycle scope here, since this method is called on conf change
        coroutineScope.launchWhenStarted {
            viewIntentFlow.collect {
                Log.v("TAGGG", "viewIntent received " + it)
                when (it) {
                    MyProductsViewIntent.SwipeToRefresh -> loadFromNetwork()
                    is MyProductsViewIntent.OnListItemClicked -> selectContent(it.product)
                }
            }
        }
    }


    private suspend fun initProductsPaged(coroutineScope: CoroutineScope) {

        try {
            productsRepo.getMyProducts(coroutineScope).collect { pagingData ->
                _state.value = MyProductsViewState.ProductList(pagingData, null)
            }

        } catch (exception: Exception) {
            //state.value = MyProductsViewState.Error()
            _state.value = MyProductsViewState.ProductList(null, exception.localizedMessage!!)
        }
    }

    private fun selectContent(prduct: Product) {
        viewModelScope.launch {
            _effect.send(MyProductsViewEffect.OpenContent(0, prduct))
        }
    }

    private fun loadFromNetwork() {
        //TODO repo load fromm netw, insert in db
    }

}
