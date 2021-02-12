package com.na.didi.skinz.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.na.didi.skinz.data.db.dao.ProductsDao
import com.na.didi.skinz.data.db.dao.UploadsDao
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.model.UploadsModel


@Database(entities = [UploadsModel::class, Product::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadsDao(): UploadsDao
    abstract fun productsDao(): ProductsDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "hangerz_database")
                .fallbackToDestructiveMigration()//TODO
                .build()
        }
    }
}

