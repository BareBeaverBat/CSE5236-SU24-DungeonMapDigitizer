package com.example.dungeontest.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Map::class], version = 1)
abstract class MapDatabase: RoomDatabase() {
    abstract fun mapDao(): MapDao

    companion object {
        @Volatile
        private var INSTANCE: MapDatabase? = null

        fun getDatabase(context: Context): MapDatabase {
            // if the INSTANCE is not null, return that existing instance;
            // if it is null, create the database object and store/return it
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapDatabase::class.java,
                    "map_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

    }
}