package com.example.comfyuiremote.data.repository

import com.example.comfyuiremote.data.api.ComfyApiService
import com.example.comfyuiremote.data.model.JobResponse
import com.example.comfyuiremote.data.model.RunWorkflowRequest
import com.example.comfyuiremote.data.model.Workflow
import com.example.comfyuiremote.data.model.WorkflowIntrospection

class ComfyRepository(private val apiService: ComfyApiService) {

    suspend fun getWorkflows(): Result<List<Workflow>> {
        return try {
            Result.success(apiService.getWorkflows())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun introspectWorkflow(name: String): Result<WorkflowIntrospection> {
        return try {
            Result.success(apiService.introspectWorkflow(name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun runWorkflow(request: RunWorkflowRequest): Result<JobResponse> {
        return try {
            Result.success(apiService.runWorkflow(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<List<JobResponse>> {
        return try {
            Result.success(apiService.getHistory())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCheckpoints(): Result<List<String>> {
        return try {
            Result.success(apiService.getCheckpoints().checkpoints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLoras(): Result<List<String>> {
        return try {
            Result.success(apiService.getLoras().loras)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
