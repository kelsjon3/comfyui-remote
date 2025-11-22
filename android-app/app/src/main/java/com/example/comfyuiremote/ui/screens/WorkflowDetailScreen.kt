package com.example.comfyuiremote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.comfyuiremote.data.model.SeedControl
import com.example.comfyuiremote.ui.components.CollapsibleNodeCard
import com.example.comfyuiremote.ui.components.DynamicInputField
import com.example.comfyuiremote.viewmodel.WorkflowViewModel

@Composable
fun WorkflowDetailScreen(
    workflowName: String,
    viewModel: WorkflowViewModel,
    onWorkflowChange: (String) -> Unit
) {
    val introspection by viewModel.introspection.collectAsState()
    val currentJob by viewModel.currentJob.collectAsState()
    val history by viewModel.history.collectAsState()
    val checkpoints by viewModel.checkpoints.collectAsState()
    val error by viewModel.error.collectAsState()

    // Local state for form inputs
    val inputValues = remember { mutableStateMapOf<String, Any>() }
    var randomizeSeed by remember { mutableStateOf(true) }
    var seedValue by remember { mutableStateOf(0L) }
    
    // Track expanded state for each node
    val expandedNodes = remember { mutableStateMapOf<String, Boolean>() }
    
    // Tab state
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(workflowName) {
        viewModel.introspectWorkflow(workflowName)
        viewModel.loadHistory()
        viewModel.loadCheckpoints()
    }

    // Initialize defaults when introspection loads
    LaunchedEffect(introspection) {
        introspection?.nodes?.forEach { node ->
            // Initialize all nodes as collapsed by default
            if (!expandedNodes.containsKey(node.id)) {
                expandedNodes[node.id] = false
            }
            
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
    ) {
        // Workflow selector dropdown
        var workflowDropdownExpanded by remember { mutableStateOf(false) }
        val workflows by viewModel.workflows.collectAsState()
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = workflowName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Workflow") },
                trailingIcon = {
                    androidx.compose.material3.IconButton(onClick = { workflowDropdownExpanded = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                            contentDescription = "Select workflow"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.material3.DropdownMenu(
                expanded = workflowDropdownExpanded,
                onDismissRequest = { workflowDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                workflows.forEach { workflow ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { 
                            Column {
                                Text(workflow.name, style = MaterialTheme.typography.bodyMedium)
                                Text(workflow.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            // Navigate to the new workflow
                            onWorkflowChange(workflow.fileName)
                            workflowDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Run button (always visible at top)
        Button(
            onClick = {
                val seedControl = if (randomizeSeed) {
                    SeedControl(mode = "random")
                } else {
                    SeedControl(mode = "fixed", value = seedValue)
                }
                viewModel.runWorkflow(workflowName, inputValues.toMap(), seedControl)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = introspection != null
        ) {
            Text("Run Workflow")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Inputs") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Results") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Error display
        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Tab content
        when (selectedTab) {
            0 -> InputsTab(
                introspection = introspection,
                expandedNodes = expandedNodes,
                inputValues = inputValues,
                randomizeSeed = randomizeSeed,
                seedValue = seedValue,
                onRandomizeSeedChange = { randomizeSeed = it },
                onSeedValueChange = { seedValue = it },
                checkpoints = checkpoints
            )
            1 -> ResultsTab(
                history = history,
                currentJob = currentJob
            )
        }
    }
}

@Composable
fun InputsTab(
    introspection: com.example.comfyuiremote.data.model.WorkflowIntrospection?,
    expandedNodes: MutableMap<String, Boolean>,
    inputValues: MutableMap<String, Any>,
    randomizeSeed: Boolean,
    seedValue: Long,
    onRandomizeSeedChange: (Boolean) -> Unit,
    onSeedValueChange: (Long) -> Unit,
    checkpoints: List<String>
) {
    if (introspection == null) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(introspection.nodes) { node ->
                if (node.inputs.isNotEmpty()) {
                    CollapsibleNodeCard(
                        node = node,
                        isExpanded = expandedNodes[node.id] ?: false,
                        onToggle = {
                            expandedNodes[node.id] = !(expandedNodes[node.id] ?: false)
                        }
                    ) {
                        node.inputs.forEach { input ->
                            if (input.isSeed) {
                                // Special handling for seed
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = randomizeSeed,
                                        onCheckedChange = onRandomizeSeedChange
                                    )
                                    Text("Randomize Seed")
                                }
                                if (!randomizeSeed) {
                                    DynamicInputField(
                                        input = input.copy(name = "Seed Value", type = "int"),
                                        value = seedValue,
                                        onValueChange = { onSeedValueChange((it as? Int)?.toLong() ?: 0L) },
                                        checkpoints = checkpoints
                                    )
                                }
                            } else {
                                val key = "${node.id}.${input.name}"
                                DynamicInputField(
                                    input = input,
                                    value = inputValues[key],
                                    onValueChange = { inputValues[key] = it },
                                    checkpoints = checkpoints
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultsTab(
    history: List<com.example.comfyuiremote.data.model.JobResponse>,
    currentJob: com.example.comfyuiremote.data.model.JobResponse?
) {
    // Combine current job and history, with current job first if it exists
    val allJobs = buildList {
        if (currentJob != null) {
            add(currentJob)
        }
        addAll(history.filter { it.jobId != currentJob?.jobId })
    }

    if (allJobs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No results yet. Run a workflow to see results here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allJobs) { job ->
                ResultImageCard(
                    job = job,
                    isCurrentJob = job.jobId == currentJob?.jobId
                )
            }
        }
    }
}

@Composable
fun ResultImageCard(
    job: com.example.comfyuiremote.data.model.JobResponse,
    isCurrentJob: Boolean
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            when (job.status) {
                "completed" -> {
                    // Show image if available
                    if (job.imageUrl != null) {
                        AsyncImage(
                            model = "http://10.0.2.2:8000${job.imageUrl}",
                            contentDescription = "Generated Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Completed but no image
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "✓ Completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "No image available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                "running" -> {
                    // Show progress indicator for running job
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                text = "Running...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isCurrentJob) {
                                Text(
                                    text = "Current Job",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                "queued" -> {
                    // Show placeholder for queued job
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Queued",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                else -> {
                    // Error or unknown status
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚠ ${job.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Status badge overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = when (job.status) {
                    "completed" -> MaterialTheme.colorScheme.primary
                    "running" -> MaterialTheme.colorScheme.secondary
                    "queued" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = job.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Job details
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Seed: ${job.resolvedSeed}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = job.workflowName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

