package com.example.dungeontest.model

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MapViewModel(application: Application) : AndroidViewModel(application)  {
    private val mapRepository: MapRepository = MapRepository(application)
    val allMaps = mapRepository.allMaps

    var transitoryMapRecord by mutableStateOf<MapRecord?>(null)

    suspend fun insertMap(map: MapRecord) : Long {
        return mapRepository.insertMap(map)
    }

    suspend fun deleteMap(map: MapRecord): Int {
        return mapRepository.deleteMap(map)
    }

    suspend fun updateMap(map: MapRecord): Int {
        return mapRepository.updateMap(map)
    }

    suspend fun mapExists(mapName: String) : Boolean {
        val map = mapRepository.getMapIfExists(mapName)
        // do != so that pattern matches if(mapExists(name))
        return map != null
    }

}