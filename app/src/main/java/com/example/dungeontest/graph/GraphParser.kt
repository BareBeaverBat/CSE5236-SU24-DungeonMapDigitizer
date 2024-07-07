package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import java.io.StringWriter

class GraphParser {
    fun graphToDot(graph: Graph<MapRoom, DefaultEdge>): String {
        val exporter = DOTExporter<MapRoom, DefaultEdge> { it.id.toString() }
        exporter.setVertexAttributeProvider {
            mapOf("label" to DefaultAttribute.createAttribute(it.label))
        }
        val writer = StringWriter()

        exporter.exportGraph(graph, writer)
        return writer.toString()
    }

}