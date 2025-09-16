package software.heim.hytack.ui

import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.ui.LongClickButton
import software.heim.hytack.ui.theme.HytackTheme
import kotlin.text.format

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: HydrationViewModel) {
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val todaysTotal by viewModel.getTodaysTotal().collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val animatedProgress by animateFloatAsState(
                todaysTotal.value.toFloat() / dailyGoal.value,
                visibilityThreshold = 0.02f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            )
            CircularWavyProgressIndicator(
                { animatedProgress },
                stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke.let {
                    Stroke(
                        width = it.width * 3.5f,
                        cap = it.cap
                    )
                },
                trackStroke = WavyProgressIndicatorDefaults.circularTrackStroke.let {
                    Stroke(
                        width = it.width * 3.5f,
                        cap = it.cap
                    )
                },
                wavelength = 30.dp,
                waveSpeed = 3.dp,
                modifier = Modifier.size(240.dp)
            )
            Text(
                text = "${todaysTotal.format()} / ${dailyGoal.format()}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            horizontalArrangement = Arrangement.Center
        ) {

            var sliderValue by remember { mutableFloatStateOf(0f) }
//            MaterialSlider()
            HydrationSlider(
                sliderValue,
                { viewModel.addIntake(it) },
                listOf(200, 300, 500, 750, 1000).map { Milliliter(it) }
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(bottom = 40.dp)
        ) {
            ButtonBar(shortcuts)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun ButtonBar(
    shortcuts: List<Triple<Milliliter?, () -> Unit, (Milliliter) -> Unit>>,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    var onAddShortcut: ((Milliliter) -> Unit)? by remember { mutableStateOf(null) }
    if (showDialog) {
        AddShortcutDialog({ showDialog = false }, { mil -> onAddShortcut?.let { it(mil) } })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        shortcuts.forEach { (milliliter, onClick, onAdd) ->
            LongClickButton({
                milliliter?.let { onClick() } ?: run {
                    onAddShortcut = onAdd
                    showDialog = true
                }
            }, {
                onAddShortcut = onAdd
                showDialog = true
            }, modifier = Modifier.weight(1f)) {
                milliliter?.let {
                    Text(
                        text = it.format(),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } ?: Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HydrationScreenPreview() {
    HytackTheme {
        // You would typically mock the ViewModel for previews if it requires Android context.
        // For now, let's imagine a simpler Greeting if ViewModel is complex to mock.
        ButtonBar(listOf())
    }
}
