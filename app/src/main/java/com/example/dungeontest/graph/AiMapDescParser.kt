package com.example.dungeontest.graph

import com.squareup.moshi.JsonAdapter
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * converts json from AI model to a JGraphT object
 */
interface AiMapDescParser {

    /**
     * todo explain
     */
    fun parseAiMapDesc(aiOutputJson: String, jsonIngester: JsonAdapter<List<AiRespRoom>>): Graph<MapRoom, DefaultEdge>;

}