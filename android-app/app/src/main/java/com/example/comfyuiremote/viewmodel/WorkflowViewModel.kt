package com.example.comfyuiremote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WorkflowViewModel : ViewModel() {
    // TODO: Inject Repository

    fun loadWorkflows() {
        viewModelScope.launch {
            // TODO: Call repository to fetch workflows
        }
    }

    fun loadWorkflowDetails(id: String) {
        viewModelScope.launch {
            // TODO: Call repository to fetch workflow details
        }
    }
}
