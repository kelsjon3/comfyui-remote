package com.example.comfyuiremote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
        
        // Check if this is a LoRA input (but not weight/strength/header/widget)
        val isLoraInput = input.name.contains("lora", ignoreCase = true) && 
                         !input.name.contains("weight", ignoreCase = true) &&
                         !input.name.contains("strength", ignoreCase = true) &&
                         !input.name.contains("header", ignoreCase = true) &&
                         !input.name.contains("widget", ignoreCase = true)

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
                                    // Handle complex inputs (Map) vs simple strings
                                    if (value is Map<*, *>) {
                                        try {
                                            @Suppress("UNCHECKED_CAST")
                                            val newMap = (value as Map<String, Any>).toMutableMap()
                                            
                                            // Try to find the correct key to update
                                            val keysToTry = listOf("ckpt_name", "checkpoint", "model", "name", "ckpt", "file", "filename")
                                            val keyToUpdate = keysToTry.firstOrNull { newMap.containsKey(it) } 
                                                ?: keysToTry.first() // Fallback to first common key
                                                
                                            newMap[keyToUpdate] = checkpoint
                                            onValueChange(newMap)
                                        } catch (e: Exception) {
                                            onValueChange(checkpoint)
                                        }
                                    } else {
                                        onValueChange(checkpoint)
                                    }
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
                
                // Extract just the filename from the value and clean up backslashes
                val rawValue = value?.toString() ?: ""
                val cleanValue = if (rawValue.contains("lora=")) {
                    // Extract lora filename from JSON-like string
                    rawValue.substringAfter("lora=").substringBefore(",").trim()
                } else {
                    rawValue
                }.replace("\\", "/")  // Replace backslashes with forward slashes
                
                val currentValue = cleanValue.ifEmpty { loras.firstOrNull()?.replace("\\", "/") ?: "" }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
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
                                    text = { Text(lora.replace("\\", "/")) },
                                    onClick = {
                                        // Handle complex inputs (Map) vs simple strings
                                        if (value is Map<*, *>) {
                                            try {
                                                @Suppress("UNCHECKED_CAST")
                                                val newMap = (value as Map<String, Any>).toMutableMap()
                                                
                                                // Try to find correct key for LoRA
                                                val keysToTry = listOf("lora", "lora_name", "model", "file", "filename")
                                                val keyToUpdate = keysToTry.firstOrNull { newMap.containsKey(it) } ?: "lora"
                                                
                                                newMap[keyToUpdate] = lora
                                                // Auto-enable if 'on' key exists
                                                if (newMap.containsKey("on")) {
                                                    newMap["on"] = true
                                                }
                                                onValueChange(newMap)
                                            } catch (e: Exception) {
                                                onValueChange(lora)
                                            }
                                        } else {
                                            onValueChange(lora)
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Remove button
                    androidx.compose.material3.IconButton(
                        onClick = { 
                            if (value is Map<*, *>) {
                                try {
                                    @Suppress("UNCHECKED_CAST")
                                    val newMap = (value as Map<String, Any>).toMutableMap()
                                    newMap["lora"] = "None"
                                    // Auto-disable if 'on' key exists
                                    if (newMap.containsKey("on")) {
                                        newMap["on"] = false
                                    }
                                    onValueChange(newMap)
                                } catch (e: Exception) {
                                    onValueChange("None")
                                }
                            } else {
                                onValueChange("None")
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Remove LoRA",
                            tint = MaterialTheme.colorScheme.error
                        )
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
