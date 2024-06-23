package com.example.dungeontest.graph

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class AiRespRoomAdapter : JsonAdapter<AiRespRoom>() {
    private val badValMsgPrefix =
        "When parsing the json object for a Room in the dungeon map's json, the value for the key"
    private val missingKeyMsgPrefix =
        "The json object for a Room in the dungeon map's json was missing the key"

    override fun fromJson(reader: JsonReader): AiRespRoom {
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

        //makes sure list in data object is immutable
        return AiRespRoom(id, label, neighbors.toList())
    }

    override fun toJson(writer: JsonWriter, value: AiRespRoom?) {
        TODO("Not implemented")//not relevant for project
    }

}