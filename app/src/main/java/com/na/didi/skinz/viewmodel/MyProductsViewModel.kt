package com.na.didi.skinz.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewEffect
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyProductsViewModel @ViewModelInject internal constructor(
    val productsRepo: ProductsRepo,
) : BaseViewModel<MyProductsViewState, MyProductsViewEffect, MyProductsViewIntent>() {


    override suspend fun bindViewIntents(viewIntentFlow: Flow<MyProductsViewIntent>) {

        viewIntentFlow.collect {
            when (it) {
                MyProductsViewIntent.InitView -> {
                    if (_state.value == DEFAULT_STATE) {
                        initProductsPaged()
                    }
                }
                MyProductsViewIntent.SwipeToRefresh -> loadFromNetwork()
                is MyProductsViewIntent.OnListItemClicked -> selectContent(it.product)
            }
        }
    }


    private fun initProductsPaged() {

        _state.value = MyProductsViewState.Loading

        viewModelScope.launch {
            try {
                productsRepo.getProductsPagingDataFlow().cachedIn(this)
                    .collectLatest { pagingData ->
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
        _effect.send(MyProductsViewEffect.OpenContent(prduct))
    }

    private fun loadFromNetwork() {
        productsRepo.loadFromNetwork()
    }


}
