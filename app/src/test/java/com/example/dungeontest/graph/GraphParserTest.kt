package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GraphParserTest {

    @Test
    fun givenGraphWasSerializedToDot_whenGraphIsParsedFromDot_thenGraphIsSame() {
        val mapGraph: Graph<MapRoom, DefaultEdge> = DefaultUndirectedGraph(DefaultEdge::class.java)
        val roomNames = listOf("room1", "room2", "room3", "room4", "room5", "room6", "room7")
        val rooms = roomNames.mapIndexed { index, name -> MapRoom(index+1, name) }
        rooms.forEach { mapGraph.addVertex(it) }

        val edgeIndices = listOf(Pair(0,1), Pair(0,5), Pair(1,2), Pair(1,4), Pair(2,6), Pair(4,6), Pair(5,6))
        edgeIndices.forEach { mapGraph.addEdge(rooms[it.first], rooms[it.second]) }

        val dotSerializationOfGraph = GraphParser().graphToDot(mapGraph)
        val reconstructedGraph = GraphParser().dotToGraph(dotSerializationOfGraph)

        assertEquals(roomNames.toSet(), reconstructedGraph.vertexSet().map { it.label }.toSet())

        edgeIndices.forEach {
            assertTrue(reconstructedGraph.containsEdge(rooms[it.first], rooms[it.second]))
        }
    }


}