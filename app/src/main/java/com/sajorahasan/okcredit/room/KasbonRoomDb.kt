package com.sajorahasan.okcredit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sajorahasan.okcredit.model.Customer
import com.sajorahasan.okcredit.model.User
import com.sajorahasan.okcredit.room.converter.CustomerConverter
import com.sajorahasan.okcredit.room.converter.TransConverter
import com.sajorahasan.okcredit.room.dao.UserDao

@Database(entities = [User::class, Customer::class], version = 1, exportSchema = false)
@TypeConverters(value = [CustomerConverter::class, TransConverter::class])
abstract class KasbonRoomDb : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: KasbonRoomDb? = null

        fun getDatabase(context: Context): KasbonRoomDb {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasbonRoomDb::class.java,
                    "kasbonDb"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}