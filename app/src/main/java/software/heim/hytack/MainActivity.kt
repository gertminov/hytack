package software.heim.hytack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import software.heim.hytack.ui.AnimatedFAB
import software.heim.hytack.ui.HydrationViewModel
import software.heim.hytack.ui.MainScreen
import software.heim.hytack.ui.theme.HytackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = HydrationViewModel(application)
        enableEdgeToEdge()
        setContent {
            val historyVisible by vm.historyVisible.collectAsState(false)
            HytackTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout), floatingActionButton = {
                        AnimatedFAB(historyVisible, {vm.exportHistory()})
                    }) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), viewModel = vm)
                }
            }
        }
    }
}
