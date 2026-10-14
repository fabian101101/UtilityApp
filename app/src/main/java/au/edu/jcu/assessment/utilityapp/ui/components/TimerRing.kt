package au.edu.jcu.assessment.utilityapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val RING_START_ANGLE = -90f
private const val FULL_CIRCLE = 360f
private const val RING_ANIMATION_MILLIS = 500

/**
 * Circular countdown. The ring is full when a session is ready and empties as time passes.
 * [progress] runs from 1.0 (full) to 0.0 (empty).
 */
@Composable
fun TimerRing(
    progress: Float,
    timeText: String,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(RING_ANIMATION_MILLIS),
        label = "ringProgress"
    )
    val trackColor = MaterialTheme.colorScheme.primaryContainer
    val ringColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(260.dp)
            .semantics { contentDescription = "$timeText, $statusText" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val inset = strokeWidth / 2
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = FULL_CIRCLE,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = ringColor,
                startAngle = RING_START_ANGLE,
                sweepAngle = FULL_CIRCLE * animatedProgress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
