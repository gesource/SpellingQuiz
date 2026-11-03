package jp.gesource.sample.spellingquiz.android

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SpellingQuizState { WRITING, CORRECT, INCORRECT, FINISHED }

@Composable
fun SpellingQuizScreen(cards: List<WordCard>, recognizer: SpellingRecognizer, modifier: Modifier = Modifier) {
  var index by rememberSaveable { mutableIntStateOf(0) }
  var state by rememberSaveable { mutableStateOf(SpellingQuizState.WRITING) }
  var strokes by remember { mutableStateOf(emptyList<List<InkPoint>>()) }
  var padSize by remember { mutableStateOf(IntSize.Zero) }
  var recognizing by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  fun startWriting(newIndex: Int) {
    index = newIndex
    state = SpellingQuizState.WRITING
    strokes = emptyList()
    errorMessage = null
  }

  fun goNext() {
    if (index + 1 < cards.size) {
      startWriting(index + 1)
    } else {
      state = SpellingQuizState.FINISHED
      strokes = emptyList()
      errorMessage = null
    }
  }

  fun judge() {
    errorMessage = null
    val spelling = cards[index].spelling
    if (strokes.isEmpty()) {
      state = SpellingQuizState.INCORRECT
      return
    }
    recognizing = true
    recognizer.recognize(
      ink = toInk(strokes),
      width = padSize.width.toFloat(),
      height = padSize.height.toFloat(),
      onResult = { candidates ->
        recognizing = false
        state =
          if (matchesSpelling(candidates, spelling)) SpellingQuizState.CORRECT
          else SpellingQuizState.INCORRECT
      },
      onError = {
        recognizing = false
        errorMessage = "認識に失敗しました"
      },
    )
  }

  if (state == SpellingQuizState.FINISHED) {
    Column(
      modifier = modifier.fillMaxSize().safeDrawingPadding(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text("終了", style = MaterialTheme.typography.displayMedium)
      Button(onClick = { startWriting(0) }, modifier = Modifier.padding(top = 24.dp)) { Text("最初から") }
    }
    return
  }

  val card = cards[index]
  val writing = state == SpellingQuizState.WRITING
  val modelState = recognizer.modelState

  Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(card.meaning, style = MaterialTheme.typography.headlineLarge)
    }

    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        HandwritingPad(
          strokes = strokes,
          onStrokesChange = { strokes = it },
          enabled = writing,
          modifier =
            Modifier.weight(1f)
              .fillMaxWidth()
              .border(1.dp, Color.Gray)
              .clipToBounds()
              .onSizeChanged { padSize = it },
        )
        if (modelState == ModelState.FAILED) {
          Text("モデルのダウンロードに失敗しました", color = MaterialTheme.colorScheme.error)
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
          OutlinedButton(
            onClick = {
              strokes = emptyList()
              errorMessage = null
            },
            enabled = writing,
          ) {
            Text("消す")
          }
          Button(
            onClick = { judge() },
            enabled = writing && modelState == ModelState.READY && !recognizing,
          ) {
            Text(if (modelState == ModelState.PREPARING) "準備中" else "判定")
          }
        }
      }

      if (state == SpellingQuizState.CORRECT || state == SpellingQuizState.INCORRECT) {
        val correct = state == SpellingQuizState.CORRECT
        // 横向きでは下半分の高さが足りないため、結果とボタンを横に並べ、○×を小さくする
        val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val markSize = if (landscape) 56.sp else 96.sp
        val overlayModifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.9f))
        val result: @Composable () -> Unit = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (correct) {
              Text("○", fontSize = markSize, color = Color(0xFF2E7D32))
            } else {
              Text("×", fontSize = markSize, color = Color(0xFFC62828))
              Text(card.spelling, style = MaterialTheme.typography.headlineMedium, color = Color.Black)
            }
          }
        }
        val buttons: @Composable () -> Unit = {
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { goNext() }) { Text("次へ") }
            if (!correct) OutlinedButton(onClick = { startWriting(index) }) { Text("やり直す") }
          }
        }
        if (landscape) {
          Row(
            modifier = overlayModifier,
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            result()
            buttons()
          }
        } else {
          Column(
            modifier = overlayModifier,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            result()
            buttons()
          }
        }
      }
    }
  }
}
