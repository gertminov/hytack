package software.heim.hytack.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import software.heim.hytack.data.domain.Milliliter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressCircle(
    targetValue: Float,
    currentProgress: String,
    progressRingHeight: Dp,
    onUpdateGoal: (Milliliter) -> Unit,
    modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AddShortcutDialog({ showDialog = false }, { mil -> onUpdateGoal(mil) }, "Set Daily Goal")
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue,
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
            modifier = Modifier.size(progressRingHeight).clickable(onClick = {showDialog = true})
        )
        Text(
            text = currentProgress,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}