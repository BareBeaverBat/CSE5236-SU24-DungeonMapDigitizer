package com.example.dungeontest.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MapDao {
    @Query("SELECT * FROM maps")
    fun getAll(): List<Map>

    @Query("SELECT * FROM maps WHERE map_name = :name LIMIT 1")
    fun getExistingMapRecordIfExists(name: String): Map?//todo this may need to be wrapped in LiveData?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMap(map: Map): Long

    @Update
    fun updateMap(map: Map): Int

    @Delete
    fun deleteMap(map: Map): Int
}