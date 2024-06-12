package com.example.dungeontest.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//todo rename data class to MapRecord to avoid namespace collision with the Map interface (i.e. dictionary concept)
@Entity(tableName = "maps")
data class Map(
    @PrimaryKey @ColumnInfo(name = "map_name") val mapName: String,
    @ColumnInfo(name = "picture_file_name") val pictureFileName: String,
)
