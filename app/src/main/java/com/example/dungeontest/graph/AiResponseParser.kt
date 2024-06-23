package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * converts json from AI model to a JGraphT object
 */
interface AiResponseParser {

    public fun parseAiResp(aiOutputJson: String): Graph<String, DefaultEdge>;

}