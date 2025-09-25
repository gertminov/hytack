package software.heim.hytack.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LongClickButton(onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable ()-> Unit) {

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    var lastDown by remember { mutableLongStateOf(0L) }
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(onClick, onLongClick) {
        interactionSource.interactions.collect { interaction ->
            if (interaction !is PressInteraction ) return@collect

            when(interaction){
                is PressInteraction.Press -> {
                    lastDown = System.currentTimeMillis()
                    pressed = true
                    scope.launch {
                        delay(600)
                        if (pressed){
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                        pressed = false
                    }
                }
                is PressInteraction.Release -> {
                    if (System.currentTimeMillis() - lastDown < 500){
                        onClick()
                    }
                    pressed = false
                }

            }

        }
    }
    OutlinedButton(
        {},
        interactionSource = interactionSource,
        modifier = modifier
            .height(80.dp),
        shape = ButtonDefaults.squareShape,
        contentPadding = ButtonDefaults.MediumContentPadding
    ) {
        content()
    }
}