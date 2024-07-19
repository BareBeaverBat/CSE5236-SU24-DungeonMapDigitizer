package com.example.dungeontest.graph

import org.junit.Test
import com.example.dungeontest.readJsonFromResources

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AiMapDescParserImplTest {
    val expectedRoomNames = listOf(
        "Entrance", "Room of Murals", "Chamber with just bones and shattered pottery",
        "Goblin Pen", "Goblin Valuables", "Goblin Pantry",
        "Giant Insects (feeding on fungal growths)", "Insect Queen!", "Insect Larder",
        "Pupation Chamber", "Insect Nursery"
    )
    fun grabRoom(index: Int): MapRoom { return MapRoom(index, expectedRoomNames[index-1])}


    @Test
    fun givenSchemaCompliantJson_whenReadGraphSpecWithoutInconsistencies_thenProduceFullGraph() {
        val aiOutputJson = readJsonFromResources("open_ai_json_output_1.json")
        val graph = AiMapDescParserImpl().parseAiMapDesc(aiOutputJson, OpenAiRespRoomAdapter())

        assertEquals(expectedRoomNames.toSet(), graph.vertexSet().map { it.label }.toSet())
        assertEquals(11, graph.edgeSet().size)
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(2)))
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(3)))
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(7)))
        assertTrue(graph.containsEdge(grabRoom(3), grabRoom(4)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(5)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(6)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(7)))
        assertTrue(graph.containsEdge(grabRoom(7), grabRoom(8)))
        assertTrue(graph.containsEdge(grabRoom(8), grabRoom(9)))
        assertTrue(graph.containsEdge(grabRoom(8), grabRoom(11)))
        assertTrue(graph.containsEdge(grabRoom(10), grabRoom(11)))
    }

    @Test
    fun givenSchemaCompliantJson_whenReadInconsistentGraphSpec_thenProduceGraphWithInconsistenciesRemoved() {
        val aiOutputJson = readJsonFromResources("open_ai_json_output_2.json")
        val graph = AiMapDescParserImpl().parseAiMapDesc(aiOutputJson, OpenAiRespRoomAdapter())

        assertEquals(expectedRoomNames.toSet(), graph.vertexSet().map { it.label }.toSet())
        assertEquals(10, graph.edgeSet().size)
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(2)))
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(3)))
        assertTrue(graph.containsEdge(grabRoom(1), grabRoom(7)))
        assertTrue(graph.containsEdge(grabRoom(3), grabRoom(4)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(5)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(6)))
        assertTrue(graph.containsEdge(grabRoom(4), grabRoom(7)))
        assertTrue(graph.containsEdge(grabRoom(7), grabRoom(8)))
        assertTrue(graph.containsEdge(grabRoom(8), grabRoom(9)))
        assertTrue(graph.containsEdge(grabRoom(10), grabRoom(11)))
    }


}