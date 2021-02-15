package com.na.didi.skinz.data.repository

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.network.Resource
import com.na.didi.skinz.data.source.ProductsLocalDataSource
import com.na.didi.skinz.data.source.ProductsRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepo @Inject constructor(
    private val localDataSoruce: ProductsLocalDataSource,
    private val remoteDataSource: ProductsRemoteDataSource
) {

    fun initProductListPaging(scope: CoroutineScope): Flow<PagingData<Product>> {
        return localDataSoruce.getProductsPaged().cachedIn(scope)
    }

    suspend fun insertProduct(product: Product): Resource<Product> {

        try {
            val apiResponse = remoteDataSource.postProduct(product)
            if (apiResponse.isSuccessful()) {

                apiResponse.body?.let {
                    localDataSoruce.addProduct(it)
                    return Resource.Success(apiResponse.body)
                } ?: run {
                    localDataSoruce.addProduct(product)
                    return Resource.Success(product)
                }

            } else {
                return Resource.Error(apiResponse.errorMessage)
            }

        } catch (e: Exception) {
            return Resource.Error(e.localizedMessage)
        }

    }

    fun loadFromNetwork() {


        //remoteDataSource
        //TODO
        try {

        } catch (e: Exception) {

        }
    }


}