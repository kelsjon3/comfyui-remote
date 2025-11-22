package com.example.comfyuiremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comfyuiremote.ui.screens.WorkflowDetailScreen
import com.example.comfyuiremote.ui.screens.WorkflowListScreen
import com.example.comfyuiremote.ui.theme.ComfyUIRemoteTheme
import com.example.comfyuiremote.viewmodel.WorkflowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComfyUIRemoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WorkflowViewModel = viewModel(factory = WorkflowViewModel.Factory)
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun AppNavigation(viewModel: WorkflowViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "workflow_list") {
        composable("workflow_list") {
            WorkflowListScreen(
                viewModel = viewModel,
                onWorkflowClick = { workflowId ->
                    navController.navigate("workflow_detail/$workflowId")
                }
            )
        }
        composable("workflow_detail/{workflowId}") { backStackEntry ->
            val workflowId = backStackEntry.arguments?.getString("workflowId") ?: return@composable
            WorkflowDetailScreen(
                workflowId = workflowId,
                viewModel = viewModel
            )
        }
    }
}
