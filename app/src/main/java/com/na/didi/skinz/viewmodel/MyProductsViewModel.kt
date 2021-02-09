package com.na.didi.skinz.viewmodel


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewEffect
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyProductsViewModel @ViewModelInject internal constructor(
    val productsRepo: ProductsRepo
) : BaseViewModel<MyProductsViewState, MyProductsViewEffect, MyProductsViewIntent>() {

    override suspend fun bindViewIntents(viewIntentFlow: Flow<MyProductsViewIntent>) {
        viewIntentFlow.collect {
            Log.v("TAGGG", "viewIntent received " + it)
            when (it) {
                MyProductsViewIntent.InitView -> {
                    if (_state.value == DEFAULT_STATE) {
                        //do this just once per viewmodelscope, not on every lifecycle ch
                        initProductsPaged()
                    }
                    //else view recreated, viewmmodel still alive, do nothing

                }
                MyProductsViewIntent.SwipeToRefresh -> loadFromNetwork()
                is MyProductsViewIntent.OnListItemClicked -> selectContent(it.product)
            }
        }
    }


    private fun initProductsPaged() {

        //qualitest.


        Log.v("YYY", "iintProductsPaged")
        _state.value = MyProductsViewState.Loading

        viewModelScope.launch {
            try {
                productsRepo.getMyProducts(this).collect { pagingData ->
                    _state.value = MyProductsViewState.ProductList(pagingData, null)
                }

            } catch (exception: Exception) {
                //state.value = MyProductsViewState.Error()
                _state.value = MyProductsViewState.ProductList(null, exception.localizedMessage!!)
            }
        }

        //test viewModelScope withing lifecycleScope -
        //if outer lcs cancelled, will this vms be cancelled? -> no
        /*viewModelScope.launch {
            while(true) {
                Log.v("YYY","dummy " + System.currentTimeMillis())
                delay(1000)
            }
        }*/

    }

    private suspend fun selectContent(prduct: Product) {
        _effect.send(MyProductsViewEffect.OpenContent(0, prduct))
    }

    private fun loadFromNetwork() {
        //TODO repo load fromm netw, insert in db
    }


}
