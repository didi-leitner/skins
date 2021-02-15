package com.na.didi.skinz.data.source

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.na.didi.skinz.data.db.dao.ProductsDao
import com.na.didi.skinz.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsLocalDataSourceImpl @Inject constructor(private val productsDao: ProductsDao) :
    ProductsLocalDataSource {

    override suspend fun addProduct(product: Product) {
        productsDao.addProduct(product)

    }

    override fun getProductsPaged(): Flow<PagingData<Product>> {

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DB_PAGE_SIZE),
            pagingSourceFactory = { productsDao.getProductsPaged() }
        ).flow

    }

    companion object {
        private const val DB_PAGE_SIZE = 5
    }
}