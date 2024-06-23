package com.example.dungeontest.graph

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import java.lang.reflect.Type

class OpenAiResponseParser : AiResponseParser {

    override fun parseAiResp(aiOutputJson: String): Graph<String, DefaultEdge> {
        val moshi = Moshi.Builder().add(AiRespRoomAdapter()).add(KotlinJsonAdapterFactory()).build()

        val type: Type = Types.newParameterizedType(List::class.java, AiRespRoom::class.java)
        val adapter: JsonAdapter<List<AiRespRoom>> = moshi.adapter(type)
        var parsedResponse: List<AiRespRoom>? = null
        var roomDescriptions: List<AiRespRoom> = listOf()

        try {
            parsedResponse = adapter.fromJson(aiOutputJson)
        } catch (e: Throwable) {
            throw JsonDataException(
                "exception when parsing OpenAI response as a list of room objects",
                e
            )
        }
        if (parsedResponse != null) {
            roomDescriptions = parsedResponse
        } else {
            throw JsonDataException("parsing of OpenAI response resulted in null")
        }

        val mapGraph: Graph<String, DefaultEdge> = DefaultUndirectedGraph(DefaultEdge::class.java)

        //todo validate edges between rooms in response
        //todo add nodes to graph
        //todo add edges to graph

        return mapGraph
        //todo add unit test class for this and try a few things (e.g. GPT-4o response)
    }

}