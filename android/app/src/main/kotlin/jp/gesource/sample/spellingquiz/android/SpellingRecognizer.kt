package jp.gesource.sample.spellingquiz.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.recognition.RecognitionContext
import com.google.mlkit.vision.digitalink.recognition.WritingArea

enum class ModelState { PREPARING, READY, FAILED }

class SpellingRecognizer {
  private val model =
    DigitalInkRecognitionModel.builder(
        requireNotNull(DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US"))
      )
      .build()
  private val recognizer =
    DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build())
  private val modelManager = RemoteModelManager.getInstance()

  var modelState by mutableStateOf(ModelState.PREPARING)
    private set

  fun prepareModel() {
    modelManager
      .isModelDownloaded(model)
      .addOnSuccessListener { downloaded ->
        if (downloaded) {
          modelState = ModelState.READY
        } else {
          modelManager
            .download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener { modelState = ModelState.READY }
            .addOnFailureListener { modelState = ModelState.FAILED }
        }
      }
      .addOnFailureListener { modelState = ModelState.FAILED }
  }

  fun recognize(
    ink: Ink,
    width: Float,
    height: Float,
    onResult: (List<String>) -> Unit,
    onError: (Exception) -> Unit,
  ) {
    val context =
      RecognitionContext.builder().setPreContext("").setWritingArea(WritingArea(width, height)).build()
    recognizer
      .recognize(ink, context)
      .addOnSuccessListener { result -> onResult(result.candidates.map { it.text }) }
      .addOnFailureListener(onError)
  }

  fun close() {
    recognizer.close()
  }
}

fun matchesSpelling(candidates: List<String>, spelling: String): Boolean {
  val answer = spelling.trim().lowercase()
  return candidates.take(5).any { it.trim().lowercase() == answer }
}
