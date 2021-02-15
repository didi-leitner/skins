package com.na.didi.skinz.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.data.source.FakeProductsLocalDataSource
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

    //private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeProductsLocalDataSource

    // Class under test
    private lateinit var tasksRepository: ProductsRepo



    @Before
    fun createRepository() {

        val products =
            mutableListOf(Product(5, "Paula's Choice", "Niacinamide 10%", "", 0))

        //tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeProductsLocalDataSource(products)
        // Get a reference to the class under test
        tasksRepository = ProductsRepo(tasksLocalDataSource)
    }

    @Test
    fun insert_product_should_update_pagingData(){




        testCoroutineRule.runBlockingTest {

            //if data inserted, new paging data is received in the flow
            /*val dao = mock<ProductsDao>()
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
            val pagingData = mock<PagingData<Product>>()*/
        }


    }

}