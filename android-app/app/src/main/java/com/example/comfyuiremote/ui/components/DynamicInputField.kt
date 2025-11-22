package com.example.comfyuiremote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.comfyuiremote.data.model.WorkflowNodeInput

@Composable
fun DynamicInputField(
    input: WorkflowNodeInput,
    value: Any?,
    onValueChange: (Any) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = input.name,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        when (input.type) {
            "int", "float" -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { newValue ->
                        if (input.type == "int") {
                            newValue.toIntOrNull()?.let { onValueChange(it) }
                        } else {
                            newValue.toFloatOrNull()?.let { onValueChange(it) }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "bool" -> {
                Switch(
                    checked = value as? Boolean ?: false,
                    onCheckedChange = { onValueChange(it) }
                )
            }
            else -> { // string and others
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { onValueChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
