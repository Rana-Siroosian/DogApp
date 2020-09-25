package com.example.android.dogs.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DogBreed::class], version = 1)
abstract class DogDatabase : RoomDatabase() {

    abstract fun dogDao(): DogDao

    companion object {

        @Volatile
        private var instance: DogDatabase? = null
        private val LOCK = Any()

        /**Whenever we invoke the DogDatabase we will either get an instance if it has already been
         * created or if not we're gonna synchronized which means if multiple threads are trying to
         * access this block of code then only one will be able to access, so we will either return
         * the instance we will build the database, assign it to instance and then return it.
         */
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }


        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, DogDatabase::class.java,
            "dogdatabase"
        ).build()
    }

}