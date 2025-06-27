package io.github.samuelmarks.off_on_ml

/*import io.github.modelcontext.ModelContext
import io.github.modelcontext.google.Google*/
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIModelService(apiKey: String) {
    // Initialize the SDK with the Google vendor and the provided API key.
    /*private val modelContext = ModelContext(
        vendors = listOf(Google(apiKey))
    )*/

    /**
     * Generates content using the specified model and prompt.
     * @param modelId The ID of the model to use (e.g., "google/gemini-1.5-flash").
     * @param prompt The text prompt for the model.
     * @return The generated text content or an error message.
     */
    suspend fun generateContent(modelId: String, prompt: String): String = withContext(Dispatchers.Default) {
        /*try {
            // Use the SDK to create a request
            val response = modelContext.create(modelId) {
                // The SDK supports multi-modal input, but for now we just add text.
                // In the future, you could add images here too.
                addText(prompt)
            }
            // Extract the first piece of text content from the response
            response.content.firstOrNull()?.text ?: "No content received."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }*/
        "Error: NotImplemented [received $modelId, \"$prompt\"]"
    }
}
