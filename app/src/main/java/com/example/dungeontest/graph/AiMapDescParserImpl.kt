package com.example.dungeontest.graph

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph

class AiMapDescParserImpl : AiMapDescParser {
    private val tag = javaClass.simpleName

    override fun parseAiMapDesc(aiOutputJson: String, jsonIngester: JsonAdapter<List<AiRespRoom>>): Graph<MapRoom, DefaultEdge> {

        var parsedResponse: List<AiRespRoom>? = null
        var roomDescriptions: List<AiRespRoom> = listOf()

        try {
            parsedResponse = jsonIngester.fromJson(aiOutputJson)
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