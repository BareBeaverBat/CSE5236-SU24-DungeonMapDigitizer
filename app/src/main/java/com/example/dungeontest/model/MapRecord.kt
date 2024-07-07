package com.example.dungeontest.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maps")
data class MapRecord(
    @PrimaryKey @ColumnInfo(name = "map_name") val mapName: String,
    @ColumnInfo(name = "picture_file_name") val pictureFileName: String,
    @ColumnInfo(name = "dot_string") var dotString: String?,
)
