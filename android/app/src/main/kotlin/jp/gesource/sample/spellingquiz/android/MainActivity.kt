package jp.gesource.sample.spellingquiz.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import jp.gesource.sample.spellingquiz.android.ui.theme.SpellingQuizTheme

class MainActivity : ComponentActivity() {
  private val recognizer = SpellingRecognizer()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val cards = loadWordCards(this)
    recognizer.prepareModel()

    enableEdgeToEdge()
    setContent {
      SpellingQuizTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          SpellingQuizScreen(cards = cards, recognizer = recognizer)
        }
      }
    }
  }

  override fun onDestroy() {
    recognizer.close()
    super.onDestroy()
  }
}
