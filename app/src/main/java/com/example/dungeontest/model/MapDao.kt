package com.example.dungeontest.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MapDao {
    @Query("SELECT * FROM maps")
    fun getAll(): LiveData<List<MapRecord>>

    @Query("SELECT * FROM maps WHERE map_name = :name LIMIT 1")
    fun getExistingMapRecordIfExists(name: String): MapRecord?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMap(map: MapRecord): Long

    @Update
    fun updateMap(map: MapRecord): Int

    @Delete
    fun deleteMap(map: MapRecord): Int
}