package software.heim.hytack.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.paging.compose.collectAsLazyPagingItems
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.ui.theme.HytackTheme
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: HydrationViewModel) {
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val todaysTotal by viewModel.getTodaysTotal().collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()
    val pagedIntakes = viewModel.intakePager.collectAsLazyPagingItems()



    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dragState = remember { AnchoredDraggableState(initialValue = DragStates.RESTING) }
        val density = LocalDensity.current
        val progressRingHeight = 240.dp
        val cardHeightOffset = 50.dp
        val cardHeightOffsetPx = with(density) { cardHeightOffset.toPx() }
        val cardHeight = remember(density) {
            maxHeight - (progressRingHeight + 100.dp)
        }
        val draggableHeight = remember(density) {
            with(density) { cardHeight.toPx() }
        }
        val anchors = remember(density) {
            DraggableAnchors {
                DragStates.RESTING at 0f
                DragStates.DRAGGED at draggableHeight
            }
        }
        SideEffect { dragState.updateAnchors(anchors) }
        LaunchedEffect(dragState.currentValue) {
            if (dragState.currentValue == DragStates.DRAGGED) {
                viewModel.historyVisible.emit(true)
            } else {
                viewModel.historyVisible.emit(false)
            }
        }


        Column(
            modifier = modifier
                .fillMaxSize()
                .anchoredDraggable(
                    dragState,
                    Orientation.Vertical
                )
                .offset {
                    IntOffset(
                        x = 0,
                        y = dragState.requireOffset().roundToInt()

                    )
                }
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressCircle(
                todaysTotal.value.toFloat() / dailyGoal.value,
                "${todaysTotal.format()} / ${dailyGoal.format()}",
                progressRingHeight,
                { viewModel.updateDailyGoal(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                horizontalArrangement = Arrangement.Center
            ) {

                var sliderValue by remember { mutableFloatStateOf(0f) }
                HydrationSlider(
                    sliderValue,
                    { viewModel.addIntake(it) },
                    listOf(200, 300, 500, 750, 1000).map { Milliliter(it) }
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
            ) {
                ButtonBar(shortcuts)
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight + cardHeightOffset)
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = dragState.requireOffset()
                            .roundToInt() - (draggableHeight.fastRoundToInt() - 100 + cardHeightOffsetPx.fastRoundToInt())
                    )
                }
        ) {
            HistoryList(
                pagedIntakes,
                { viewModel.deleteIntake(it) },
                dragState.targetValue != DragStates.RESTING,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val rotationAngle by animateFloatAsState(
                    targetValue = if (dragState.targetValue == DragStates.DRAGGED) {
                        180f
                    } else {
                        0f
                    },
                    animationSpec = MotionScheme.expressive().defaultEffectsSpec()
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    "pull",
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }

    Box(modifier= Modifier.fillMaxSize().padding()){
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
        AddShortcutDialog({ showDialog = false }, { mil -> onAddShortcut?.let { it(mil) } }, "Add Drink Shortcut")
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        shortcuts.forEach { (milliliter, onClick, onAdd) ->
            LongClickButton({
                milliliter?.let { onClick(); } ?: run {
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
