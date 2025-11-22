package com.example.comfyuiremote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.comfyuiremote.data.model.SeedControl
import com.example.comfyuiremote.ui.components.DynamicInputField
import com.example.comfyuiremote.viewmodel.WorkflowViewModel

@Composable
fun WorkflowDetailScreen(
    workflowName: String,
    viewModel: WorkflowViewModel
) {
    val introspection by viewModel.introspection.collectAsState()
    val currentJob by viewModel.currentJob.collectAsState()
    val error by viewModel.error.collectAsState()

    // Local state for form inputs
    val inputValues = remember { mutableStateMapOf<String, Any>() }
    var randomizeSeed by remember { mutableStateOf(true) }
    var seedValue by remember { mutableStateOf(0L) }

    LaunchedEffect(workflowName) {
        viewModel.introspectWorkflow(workflowName)
    }

    // Initialize defaults when introspection loads
    LaunchedEffect(introspection) {
        introspection?.nodes?.forEach { node ->
            node.inputs.forEach { input ->
                if (input.default != null) {
                    val key = "${node.id}.${input.name}"
                    if (!inputValues.containsKey(key)) {
                        inputValues[key] = input.default
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Workflow: $workflowName", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (introspection == null) {
            CircularProgressIndicator()
        } else {
            // Render Inputs
            introspection!!.nodes.forEach { node ->
                if (node.inputs.isNotEmpty()) {
                    Text(
                        text = node.label ?: node.type,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    node.inputs.forEach { input ->
                        if (input.isSeed) {
                            // Special handling for seed
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = randomizeSeed,
                                    onCheckedChange = { randomizeSeed = it }
                                )
                                Text("Randomize Seed")
                            }
                            if (!randomizeSeed) {
                                DynamicInputField(
                                    input = input.copy(name = "Seed Value", type = "int"),
                                    value = seedValue,
                                    onValueChange = { seedValue = (it as? Int)?.toLong() ?: 0L }
                                )
                            }
                        } else {
                            val key = "${node.id}.${input.name}"
                            DynamicInputField(
                                input = input,
                                value = inputValues[key],
                                onValueChange = { inputValues[key] = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val seedControl = if (randomizeSeed) {
                        SeedControl(mode = "random")
                    } else {
                        SeedControl(mode = "fixed", value = seedValue)
                    }
                    viewModel.runWorkflow(workflowName, inputValues.toMap(), seedControl)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Workflow")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
        }

        if (currentJob != null) {
            Text(text = "Status: ${currentJob?.status}")
            
            if (currentJob?.imageUrl != null) {
                Text(text = "Generated Image:", style = MaterialTheme.typography.titleMedium)
                
                // 10.0.2.2 is localhost for Android Emulator
                val imageUrl = "http://10.0.2.2:8000${currentJob?.imageUrl}"
                
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Generated Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
