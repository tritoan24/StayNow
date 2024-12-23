package com.ph32395.staynow_datn.hieunt.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ph32395.staynow_datn.hieunt.database.dao.NotificationDao
import com.ph32395.staynow_datn.hieunt.model.NotificationModel

@Database(
    entities = [NotificationModel::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    companion object {
        private const val DB_NAME = "database.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun notificationDao(): NotificationDao

}