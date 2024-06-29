package com.example.dungeontest.graph

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class OpenAiRespRoomAdapter : AiRespJsonIngester() {
    private val tag = javaClass.simpleName

    @FromJson
    override fun fromJson(reader: JsonReader): List<AiRespRoom> {
        val aiRoomDescriptions: MutableList<AiRespRoom> = mutableListOf();

        if (!reader.hasNext()) {
            Log.w(tag, "empty json string fed to open ai response room adapter's parser")
            return aiRoomDescriptions
        }
        val firstTokenType = reader.peek()
        if (firstTokenType != JsonReader.Token.BEGIN_OBJECT) {
            throw JsonDataException("open ai response room adapter expects a json object at root of json document but instead encountered ${reader.readJsonValue()}")
        }
        reader.beginObject()

        val firstKey = reader.nextName()
        if (firstKey != "rooms") {
            throw JsonDataException("open ai response failed to include 'rooms' segment at start of json object (for preliminary Chain-of-Thought-style analysis)")
        }
        reader.skipValue()//ignore the CoT-style listing of the rooms

        val secondKey = reader.nextName()
        if (secondKey != "adjacency_analysis") {
            throw JsonDataException("open ai response failed to include 'adjacency_analysis' segment in the middle json object (for Chain-of-Thought-style analysis)")
        }
        reader.skipValue()//ignore the CoT-style analysis of the adjacency relationships of the rooms


        val thirdKey = reader.nextName()
        if (thirdKey != "final_result") {
            throw JsonDataException("open ai response's json object failed to have 'final_result' as its last key")
        }

        val firstTokenOfLastValue = reader.peek()
        if (firstTokenOfLastValue != JsonReader.Token.BEGIN_ARRAY) {
            throw JsonDataException("open ai response's json object failed to have a json array as the value for the 'final_result' key; it instead had ${reader.readJsonValue()}")
        }

        reader.beginArray()
        while (reader.hasNext()) {
            val nextTokenType = reader.peek()
            if (nextTokenType == JsonReader.Token.BEGIN_OBJECT) {
                aiRoomDescriptions += parseSingleRoom(reader)
            } else {
                throw JsonDataException("expected beginning of a json object corresponding to a room in the map, but instead encountered: ${reader.readJsonValue()}")
            }
        }
        reader.endArray()

        val lastToken = reader.peek()
        if (lastToken != JsonReader.Token.END_OBJECT) {
            throw JsonDataException("open ai response's json object had extra junk after the value of the 'final_result' key: ${reader.readJsonValue()}")
        }

        reader.endObject()
        return aiRoomDescriptions
    }


    @ToJson
    override fun toJson(writer: JsonWriter, value: List<AiRespRoom>?) {
        TODO("Not implemented")//not relevant for project
    }

}