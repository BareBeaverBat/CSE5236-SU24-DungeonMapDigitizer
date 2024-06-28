package com.example.dungeontest.graph

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.dot.DOTExporter
import java.io.StringWriter

class GraphParser {
//    fun graphToPng(graph: Graph<MapRoom, DefaultEdge>): File {
//        val dot = graphToDot(graph)
//        return dotToPng(dot)
//    }
    private fun graphToDot(graph: Graph<MapRoom, DefaultEdge>): String {
        val exporter = DOTExporter<MapRoom, DefaultEdge>()
        val writer = StringWriter()

        return exporter.exportGraph(graph, writer).toString()
    }


    fun dotToPng(dot: String): ByteArray {
        val client = OkHttpClient()

        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("quickchart.io")
            .addPathSegment("graphviz")
            .addQueryParameter("graph", dot)
            .addQueryParameter("format", "png")
            .addQueryParameter("width", "500")
            .addQueryParameter("height", "500")
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.bytes() ?: throw Exception("response body null")
            } else {
                throw Exception("request failed. Code: ${response.code}")
            }
        }

    }



}