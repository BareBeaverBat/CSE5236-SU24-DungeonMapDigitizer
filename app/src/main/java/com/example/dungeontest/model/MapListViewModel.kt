package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MapListViewModel(application: Application) : AndroidViewModel(application)  {
    private val mapRepository: MapRepository = MapRepository(application)
    val allMaps = mapRepository.allMaps

    // onclick listener

    //todo the business logic for the 'save' button
    // how
    // per Eric- create a State<MaybeSomeEnum> that'll be returned to the map-namer Screen composable
    // map namer screen composable will control what gets rendered vs not (e.g. display of overwrite button) based on that State object
    // but first this launches a coroutine that, when it finishes the suspending function, will update the State object
    //      idea- maybe the MutableState<SomeEnum> object is passed to this method by the screen composable rather than being returned to the composable from this method

    //todo add method to be called by overwrite button?


    //todo add method to insert map

    //todo add method to delete map


    //todo add method to update map entry

    //todo add onclear method to null the maprepository and allMaps references
}