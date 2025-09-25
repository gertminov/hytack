package software.heim.hytack.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedFAB(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideIn(MotionScheme.expressive().defaultSpatialSpec(), initialOffset = { IntOffset(0,it.height) }),
        exit = fadeOut()+ slideOut(MotionScheme.expressive().defaultSpatialSpec(), targetOffset = { IntOffset(0,it.height) }),
        modifier = modifier.offset(16.dp, 16.dp)
    ) {
        MediumFloatingActionButton(onClick = onClick, modifier = Modifier.padding(16.dp) ) {
            Icon(Icons.Outlined.Share, "Share")
        }
    }
}