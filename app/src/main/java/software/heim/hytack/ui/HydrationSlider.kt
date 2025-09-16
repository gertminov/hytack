package software.heim.hytack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtMost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import software.heim.hytack.data.domain.Milliliter
import kotlin.math.roundToInt

@Composable
fun HydrationSlider(
    value: Float, // Expected to be between 0f (bottom) and 1f (top)
    onValueChange: (Milliliter) -> Unit,
    steps: List<Milliliter>,
    width: Dp = 150.dp,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val activeTrackColor = SliderDefaults.colors().activeTrackColor
    val inactiveTrackColor = SliderDefaults.colors().inactiveTrackColor
    val borderColor = MaterialTheme.colorScheme.outline
    val activeTickColor = SliderDefaults.colors().activeTickColor
    val inactiveTickColor = SliderDefaults.colors().inactiveTickColor
    var oldValue by remember { mutableFloatStateOf(value) }
    var dragging by remember { mutableStateOf(false) }
    val currentMl by remember {
        derivedStateOf {
            steps.getOrElse(
                (oldValue * steps.size).roundToInt() - 1,
                { Milliliter(0) })
        }
    }
    val animatedSliderPosition by animateFloatAsState(
        targetValue = oldValue,
        animationSpec = spring()
    )

    val scope = rememberCoroutineScope()


    // Configuration for appearance
    val stepCount = steps.size - 1
    val stepSize = 1f / (stepCount + 1)
    val cornerRadiusRatio = 1f / 5f // Corner radius as a fraction of the width
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            currentMl.format(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BoxWithConstraints(
            modifier = modifier
                .width(width)
                .fillMaxHeight()
                .pointerInput(enabled, onValueChange) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = {

                            dragging = false
                            scope.launch {
                                delay(500)
                                if (!dragging) {
                                    onValueChange(
                                        currentMl
                                    )
                                    oldValue = 0f
                                }
                            }
                        }) { change, _ ->
                        change.consume()
                        val newValue = (size.height - change.position.y.coerceIn(
                            0f,
                            size.height.toFloat()
                        )) / size.height.toFloat()
                        val steppedValue = (newValue / stepSize).roundToInt() * stepSize
                        if (oldValue != steppedValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                        oldValue = steppedValue
                    }


                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val cornerRadiusPx = canvasWidth * cornerRadiusRatio

                // 1. Draw the inactive track (background)
                drawRoundRect(
                    color = inactiveTrackColor,
                    topLeft = Offset.Zero,
                    size = this.size,
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )

                // 2. Draw the active track (filled portion)
                val coercedValue = animatedSliderPosition.coerceIn(0f, 1f)
                val activeTrackActualHeight = canvasHeight * coercedValue
                val heightFromBottom = canvasHeight - activeTrackActualHeight
                if (coercedValue >= 0f) {
                    val clipPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(Offset.Zero, size),
                                cornerRadius = CornerRadius(cornerRadiusPx)
                            )
                        )
                    }

                    val handlePaddingX = canvasWidth / 2.5f // Padding from each side
                    val handleHeight = (canvasHeight - (activeTrackActualHeight - 22.dp.toPx())).fastCoerceAtMost(canvasHeight - 16.dp.toPx())


                    clipPath(path = clipPath, clipOp = ClipOp.Intersect) {
                        drawRoundRect(
                            color = activeTrackColor,
                            topLeft = Offset(0f, heightFromBottom),
                            size = Size(canvasWidth, activeTrackActualHeight),
                            cornerRadius = CornerRadius(cornerRadiusPx)
                        )
                        drawLine(
                            color = if (coercedValue == 0f) borderColor else inactiveTrackColor,
                            start = Offset(handlePaddingX, handleHeight),
                            end = Offset(canvasWidth - handlePaddingX, handleHeight),
                            strokeWidth = 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 3. Draw the track border
//                drawRoundRect(
//                    color = borderColor,
//                    topLeft = Offset.Zero,
//                    size = this.size,
//                    cornerRadius = CornerRadius(cornerRadiusPx),
//                    style = Stroke(width = 1.dp.toPx())
//                )

                // 4. Draw Tick Marks
                val tickLineStrokeWidthPx = 4.dp.toPx()
                val tickHorizontalPaddingPx = canvasWidth / 8f

                if (stepCount > 0) {
                    val numSections = stepCount + 1
                    for (i in 1..stepCount) {
                        val yPos = canvasHeight * (i.toFloat() / numSections.toFloat())
                        drawLine(
                            color = if (heightFromBottom + 49 < yPos) activeTickColor else inactiveTickColor,
                            start = Offset(tickHorizontalPaddingPx, yPos),
                            end = Offset(canvasWidth - tickHorizontalPaddingPx, yPos),
                            strokeWidth = tickLineStrokeWidthPx
                        )
                    }
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun HydrationSliderPreview() {
    var sliderValue by remember { mutableStateOf(0.5f) }
    MaterialTheme {
        HydrationSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it.value.toFloat() },
            steps = listOf(),
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HydrationSliderDisabledPreview() {
    var sliderValue by remember { mutableStateOf(0.3f) }
    MaterialTheme {
        HydrationSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it.value.toFloat() },
            steps = listOf(),
            enabled = false,
            modifier = Modifier.fillMaxHeight()
        )
    }
}
