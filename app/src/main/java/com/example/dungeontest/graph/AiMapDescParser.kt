package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * converts json from AI model to a JGraphT object
 */
interface AiMapDescParser {

    public fun parseAiMapDesc(aiOutputJson: String): Graph<MapRoom, DefaultEdge>;

}