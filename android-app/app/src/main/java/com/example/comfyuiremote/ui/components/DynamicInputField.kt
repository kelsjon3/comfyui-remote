package com.example.comfyuiremote.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.comfyuiremote.data.model.WorkflowNodeInput

@Composable
fun DynamicInputField(
    input: WorkflowNodeInput,
    value: Any?,
    onValueChange: (Any) -> Unit,
    checkpoints: List<String> = emptyList(),
    loras: List<String> = emptyList()
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = input.name,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Check if this is a checkpoint input
        val isCheckpointInput = input.name.contains("ckpt", ignoreCase = true) || 
                                input.name.contains("checkpoint", ignoreCase = true)
        
        // Check if this is a LoRA input (but not weight/strength)
        val isLoraInput = input.name.contains("lora", ignoreCase = true) && 
                         !input.name.contains("weight", ignoreCase = true) &&
                         !input.name.contains("strength", ignoreCase = true)

        when {
            isCheckpointInput && checkpoints.isNotEmpty() -> {
                // Dropdown for checkpoints
                var expanded by remember { mutableStateOf(false) }
                val currentValue = value?.toString() ?: checkpoints.firstOrNull() ?: ""

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { expanded = true }) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                    contentDescription = "Select checkpoint"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    androidx.compose.material3.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        checkpoints.forEach { checkpoint ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(checkpoint) },
                                onClick = {
                                    onValueChange(checkpoint)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            isLoraInput && loras.isNotEmpty() -> {
                // Dropdown for LoRAs
                var expanded by remember { mutableStateOf(false) }
                
                // Extract just the filename from the value (in case it's a complex string)
                val rawValue = value?.toString() ?: ""
                val cleanValue = if (rawValue.contains("lora=")) {
                    // Extract lora filename from JSON-like string
                    rawValue.substringAfter("lora=").substringBefore(",").trim()
                } else {
                    rawValue
                }
                val currentValue = cleanValue.ifEmpty { loras.firstOrNull() ?: "" }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { expanded = true }) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                    contentDescription = "Select LoRA"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    androidx.compose.material3.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        loras.forEach { lora ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(lora) },
                                onClick = {
                                    onValueChange(lora)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            input.type == "int" || input.type == "float" -> {
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
            input.type == "bool" -> {
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
