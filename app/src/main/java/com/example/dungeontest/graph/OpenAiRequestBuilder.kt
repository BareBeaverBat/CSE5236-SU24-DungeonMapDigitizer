
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()

data class OpenAIResponse(val id: String, val objectType: String, val created: Long, val choice: Choice)
data class Choice(val text: String, val index: Int, val logprobs: Any?, val finish_reason: String?)

class OpenAiRequestBuilder {
    private val baseUri = "https://api.openai.com/v1/images/generations"
    private val prompt = """
        This drawing describes a network of rooms, which can be thought of as an undirected graph (each room is a node, hallways are edges). Each room is represented by box containing a text label, and each hallway is represented by 2 parallel lines.

        All of your output must be in a single JSON object.

        First, please create a numbered list of the rooms in the drawing (root-level json key is "rooms", each list entry should have a numeric id in addition to a name).
        Second, for each room (A), make a list for A's top side hallways of the names and ids of the rooms on the other ends of those hallways; then make a list for A's left side hallways of the names and ids of the rooms on the other ends of those hallways; then make a list for A's right side hallways of the names and ids of the rooms on the other ends of those hallways; then make a list for A's bottom side hallways of the names and ids of the rooms on the other ends of those hallways; finally, create a single list with all of A's neighbors on those different sides. (root level json key is "adjacency_analysis").
        Third, please make a JSON array describing that graph (root level key for the array is "final_result"), where each entry in the array is a json object containing a) a unique numeric node id (json key "id"), b) the room label from the drawing (json key "name"), and c) the id's of the rooms which it's connected to by hallways (json key "neighbors").
    """.trimIndent()

    private val client = OkHttpClient()

    fun testingRequests(callback: (String?) -> Unit){
        val request = Request.Builder()
            .url("https://nvgtn.com/products.json")
            .addHeader("Referer", "https://nvgtn.com/products.json")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Unexpected code $response")
                    callback(null)
                } else {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        callback(it)
                    } ?: run {
                        callback(null)
                    }
                }
            }
        })
    }

    fun sendOpenAIRequest(apiKey: String, imagePath: String, callback: (OpenAIResponse?) -> Unit) {
        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            println("Image file not found: $imagePath")
            callback(null)
            return
        }

        val imageRequestBody = imageFile.asRequestBody(MEDIA_TYPE_PNG)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("image", imageFile.name, imageRequestBody)
            .build()

        val request = Request.Builder()
            .url(baseUri)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json") // Ensure the response returns JSON
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Unexpected code $response")
                    callback(null)
                } else {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)
                        val choiceJson = jsonResponse.getJSONArray("choices").getJSONObject(0)

                        val choice = Choice(
                            text = choiceJson.getString("text"),
                            index = choiceJson.getInt("index"),
                            logprobs = choiceJson.opt("logprobs"),
                            finish_reason = choiceJson.getString("finish_reason")
                        )

                        val openAIResponse = OpenAIResponse(
                            id = jsonResponse.getString("id"),
                            objectType = jsonResponse.getString("object"),
                            created = jsonResponse.getLong("created"),
                            choice = choice
                        )
                        callback(openAIResponse)
                    } ?: run {
                        callback(null)
                    }
                }
            }
        })
    }
}
