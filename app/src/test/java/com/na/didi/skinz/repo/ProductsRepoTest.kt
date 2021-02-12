package com.na.didi.skinz.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.na.didi.skinz.data.db.dao.ProductsDao
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import util.MainCoroutineScopeRule

class ProductsRepoTest {
    @get:Rule
    val testCoroutineRule = MainCoroutineScopeRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insert_product_should_update_pagingData(){
        testCoroutineRule.runBlockingTest {

            //if data inserted, new paging data is received in the flow
            val dao = mock<ProductsDao>()
            val productsRepo = ProductsRepo(dao)
            val product = mock<Product>()

            val pd = mock<PagingData<Product>>()
            val mockPagingData = flowOf(pd)

            whenever(productsRepo.initProductListPaging(mock<CoroutineScope>())).thenReturn(mockPagingData)


            val stateObserver = mock<Observer<PagingData<Product>>>()
            productsRepo.initProductListPaging(mock<CoroutineScope>())
                .asLiveData().observeForever(stateObserver)


            productsRepo.insertProduct(product)


            val captor = ArgumentCaptor.forClass(PagingData::class.java)
            captor.run {
                verify(stateObserver, times(1)).onChanged(capture() as PagingData<Product>?)

            }

            val pagingSource = mock<PagingSource<Int, Product>>()
            val pagingData = mock<PagingData<Product>>()
        }


    }

}