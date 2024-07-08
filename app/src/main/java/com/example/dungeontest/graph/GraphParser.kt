package com.example.dungeontest.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.nio.dot.DOTImporter
import java.io.StringReader
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

    fun dotToGraph(dotString: String): Graph<MapRoom, DefaultEdge> {
        val importer = DOTImporter<MapRoom, DefaultEdge>()
        importer.setVertexWithAttributesFactory { idStr, attribs ->
            val newVertexId = Integer.parseInt(idStr)
            MapRoom(newVertexId, attribs.get("label")?.value ?: "ERROR in deserialization, vertex didn't have a label")
        }

        val strReader = StringReader(dotString)

        val graph = DefaultUndirectedGraph<MapRoom, DefaultEdge>(DefaultEdge::class.java)
        importer.importGraph(graph, strReader)
        return graph
    }

}