package com.na.didi.skinz.viewmodel


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.util.onEachEvent
import com.na.didi.skinz.view.viewcontract.MyProductsViewContract
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MyProductsViewModel @ViewModelInject internal constructor(
        val productsRepo: ProductsRepo
) : ViewModel() {


    private val state = MutableStateFlow<MyProductsViewState>(MyProductsViewState.Loading())

    init {
        viewModelScope.launch {
            initUploads()
        }
    }


    fun bindIntents(viewContract: MyProductsViewContract) {

        viewContract.initState().onEach {

            state.filterNotNull().collect {
                Log.v("TAGGG","collect state, render " + it)
                viewContract.render(it)
            }
            Log.v("TAGGG","onEach, after collect call")

        }.launchIn(viewModelScope)


        viewContract.onSwipeToRefresh().onEach {
            loadFromNetwork()
        }.launchIn(viewModelScope)

        viewContract.onListEntryClicked().onEachEvent { selectContent ->
            selectContent(selectContent)
        }.launchIn(viewModelScope)

    }

    suspend fun initUploads() {

        Log.v("TAGGG","initUploads")

        state.value = MyProductsViewState.Error()

        try {
            productsRepo.getMyProducts(viewModelScope).collect { pagingData ->
                Log.v("TAGGG","collected data " + pagingData)
                state.value = MyProductsViewState.ProductList(pagingData, null)
            }

            Log.v("TAGGG","after repo collect")
        } catch (exception: Exception) {
            state.value = MyProductsViewState.ProductList(null, exception.localizedMessage!!)
        }

    }

    private fun selectContent(selectContent: MyProductsViewIntent.SelectContent) {
        state.value = MyProductsViewState.OpenContent(selectContent.position, selectContent.content)
    }

    private fun loadFromNetwork() {
        /*uploadsRepository.getMainFeedNetwork(...).onEach { resource ->
            when (resource.status) {
               LOADING -> repository.getMainFeedRoom(...
                    )
                    .onEach { pagedList ->
                    // Pass local data.
                    state.value = FeedViewState.Feed(...)
                }.launchIn(coroutineScope)
               SUCCESS -> resource.data?.collect { pagedList ->
                  // Pass network data.
                  state.value = FeedViewState.Feed(...)
               }
               ERROR -> repository.getMainFeedRoom(...
                    )
                    .onEach { pagedList ->
                    // Pass local data.
                    state.value = FeedViewState.Feed(...)
                }.launchIn(coroutineScope)
            }
        }.launchIn(coroutineScope)*/
    }




}
