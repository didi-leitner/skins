package com.na.didi.skinz.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.na.didi.skinz.data.db.dao.ProductsDao
import com.na.didi.skinz.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepo @Inject constructor(private val productsDao: ProductsDao) {

    suspend fun insertProduct(product: Product) {
        productsDao.addProduct(product)
    }

    fun initProductListPaging(scope: CoroutineScope): Flow<PagingData<Product>> {

        val pagingSource = productsDao.getProductsPaged()

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DB_PAGE_SIZE),
            pagingSourceFactory = { pagingSource }
        ).flow.cachedIn(scope)
    }

    fun loadFromNetwork() {
        //TODO
    }

    companion object {
        private const val DB_PAGE_SIZE = 5
    }
}