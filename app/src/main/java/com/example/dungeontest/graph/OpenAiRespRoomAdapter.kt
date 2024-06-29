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

        

        //todo consume key "rooms" (error if actual key is not that expected string) and skip its value
        //todo consume key "adjacency_analysis" (error if actual key is not that expected string) and skip its value

        //todo consume key "final_result"
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

        reader.endObject()
        return aiRoomDescriptions
    }


    @ToJson
    override fun toJson(writer: JsonWriter, value: List<AiRespRoom>?) {
        TODO("Not implemented")//not relevant for project
    }

}