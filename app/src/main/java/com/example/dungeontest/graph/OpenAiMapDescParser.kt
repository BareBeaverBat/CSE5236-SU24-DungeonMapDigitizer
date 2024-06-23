package com.example.dungeontest.graph

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import java.lang.reflect.Type

class OpenAiMapDescParser : AiMapDescParser {
    private val tag = OpenAiMapDescParser::class.java.simpleName

    override fun parseAiMapDesc(aiOutputJson: String): Graph<MapRoom, DefaultEdge> {
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

        val mapGraph: Graph<MapRoom, DefaultEdge> = DefaultUndirectedGraph(DefaultEdge::class.java)

        val mapRooms = roomDescriptions.map { MapRoom(it.id, it.label) }
        mapRooms.forEach { mapGraph.addVertex(it) }
        val idsToRooms = mapRooms.associateBy(MapRoom::id) { it }

        val idsToNeighbors = cleanGraphDesc(roomDescriptions)
        idsToNeighbors.forEach { (currId, neighborIds) ->
            neighborIds.forEach { mapGraph.addEdge(idsToRooms[currId], idsToRooms[it]) }
        }

        return mapGraph
        //todo add unit test class for this and try a few things (e.g. GPT-4o response)
    }


    private fun cleanGraphDesc(rooms: List<AiRespRoom>): Map<Int, List<Int>> {
        val validatedEdgesDesc =
            rooms.associateBy(AiRespRoom::id) { it.neighbors.toMutableList() }.toMutableMap()

        val ids = HashSet(rooms.map { it.id })
        rooms.forEach {
            val roomDesc = it
            roomDesc.neighbors.forEach {
                if (!ids.contains(it)) {
                    Log.i(
                        tag,
                        "room $roomDesc contains an invalid neighbor id $it which is not found in the set of room ids $ids; discarding that neighbor reference"
                    )
                    val badEdgeRemoved = validatedEdgesDesc[roomDesc.id]!!.remove(it)
                    if (!badEdgeRemoved) {
                        Log.w(
                            tag,
                            "tried to remove invalid edge reference $it from the validated-edges for room $roomDesc, but that invalid edge reference was already removed from the validated edges for that room"
                        )
                    }
                }
            }
        }

        //force the ai's output to respect our current assumption that the graph is undirected
        for (roomDesc in rooms) {
            if (validatedEdgesDesc.contains(roomDesc.id)) {
                val fixedNeighborsOfCurrentRoom = validatedEdgesDesc[roomDesc.id]!!.filter {
                    validatedEdgesDesc[it]!!.contains(roomDesc.id)
                }
                validatedEdgesDesc[roomDesc.id] = fixedNeighborsOfCurrentRoom.toMutableList()
            } else {
                Log.w(
                    tag,
                    "inexplicable malfunction- $roomDesc was used when creating the validated edges map local variable, and yet somehow that room's id is not a valid key for that map"
                )
            }
        }

        //ensure the data structure returned by this is deeply immutable
        return validatedEdgesDesc.mapValues { it.value.toList() }.toMap()
    }

}