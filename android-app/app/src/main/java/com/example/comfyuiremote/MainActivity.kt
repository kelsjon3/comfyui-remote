package com.example.comfyuiremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comfyuiremote.ui.theme.ComfyUIRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComfyUIRemoteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "workflow_list") {
        composable("workflow_list") {
            WorkflowListScreen(
                onWorkflowClick = { workflowId ->
                    navController.navigate("workflow_detail/$workflowId")
                }
            )
        }
        composable("workflow_detail/{workflowId}") { backStackEntry ->
            val workflowId = backStackEntry.arguments?.getString("workflowId")
            WorkflowDetailScreen(workflowId = workflowId)
        }
        // TODO: Add other screens: Node Editor, Job Status, Image Gallery
    }
}

@Composable
fun WorkflowListScreen(onWorkflowClick: (String) -> Unit) {
    Text(text = "Workflow List Screen Placeholder")
    // TODO: Display list of workflows
}

@Composable
fun WorkflowDetailScreen(workflowId: String?) {
    Text(text = "Workflow Detail Screen for $workflowId")
    // TODO: Display workflow details and nodes
}
