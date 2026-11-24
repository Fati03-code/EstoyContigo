package mx.ipn.upiicsa.estoycontigo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.theme.colors

/** Carita minimalista por emoción (decorativa: el texto acompaña siempre a la carita). */
@Composable
fun EmotionFace(emotion: Emotion, modifier: Modifier = Modifier, size: Int = 40) {
    val c = emotion.colors(AppTheme.extra.isDark)
    Canvas(modifier.size(size.dp)) {
        val w = this.size.width
        val st = w * 0.07f
        drawCircle(c.base, w / 2)
        val ink = c.onContainer
        val eyeY = w * 0.42f
        when (emotion) {
            Emotion.CANSANCIO -> {
                drawLine(ink, Offset(w * 0.28f, eyeY), Offset(w * 0.4f, eyeY), st, StrokeCap.Round)
                drawLine(ink, Offset(w * 0.6f, eyeY), Offset(w * 0.72f, eyeY), st, StrokeCap.Round)
            }
            Emotion.CALMA -> listOf(0.34f, 0.66f).forEach { x ->
                val p = Path().apply {
                    moveTo(w * (x - 0.07f), eyeY); quadraticTo(w * x, eyeY + w * 0.06f, w * (x + 0.07f), eyeY)
                }
                drawPath(p, ink, style = Stroke(st, cap = StrokeCap.Round))
            }
            else -> {
                drawCircle(ink, w * 0.05f, Offset(w * 0.35f, eyeY))
                drawCircle(ink, w * 0.05f, Offset(w * 0.65f, eyeY))
            }
        }
        if (emotion == Emotion.ENOJO) {
            drawLine(ink, Offset(w * 0.26f, w * 0.29f), Offset(w * 0.42f, w * 0.34f), st, StrokeCap.Round)
            drawLine(ink, Offset(w * 0.74f, w * 0.29f), Offset(w * 0.58f, w * 0.34f), st, StrokeCap.Round)
        }
        val mouth = Path()
        val my = w * 0.66f
        when (emotion) {
            Emotion.ALEGRIA -> { mouth.moveTo(w * 0.3f, my - w * 0.04f); mouth.quadraticTo(w * 0.5f, my + w * 0.14f, w * 0.7f, my - w * 0.04f) }
            Emotion.CALMA -> { mouth.moveTo(w * 0.38f, my); mouth.quadraticTo(w * 0.5f, my + w * 0.06f, w * 0.62f, my) }
            Emotion.TRISTEZA -> { mouth.moveTo(w * 0.34f, my + w * 0.06f); mouth.quadraticTo(w * 0.5f, my - w * 0.07f, w * 0.66f, my + w * 0.06f) }
            Emotion.ENOJO -> { mouth.moveTo(w * 0.36f, my + w * 0.04f); mouth.quadraticTo(w * 0.5f, my - w * 0.04f, w * 0.64f, my + w * 0.04f) }
            Emotion.ANSIEDAD -> {
                mouth.moveTo(w * 0.32f, my); mouth.quadraticTo(w * 0.41f, my - w * 0.06f, w * 0.5f, my)
                mouth.quadraticTo(w * 0.59f, my + w * 0.06f, w * 0.68f, my)
            }
            Emotion.CANSANCIO -> { mouth.moveTo(w * 0.42f, my + w * 0.02f); mouth.lineTo(w * 0.58f, my + w * 0.02f) }
        }
        drawPath(mouth, ink, style = Stroke(st, cap = StrokeCap.Round))
    }
}
