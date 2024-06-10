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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMap(map: Map): Long

    @Update
    fun updateMap(map: Map): Int

    @Delete
    fun deleteMap(map: Map): Int
}