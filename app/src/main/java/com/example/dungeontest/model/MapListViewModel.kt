package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MapListViewModel(application: Application) : AndroidViewModel(application)  {
    private val mapRepository: MapRepository = MapRepository(application)
    val allMaps = mapRepository.allMaps

    //todo add method to insert map
    suspend fun insertMap(map: MapRecord) : Long {
        return mapRepository.insertMap(map)
    }

    suspend fun deleteMap(map: MapRecord): Int {
        return mapRepository.deleteMap(map)
    }

    //todo add method to update map entry
    suspend fun updateMap(map: MapRecord): Int {
        return mapRepository.updateMap(map)
    }

    //todo add method to get a map with given name if it already exists
    suspend fun mapExists(mapName: String) : Boolean {
        val map = mapRepository.getMapIfExists(mapName)
        // do != so that pattern matches if(mapExists(name))
        return map != null
    }

}