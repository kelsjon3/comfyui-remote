package com.example.comfyuiremote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.comfyuiremote.viewmodel.WorkflowViewModel

@Composable
fun WorkflowDetailScreen(
    workflowId: String,
    viewModel: WorkflowViewModel
) {
    val jobStatus by viewModel.currentJobStatus.collectAsState()
    val jobImages by viewModel.jobImages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Workflow: $workflowId", style = MaterialTheme.typography.headlineSmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { viewModel.startJob(workflowId) }) {
            Text("Start Job")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        jobStatus?.let { status ->
            Text(text = "Status: ${status.status}", style = MaterialTheme.typography.bodyLarge)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (jobImages.isNotEmpty()) {
            Text(text = "Generated Images:", style = MaterialTheme.typography.titleMedium)
            jobImages.forEach { imageName ->
                // Text(text = imageName) // Removed text display
                
                // Display image using Coil
                // 10.0.2.2 is localhost for Android Emulator
                val imageUrl = "http://10.0.2.2:8000/jobs/${jobStatus?.jobId}/images/$imageName"
                
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
