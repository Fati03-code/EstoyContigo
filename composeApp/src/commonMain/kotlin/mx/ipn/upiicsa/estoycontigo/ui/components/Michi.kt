package mx.ipn.upiicsa.estoycontigo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion

enum class MichiMood { CALM, HAPPY, CARING, SLEEPY, CURIOUS }

fun Emotion?.toMichiMood(): MichiMood = when (this) {
    Emotion.ALEGRIA -> MichiMood.HAPPY
    Emotion.CALMA -> MichiMood.CALM
    Emotion.TRISTEZA, Emotion.ENOJO, Emotion.ANSIEDAD -> MichiMood.CARING
    Emotion.CANSANCIO -> MichiMood.SLEEPY
    null -> MichiMood.CURIOUS
}

/**
 * Michi, la mascota de EstoyContigo: un gato dibujado con Canvas (sin imágenes externas),
 * que respira suavemente y parpadea. [breathScale] permite sincronizarlo con los ejercicios.
 */
@Composable
fun Michi(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    mood: MichiMood = MichiMood.CALM,
    furColor: Color = Color(0xFF8C9A92),
    breathScale: Float? = null,
    description: String = "Michi, el gato de EstoyContigo",
) {
    val transition = rememberInfiniteTransition(label = "michi")
    val idleBreath by transition.animateFloat(
        initialValue = 0.985f, targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    val blink by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4200
                1f at 0
                1f at 3900
                0.1f at 4000
                1f at 4150
            },
        ),
        label = "blink",
    )
    val tail by transition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "tail",
    )

    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val s = breathScale ?: idleBreath
        scale(s, pivot = Offset(this.size.width / 2, this.size.height * 0.9f)) {
            drawMichi(mood, furColor, if (mood == MichiMood.SLEEPY) 0f else blink, tail)
        }
    }
}

