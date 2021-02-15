package com.na.didi.skinz.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewEffect
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import util.MainCoroutineScopeRule

class MyProductsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = MainCoroutineScopeRule()

    @Captor
    lateinit var stateCaptor: ArgumentCaptor<MyProductsViewState>

    @Captor
    lateinit var effectCaptor: ArgumentCaptor<MyProductsViewEffect>

    lateinit var productsrepo: ProductsRepo
    lateinit var viewModel: MyProductsViewModel


    @Before
    fun init() {
        stateCaptor = ArgumentCaptor.forClass(MyProductsViewState::class.java)
        effectCaptor = ArgumentCaptor.forClass(MyProductsViewEffect::class.java)
        productsrepo = mock<ProductsRepo>()
        viewModel = MyProductsViewModel(productsrepo)

    }


    @Test
    fun InitView_intent_should_update_state_to_loading_then_productList() {

        testCoroutineRule.runBlockingTest {

            val mockViewIntentFlow = flowOf(MyProductsViewIntent.InitView)

            val pagingData = mock<PagingData<Product>>()
            val mockPagingData = flowOf(pagingData)
            whenever(productsrepo.initProductListPaging(any())).thenReturn(mockPagingData)

            val stateObserver = mock<Observer<MyProductsViewState>>()
            viewModel.state.asLiveData().observeForever(stateObserver)

            viewModel.bindViewIntents(mockViewIntentFlow)

            stateCaptor.run {
                verify(stateObserver, times(2)).onChanged(capture())
                assert(allValues[0] is MyProductsViewState.Loading)
                assert(allValues[1] == MyProductsViewState.ProductList(pagingData,null))
            }
        }
    }

    @Test
    fun InitView_intent_two_times_should_init_paging_once() {

        testCoroutineRule.runBlockingTest {

            val mockViewIntentFlow = flowOf(MyProductsViewIntent.InitView,
                MyProductsViewIntent.InitView)

            val pagingData = mock<PagingData<Product>>()
            val mockPagingData = flowOf(pagingData)
            whenever(productsrepo.initProductListPaging(any())).thenReturn(mockPagingData)

            val stateObserver = mock<Observer<MyProductsViewState>>()
            viewModel.state.asLiveData().observeForever(stateObserver)

            viewModel.bindViewIntents(mockViewIntentFlow)

            verify(productsrepo, times(1)).initProductListPaging(any())
        }
    }

    @Test
    fun ItemClicked_intent_should_fire_OpenContent_effect() {
        testCoroutineRule.runBlockingTest {

            val product = mock<Product>()
            val mockViewIntentFlow = flowOf(MyProductsViewIntent.OnListItemClicked(product))

            val effectObserver = mock<Observer<MyProductsViewEffect>>()
            viewModel.effect.asLiveData().observeForever(effectObserver)

            viewModel.bindViewIntents(mockViewIntentFlow)

            effectCaptor.run {
                verify(effectObserver).onChanged(capture())
                assert(allValues[0] == MyProductsViewEffect.OpenContent(product))
            }
        }
    }

    @Test
    fun SwipeToRefresh_intent_should_call_repos_LoadFromNetwork() {

        testCoroutineRule.runBlockingTest {
            val mockViewIntentFlow = flowOf(MyProductsViewIntent.SwipeToRefresh)

            viewModel.bindViewIntents(mockViewIntentFlow)

            verify(productsrepo, times(1)).loadFromNetwork()
        }

    }


}