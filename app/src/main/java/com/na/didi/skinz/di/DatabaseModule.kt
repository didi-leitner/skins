package com.na.didi.skinz.di

import android.content.Context
import com.na.didi.skinz.data.db.AppDatabase
import com.na.didi.skinz.data.db.dao.UploadsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideUploadsDao(appDatabase: AppDatabase): UploadsDao {
        return appDatabase.uploadsDao()
    }

}

