package jp.gesource.sample.spellingquiz.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.digitalink.recognition.Ink

/** 手書きの1点。座標は入力欄内の px、t は uptime のミリ秒。 */
data class InkPoint(val x: Float, val y: Float, val t: Long)

@Composable
fun HandwritingPad(
  strokes: List<List<InkPoint>>,
  onStrokesChange: (List<List<InkPoint>>) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val currentStrokes by rememberUpdatedState(strokes)
  val currentOnStrokesChange by rememberUpdatedState(onStrokesChange)

  Canvas(
    modifier =
      modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
          val down = awaitFirstDown()
          val previous = currentStrokes
          var stroke = listOf(InkPoint(down.position.x, down.position.y, down.uptimeMillis))
          currentOnStrokesChange(previous + listOf(stroke))
          down.consume()
          while (true) {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            stroke = stroke + InkPoint(change.position.x, change.position.y, change.uptimeMillis)
            currentOnStrokesChange(previous + listOf(stroke))
            change.consume()
          }
        }
      }
  ) {
    val style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    for (points in strokes) {
      if (points.size == 1) {
        drawCircle(Color.Black, radius = style.width / 2, center = Offset(points[0].x, points[0].y))
        continue
      }
      val path = Path()
      points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
      drawPath(path, Color.Black, style = style)
    }
  }
}

fun toInk(strokes: List<List<InkPoint>>): Ink {
  val inkBuilder = Ink.builder()
  for (points in strokes) {
    val strokeBuilder = Ink.Stroke.builder()
    for (p in points) strokeBuilder.addPoint(Ink.Point.create(p.x, p.y, p.t))
    inkBuilder.addStroke(strokeBuilder.build())
  }
  return inkBuilder.build()
}
