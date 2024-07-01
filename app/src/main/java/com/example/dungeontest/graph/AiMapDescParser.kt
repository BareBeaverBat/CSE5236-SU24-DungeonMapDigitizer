package com.example.dungeontest.graph

import com.squareup.moshi.JsonAdapter
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * converts json from AI model to a JGraphT object
 */
interface AiMapDescParser {

    /**
     * takes the json document string produced by an LMM/VLM and turns it into a well-behaved
     * graph data structure
     * @param aiOutputJson the json document string produced by an LMM/VLM based on a picture of a
     *          hand-drawn map
     * @param jsonIngester something that can parse the json string into a representation of a
     *          intermediate level of abstraction
     * @return a well-behaved graph data structure describing the hand-drawn map
     */
    fun parseAiMapDesc(aiOutputJson: String, jsonIngester: JsonAdapter<List<AiRespRoom>>): Graph<MapRoom, DefaultEdge>;

}