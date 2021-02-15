package com.na.didi.skinz.di

import com.na.didi.skinz.data.source.ProductsRemoteDataSource
import com.na.didi.skinz.data.source.ProductsRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@InstallIn(ApplicationComponent::class)
@Module
class NetworkModule {

    @Provides
    fun provideProductsRemoteDataSource(): ProductsRemoteDataSource {
        return ProductsRemoteDataSourceImpl()
    }

}