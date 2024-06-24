package com.example.dungeontest

fun Any.readJsonFromResources(fileName: String): String {
    val classLoader = javaClass.classLoader ?: throw Error("somehow trying to call this extension function (to read a json file $fileName from resources folder) from a kotlin context that doesn't have a java class with class loader")
    val resourceAsStream = classLoader.getResourceAsStream(fileName) ?: throw Exception("test file $fileName not found")
    return resourceAsStream.bufferedReader().use { it.readText() }
}