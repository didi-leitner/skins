package com.na.didi.hangerz.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.na.didi.hangerz.data.db.dao.UploadsDao
import com.na.didi.hangerz.data.model.UploadsModel


@Database(entities = [UploadsModel::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadsDao(): UploadsDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "hangerz_database")
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Log.v("TAGGG","calbback room " + db.version)
                                    //val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                                    //WorkManager.getInstance(context).enqueue(request)
                                }
                            }
                    )
                    .fallbackToDestructiveMigration()//TODO
                    .build()
        }
    }
}

