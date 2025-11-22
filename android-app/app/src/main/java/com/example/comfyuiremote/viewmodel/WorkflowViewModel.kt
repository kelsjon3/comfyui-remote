package com.example.comfyuiremote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.comfyuiremote.ComfyRemoteApp
import com.example.comfyuiremote.data.model.JobStatus
import com.example.comfyuiremote.data.model.Workflow
import com.example.comfyuiremote.data.repository.ComfyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkflowViewModel(private val repository: ComfyRepository) : ViewModel() {

    private val _workflows = MutableStateFlow<List<Workflow>>(emptyList())
    val workflows: StateFlow<List<Workflow>> = _workflows.asStateFlow()

    private val _currentJobStatus = MutableStateFlow<JobStatus?>(null)
    val currentJobStatus: StateFlow<JobStatus?> = _currentJobStatus.asStateFlow()
    
    private val _jobImages = MutableStateFlow<List<String>>(emptyList())
    val jobImages: StateFlow<List<String>> = _jobImages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun startJob(workflowId: String) {
        viewModelScope.launch {
            val result = repository.startJob(workflowId)
            result.onSuccess { response ->
                pollJobStatus(response.jobId)
            }
            result.onFailure { /* TODO: Handle error */ }
        }
    }

    private fun pollJobStatus(jobId: String) {
        viewModelScope.launch {
            while (true) {
                val result = repository.getJobStatus(jobId)
                result.onSuccess { status ->
                    _currentJobStatus.value = status
                    if (status.status == "completed") {
                        loadJobImages(jobId)
                        return@launch
                    }
                }
                delay(1000) // Poll every second
            }
        }
    }
    
    private fun loadJobImages(jobId: String) {
        viewModelScope.launch {
            val result = repository.getJobImages(jobId)
            result.onSuccess { _jobImages.value = it }
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
