package software.heim.hytack.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animate
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.collections.getValue

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MaterialSlider(modifier: Modifier = Modifier) {

    val coroutineScope = rememberCoroutineScope()
    val sliderState =
        rememberSliderState(
            // Only allow multiples of 10. Excluding the endpoints of `valueRange`,
            // there are 9 steps (10, 20, ..., 90).
            steps = 9,
            valueRange = 0f..100f,
        )
    val snapAnimationSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    var currentValue by rememberSaveable { mutableFloatStateOf(sliderState.value) }
    var animateJob: Job? by remember { mutableStateOf(null) }
    sliderState.shouldAutoSnap = false
    sliderState.onValueChange = { newValue ->
        currentValue = newValue
        // only update the sliderState instantly if dragging
        if (sliderState.isDragging) {
            animateJob?.cancel()
            sliderState.value = newValue
        }
    }
    sliderState.onValueChangeFinished = {
        animateJob =
            coroutineScope.launch {
                animate(
                    initialValue = sliderState.value,
                    targetValue = currentValue,
                    animationSpec = snapAnimationSpec,
                ) { value, _ ->
                    sliderState.value = value
                }
            }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "%.2f".format(sliderState.value),
        )
        Spacer(Modifier.height(16.dp))
        VerticalSlider(
            state = sliderState,
            modifier =
                Modifier.height(300.dp)
                    .align(Alignment.CenterHorizontally)
                    .progressSemantics(
                        currentValue,
                        sliderState.valueRange.start..sliderState.valueRange.endInclusive,
                        sliderState.steps,
                    ),
            interactionSource = interactionSource,
            track = {
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.width(150.dp),
                    trackCornerSize = 36.dp,
                )
            },
            thumb = {

                SliderDefaults.Thumb(interactionSource, thumbSize = DpSize(160.dp, 4.dp))
            },
            reverseDirection = true,
        )
    }
}