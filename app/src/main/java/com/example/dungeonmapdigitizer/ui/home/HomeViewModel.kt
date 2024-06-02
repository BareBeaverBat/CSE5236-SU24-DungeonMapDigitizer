package com.example.dungeonmapdigitizer.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _maps = MutableLiveData<List<MapData>>()
    val maps: LiveData<List<MapData>> = _maps

    data class MapData(
        val index: Int,
        val name: String
    )

    private fun loadDummyData() {
        _maps.value = listOf(
            MapData(1, "Dungeon Alpha"),
            MapData(2, "Dungeon Beta"),
            MapData(3, "Dungeon Gamma")
        )
    }
    init {
        loadDummyData()
    }




}