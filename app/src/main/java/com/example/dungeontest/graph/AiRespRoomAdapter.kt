package com.example.dungeontest.graph

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class AiRespRoomAdapter : JsonAdapter<List<AiRespRoom>>() {
    private val tag = javaClass.simpleName

    private val badValMsgPrefix =
        "When parsing the json object for a Room in the dungeon map's json, the value for the key"
    private val missingKeyMsgPrefix =
        "The json object for a Room in the dungeon map's json was missing the key"

    @FromJson
    override fun fromJson(reader: JsonReader): List<AiRespRoom> {
        val aiRoomDescriptions: MutableList<AiRespRoom> = mutableListOf();

        if (!reader.hasNext()) {
            Log.w(tag, "empty json string fed to ai response room adapter's parser")
            return aiRoomDescriptions
        }
        val firstTokenType = reader.peek()
        if (firstTokenType != JsonReader.Token.BEGIN_ARRAY) {
            throw JsonDataException("ai response room adapter expects a json array at root of json document but instead encountered ${reader.readJsonValue()}")
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
        return aiRoomDescriptions
    }


    fun parseSingleRoom(reader: JsonReader): AiRespRoom {
        reader.beginObject()
        var id: Int? = null
        var label: String? = null
        var neighbors: MutableList<Int>? = null;


        var detailsAboutAlreadyParsedFields = ""
        while (reader.hasNext()) {
            val nextKey = reader.nextName()
            val nextElementType = reader.peek()
            when (nextKey) {
                "id" -> {
                    when (nextElementType) {
                        JsonReader.Token.NUMBER -> {
                            try {
                                id = reader.nextInt()
                            } catch (e: JsonDataException) {
                                throw JsonDataException(
                                    "$badValMsgPrefix id was numeric but not integer$detailsAboutAlreadyParsedFields",
                                    e
                                )
                            }
                        }

                        JsonReader.Token.STRING -> {
                            val idStr = reader.nextString()
                            try {
                                id = Integer.parseInt(idStr)
                            } catch (e: NumberFormatException) {
                                throw JsonDataException(
                                    "$badValMsgPrefix id was string that couldn't parse to int$detailsAboutAlreadyParsedFields",
                                    e
                                )
                            }
                        }

                        else -> {
                            throw JsonDataException("$badValMsgPrefix id was null or of the wrong type: ${reader.readJsonValue()}$detailsAboutAlreadyParsedFields")
                        }
                    }
                    if (id < 0) {
                        throw JsonDataException("$badValMsgPrefix id was negative$detailsAboutAlreadyParsedFields")
                    }
                }

                "label" -> {
                    if (nextElementType == JsonReader.Token.STRING) {
                        label = reader.nextString()
                    } else {
                        throw JsonDataException("$badValMsgPrefix label was null or of the wrong type: ${reader.readJsonValue()}$detailsAboutAlreadyParsedFields")
                    }
                    if (label.isBlank()) {
                        throw JsonDataException("$badValMsgPrefix label was empty or all whitespace")
                    }
                }

                "neighbors" -> {
                    if (nextElementType == JsonReader.Token.BEGIN_ARRAY) {
                        neighbors = MutableList(0) { it }
                        reader.beginArray();
                        while (reader.hasNext()) {
                            val nextElementTypeInArr = reader.peek()
                            when (nextElementTypeInArr) {
                                JsonReader.Token.END_ARRAY -> break
                                JsonReader.Token.NUMBER -> {
                                    try {
                                        neighbors += reader.nextInt()
                                    } catch (e: JsonDataException) {
                                        throw JsonDataException(
                                            "$badValMsgPrefix neighbors contained an entry that was numeric but not integer$detailsAboutAlreadyParsedFields",
                                            e
                                        )
                                    }
                                }

                                JsonReader.Token.STRING -> {
                                    val neighborIdStr = reader.nextString()
                                    try {
                                        neighbors += Integer.parseInt(neighborIdStr)
                                    } catch (e: NumberFormatException) {
                                        throw JsonDataException(
                                            "$badValMsgPrefix neighbors contained an entry that was a string that couldn't parse to int$detailsAboutAlreadyParsedFields",
                                            e
                                        )
                                    }
                                }

                                else -> {
                                    throw JsonDataException("$badValMsgPrefix neighbors contained an entry that was null or of the wrong type: ${reader.readJsonValue()}$detailsAboutAlreadyParsedFields")
                                }
                            }
                        }
                        reader.endArray()

                    } else {
                        throw JsonDataException("$badValMsgPrefix neighbors was null or of the wrong type (i.e. that key wasn't followed by the start of an array): ${reader.readJsonValue()}$detailsAboutAlreadyParsedFields")
                    }
                }

                else -> {
                    throw JsonDataException("When parsing the json object for a Room in the dungeon map's json, unexpected key found: $nextKey, with value ${reader.readJsonValue()}$detailsAboutAlreadyParsedFields")
                }
            }

            detailsAboutAlreadyParsedFields = "; values extracted so far: " +
                    (if (id != null) ", id=$id" else "") + (if (label != null) ", label=$label" else "") +
                    (if (neighbors != null) ", neighbors=$neighbors" else "")
        }

        if (id == null) {
            throw JsonDataException("$missingKeyMsgPrefix id$detailsAboutAlreadyParsedFields")
        } else if (label == null) {
            throw JsonDataException("$missingKeyMsgPrefix label$detailsAboutAlreadyParsedFields")
        } else if (neighbors == null) {
            throw JsonDataException("$missingKeyMsgPrefix neighbors$detailsAboutAlreadyParsedFields")
        }

        reader.endObject()
        //makes sure list in data object is immutable
        return AiRespRoom(id, label, neighbors.toList())
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: List<AiRespRoom>?) {
        TODO("Not implemented")//not relevant for project
    }

}