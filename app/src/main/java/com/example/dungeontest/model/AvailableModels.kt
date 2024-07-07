package com.example.dungeontest.model

data class AvailableModels(
    val id: Int,
    val title: String,
    val description: String,
    val codeName: String,
    val isDefault: Boolean
)

// first model in the list with isDefault == true will be selected by default
val cardInfos = listOf(
    AvailableModels(id = 0,"GPT-4", "OpenAI's most complex model","gpt-4-turbo", true),
    AvailableModels(id = 1,"GPT-4o", "OpenAI's latest model", "gpt-4o",false),
//    AvailableModels(id = 2,"GPT-3", "OpenAI's previous model", false),
//    AvailableModels(id = 3,"GPT-2", "OpenAI's first model", false),

)