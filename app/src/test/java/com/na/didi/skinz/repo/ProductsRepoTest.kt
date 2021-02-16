package com.na.didi.skinz.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.network.Resource
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.data.source.FakeProductsLocalDataSource
import com.na.didi.skinz.data.source.FakeProductsRemoteDataSource
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import util.MainCoroutineScopeRule

class ProductsRepoTest {
    @get:Rule
    val testCoroutineRule = MainCoroutineScopeRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var productsRemoteDataSource: FakeProductsRemoteDataSource
    private lateinit var productsLocalDataSource: FakeProductsLocalDataSource

    private lateinit var productsRepo: ProductsRepo


    @Before
    fun createRepository() {

        val products =
            mutableListOf(Product(5, "Paula's Choice", "Niacinamide 10%", "", 0))

        productsRemoteDataSource = spy(FakeProductsRemoteDataSource())
        productsLocalDataSource = spy(FakeProductsLocalDataSource(products))

        productsRepo = ProductsRepo(productsLocalDataSource, productsRemoteDataSource)
    }

    @Test
    fun observe_products_paging_will_get_from_local_and_update_pagingData() {
        testCoroutineRule.runBlockingTest {

            val stateObserver = mock<Observer<PagingData<Product>>>()
            productsRepo.getProductsPagingDataFlow().asLiveData()
                .observeForever(stateObserver)

            verify(productsLocalDataSource, times(1)).getProductsPaged()

            argumentCaptor<PagingData<Product>>()
                .run {
                    verify(stateObserver, times(1)).onChanged(capture())
                }
        }
    }

    @Test
    fun add_product_success_should_post_and_insert_and_update_pagingData() {


        testCoroutineRule.runBlockingTest {

            val stateObserver = mock<Observer<PagingData<Product>>>()
            productsRepo.getProductsPagingDataFlow().asLiveData()
                .observeForever(stateObserver)


            val product = Product(6, "Paula's Choice", "Vit C Booster", "", 0)
            val res = productsRepo.addProduct(product)

            assert(res is Resource.Success)
            verify(productsRemoteDataSource, times(1)).postProduct(product)
            verify(productsLocalDataSource, times(1)).insertProduct(any())

            val captor = argumentCaptor<PagingData<Product>>()
            captor.run {
                verify(stateObserver, times(2)).onChanged(capture())
                //no useful apis to test PagingData, reiterate in the future
                // allValues[1].filterSync {
                //     it.equals(product)
                //} .equals(PagingData.from(emptyList()))//listOf(product)))
                //)

            }
        }

    }

    @Test
    fun add_product_error() {

        testCoroutineRule.runBlockingTest {

            val product = Product(8, "", "", "", 0)
            val res = productsRepo.addProduct(product)
            assert(res is Resource.Error)


        }

    }

}