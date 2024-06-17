package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MapListViewModel(application: Application) : AndroidViewModel(application)  {
    private val mapRepository: MapRepository = MapRepository(application)
    val allMaps = mapRepository.allMaps

    //todo add method to get a map with given name if it already exists
    //todo add method to insert map

    suspend fun deleteMap(map: MapRecord): Int {
        return mapRepository.deleteMap(map)
    }

    //todo add method to update map entry


}