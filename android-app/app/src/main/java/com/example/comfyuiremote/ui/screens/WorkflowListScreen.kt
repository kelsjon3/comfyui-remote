package com.example.comfyuiremote.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comfyuiremote.viewmodel.WorkflowViewModel

@Composable
fun WorkflowListScreen(
    viewModel: WorkflowViewModel,
    onWorkflowClick: (String) -> Unit
) {
    val workflows by viewModel.workflows.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadWorkflows()
    }

    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else if (workflows.isEmpty()) {
        // Show loading or empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No workflows found or loading...")
        }
    } else {
        LazyColumn {
            items(workflows) { workflow ->
                WorkflowItem(workflow) {
                    onWorkflowClick(workflow.fileName)
                }
            }
        }
    }
}

@Composable
fun WorkflowItem(
    workflow: com.example.comfyuiremote.data.model.Workflow,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = workflow.name, style = MaterialTheme.typography.titleMedium)
            // Text(text = workflow.description, style = MaterialTheme.typography.bodyMedium) // Description removed from model
            Text(text = workflow.fileName, style = MaterialTheme.typography.bodySmall)
        }
    }
}
