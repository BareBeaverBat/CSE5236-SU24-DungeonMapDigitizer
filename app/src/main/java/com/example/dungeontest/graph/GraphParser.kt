package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.dot.DOTExporter
import java.io.StringWriter

class GraphParser {
    fun graphToDot(graph: Graph<MapRoom, DefaultEdge>): String {
        val exporter = DOTExporter<MapRoom, DefaultEdge>()
        val writer = StringWriter()

        return exporter.exportGraph(graph, writer).toString()
    }

}