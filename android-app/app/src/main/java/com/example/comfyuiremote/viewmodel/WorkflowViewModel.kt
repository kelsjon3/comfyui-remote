package com.example.comfyuiremote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.comfyuiremote.ComfyRemoteApp
import com.example.comfyuiremote.data.model.JobResponse
import com.example.comfyuiremote.data.model.RunWorkflowRequest
import com.example.comfyuiremote.data.model.SeedControl
import com.example.comfyuiremote.data.model.Workflow
import com.example.comfyuiremote.data.model.WorkflowIntrospection
import com.example.comfyuiremote.data.repository.ComfyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkflowViewModel(private val repository: ComfyRepository) : ViewModel() {

    private val _workflows = MutableStateFlow<List<Workflow>>(emptyList())
    val workflows: StateFlow<List<Workflow>> = _workflows.asStateFlow()

    private val _introspection = MutableStateFlow<WorkflowIntrospection?>(null)
    val introspection: StateFlow<WorkflowIntrospection?> = _introspection.asStateFlow()

    private val _currentJob = MutableStateFlow<JobResponse?>(null)
    val currentJob: StateFlow<JobResponse?> = _currentJob.asStateFlow()
    
    private val _history = MutableStateFlow<List<JobResponse>>(emptyList())
    val history: StateFlow<List<JobResponse>> = _history.asStateFlow()

    private val _checkpoints = MutableStateFlow<List<String>>(emptyList())
    val checkpoints: StateFlow<List<String>> = _checkpoints.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCheckpoints() {
        viewModelScope.launch {
            val result = repository.getCheckpoints()
            result.onSuccess { _checkpoints.value = it }
        }
    }

    fun loadWorkflows() {
        viewModelScope.launch {
            _error.value = null
            val result = repository.getWorkflows()
            result.onSuccess { _workflows.value = it }
            result.onFailure { 
                _error.value = it.message ?: "Unknown error loading workflows"
                it.printStackTrace()
            }
        }
    }

    fun introspectWorkflow(name: String) {
        viewModelScope.launch {
            _error.value = null
            _introspection.value = null
            val result = repository.introspectWorkflow(name)
            result.onSuccess { _introspection.value = it }
            result.onFailure {
                _error.value = "Failed to introspect: ${it.message}"
            }
        }
    }

    fun runWorkflow(workflowName: String, inputs: Map<String, Any>, seedControl: SeedControl) {
        viewModelScope.launch {
            _error.value = null
            val request = RunWorkflowRequest(workflowName, inputs, seedControl)
            val result = repository.runWorkflow(request)
            result.onSuccess { job ->
                _currentJob.value = job
                pollJobStatus(job.jobId)
            }
            result.onFailure {
                _error.value = "Failed to start job: ${it.message}"
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            val result = repository.getHistory()
            result.onSuccess { _history.value = it }
        }
    }

    private fun pollJobStatus(jobId: String) {
        viewModelScope.launch {
            while (true) {
                val result = repository.getHistory()
                result.onSuccess { jobs ->
                    val job = jobs.find { it.jobId == jobId }
                    if (job != null) {
                        _currentJob.value = job
                        if (job.status == "completed" || job.status == "failed") {
                            loadHistory() // Refresh full history
                            return@launch
                        }
                    }
                }
                delay(1000) // Poll every second
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ComfyRemoteApp)
                WorkflowViewModel(application.repository)
            }
        }
    }
}