private fun DrawScope.drawMichi(mood: MichiMood, fur: Color, blink: Float, tailAngle: Float) {
    val w = size.width
    val h = size.height
    val ink = Color(0xFF2F3E36)
    val pink = Color(0xFFE9B9A6)
    val cheek = Color(0xFFF2C9BA).copy(alpha = 0.8f)
    val belly = Color(0xFFF3EEE6)
    val stroke = w * 0.026f

    // Cola
    rotate(tailAngle, pivot = Offset(w * 0.7f, h * 0.86f)) {
        val tail = Path().apply {
            moveTo(w * 0.68f, h * 0.86f)
            cubicTo(w * 0.95f, h * 0.88f, w * 0.98f, h * 0.62f, w * 0.86f, h * 0.55f)
        }
        drawPath(tail, fur, style = Stroke(width = w * 0.075f, cap = StrokeCap.Round))
    }

    // Cuerpo
    drawOval(fur, topLeft = Offset(w * 0.24f, h * 0.52f), size = Size(w * 0.52f, h * 0.42f))
    drawOval(belly, topLeft = Offset(w * 0.36f, h * 0.62f), size = Size(w * 0.28f, h * 0.3f))
    // Patitas
    drawRoundRect(fur, Offset(w * 0.33f, h * 0.86f), Size(w * 0.13f, h * 0.09f), CornerRadius(w * 0.06f))
    drawRoundRect(fur, Offset(w * 0.54f, h * 0.86f), Size(w * 0.13f, h * 0.09f), CornerRadius(w * 0.06f))

    // Orejas
    fun ear(x1: Float, x2: Float, tipX: Float, inner: Boolean) {
        val p = Path().apply {
            moveTo(w * x1, h * 0.27f)
            lineTo(w * tipX, h * (if (inner) 0.11f else 0.06f))
            lineTo(w * x2, h * 0.22f)
            close()
        }
        drawPath(p, if (inner) pink else fur)
    }
    ear(0.2f, 0.42f, 0.24f, false)
    ear(0.8f, 0.58f, 0.76f, false)
    ear(0.25f, 0.38f, 0.27f, true)
    ear(0.75f, 0.62f, 0.73f, true)

    // Cabeza
    drawOval(fur, topLeft = Offset(w * 0.16f, h * 0.14f), size = Size(w * 0.68f, h * 0.48f))

    // Ojos
    val eyeY = h * 0.36f
    val leftX = w * 0.36f
    val rightX = w * 0.64f
    when {
        mood == MichiMood.HAPPY || mood == MichiMood.CALM || blink < 0.3f -> {
            // Ojos cerrados felices (arcos)
            listOf(leftX, rightX).forEach { x ->
                val arc = Path().apply {
                    moveTo(x - w * 0.055f, eyeY)
                    quadraticTo(x, eyeY + (if (mood == MichiMood.HAPPY) -h * 0.05f else h * 0.04f), x + w * 0.055f, eyeY)
                }
                drawPath(arc, ink, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        mood == MichiMood.SLEEPY -> listOf(leftX, rightX).forEach { x ->
            drawLine(ink, Offset(x - w * 0.05f, eyeY), Offset(x + w * 0.05f, eyeY), stroke, StrokeCap.Round)
        }
        else -> listOf(leftX, rightX).forEach { x ->
            val eh = h * 0.075f * blink
            drawOval(ink, Offset(x - w * 0.04f, eyeY - eh / 2), Size(w * 0.08f, eh))
            drawCircle(Color.White, w * 0.013f, Offset(x + w * 0.012f, eyeY - eh * 0.2f))
            if (mood == MichiMood.CARING) {
                // Cejas suaves de preocupación empática
                drawLine(ink.copy(alpha = 0.6f), Offset(x - w * 0.05f, eyeY - h * 0.07f), Offset(x + w * 0.04f, eyeY - h * 0.085f), stroke * 0.6f, StrokeCap.Round)
            }
        }
    }

    // Mejillas
    drawOval(cheek, Offset(w * 0.24f, h * 0.43f), Size(w * 0.09f, h * 0.045f))
    drawOval(cheek, Offset(w * 0.67f, h * 0.43f), Size(w * 0.09f, h * 0.045f))

    // Nariz
    val nose = Path().apply {
        moveTo(w * 0.47f, h * 0.425f); lineTo(w * 0.53f, h * 0.425f); lineTo(w * 0.5f, h * 0.455f); close()
    }
    drawPath(nose, pink)

    // Boca
    val mouth = Path().apply {
        moveTo(w * 0.5f, h * 0.455f)
        quadraticTo(w * 0.47f, h * 0.5f, w * 0.43f, h * 0.48f)
        moveTo(w * 0.5f, h * 0.455f)
        quadraticTo(w * 0.53f, h * 0.5f, w * 0.57f, h * 0.48f)
    }
    drawPath(mouth, ink, style = Stroke(stroke * 0.75f, cap = StrokeCap.Round))

    // Bigotes
    val wh = ink.copy(alpha = 0.45f)
    listOf(-1f, 1f).forEach { side ->
        val baseX = w * (0.5f + side * 0.13f)
        drawLine(wh, Offset(baseX, h * 0.45f), Offset(baseX + side * w * 0.16f, h * 0.43f), stroke * 0.45f, StrokeCap.Round)
        drawLine(wh, Offset(baseX, h * 0.47f), Offset(baseX + side * w * 0.16f, h * 0.48f), stroke * 0.45f, StrokeCap.Round)
    }

    if (mood == MichiMood.SLEEPY) {
        // "z" pequeña
        val z = Path().apply {
            moveTo(w * 0.8f, h * 0.12f); lineTo(w * 0.88f, h * 0.12f); lineTo(w * 0.8f, h * 0.2f); lineTo(w * 0.88f, h * 0.2f)
        }
        drawPath(z, ink.copy(alpha = 0.5f), style = Stroke(stroke * 0.7f, cap = StrokeCap.Round))
    }
    if (mood == MichiMood.CARING) {
        // Corazón pequeño junto a Michi
        val hx = w * 0.84f
        val hy = h * 0.2f
        val r = w * 0.035f
        drawCircle(Color(0xFFE9A89A), r, Offset(hx - r * 0.7f, hy))
        drawCircle(Color(0xFFE9A89A), r, Offset(hx + r * 0.7f, hy))
        val tip = Path().apply {
            moveTo(hx - r * 1.65f, hy + r * 0.25f); lineTo(hx, hy + r * 2.1f); lineTo(hx + r * 1.65f, hy + r * 0.25f); close()
        }
        drawPath(tip, Color(0xFFE9A89A))
    }
}
