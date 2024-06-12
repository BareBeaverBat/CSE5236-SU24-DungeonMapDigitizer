package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapRepository(application: Application) {
    private var mapDao: MapDao
    val allMaps: LiveData<List<MapRecord>>

    suspend fun getMapIfExists(mapNam: String): MapRecord? {
        return withContext(Dispatchers.IO) { mapDao.getExistingMapRecordIfExists(mapNam) }
    }

    suspend fun insertMap(map: MapRecord): Long {
        return withContext(Dispatchers.IO) { mapDao.insertMap(map) }
    }

    suspend fun updateMap(map: MapRecord): Int {
        return withContext(Dispatchers.IO) { mapDao.updateMap(map) }
    }

    suspend fun deleteMap(map: MapRecord): Int {
        return withContext(Dispatchers.IO) { mapDao.deleteMap(map) }
    }

    init {
        val db: MapDatabase = MapDatabase.getDatabase(application)
        mapDao = db.mapDao()
        allMaps = mapDao.getAll()
    }

}