package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeontest.graph.GraphParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel( application: Application): AndroidViewModel(application) {

    val testDot = "strict digraph G {\n" +
            "  www_google_com [ label=\"http://www.google.com\" ];\n" +
            "  www_wikipedia_org [ label=\"http://www.wikipedia.org\" ];\n" +
            "  www_jgrapht_org [ label=\"http://www.jgrapht.org\" ];\n" +
            "  www_jgrapht_org -> www_wikipedia_org;\n" +
            "  www_google_com -> www_jgrapht_org;\n" +
            "  www_google_com -> www_wikipedia_org;\n" +
            "  www_wikipedia_org -> www_google_com;\n" +
            "}"

    private var _byteArrayOfImage = MutableStateFlow<ByteArray>(ByteArray(0)) // string if svg. rmb to change this
    val byteArrayOfImage: StateFlow<ByteArray> = _byteArrayOfImage

    init {
        val graphParser = GraphParser()
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                graphParser.dotToPng(testDot)
            }
            _byteArrayOfImage.value = result
        }
    }



}