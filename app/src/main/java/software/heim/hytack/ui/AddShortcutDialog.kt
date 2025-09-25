package software.heim.hytack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.data.domain.mapper
import software.heim.hytack.data.domain.milliliter

@Composable
fun AddShortcutDialog(
    onDisMissRequest: () -> Unit,
    onShortcutAdded: (millis: Milliliter) -> Unit,
    heading: String,
    modifier: Modifier = Modifier
) {
    val millis = rememberTextFieldState()
    val valid by remember { derivedStateOf { millis.text.isNotEmpty() } }
    Dialog(onDisMissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(heading, style = MaterialTheme.typography.headlineMedium)
                TextField(
                    state = millis,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    inputTransformation = InputTransformation.maxLength(6).then {
                        if (!asCharSequence().isDigitsOnly()) {
                            revertAllChanges()
                        }
                    },
                    label = { Text("Milliliters") }

                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onDisMissRequest) {
                        Text("Cancel")
                    }
                    TextButton({
                        onShortcutAdded(millis.text.toString().toInt().mapper().milliliter())
                        onDisMissRequest()
                    }, enabled = valid) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}