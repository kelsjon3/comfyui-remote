package com.example.comfyuiremote.data.repository

import com.example.comfyuiremote.data.api.ComfyApiService
import com.example.comfyuiremote.data.model.JobStartResponse
import com.example.comfyuiremote.data.model.JobStatus
import com.example.comfyuiremote.data.model.Workflow

class ComfyRepository(private val apiService: ComfyApiService) {

    suspend fun getWorkflows(): Result<List<Workflow>> {
        return try {
            Result.success(apiService.getWorkflows())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startJob(workflowId: String): Result<JobStartResponse> {
        return try {
            Result.success(apiService.startJob(workflowId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobStatus(jobId: String): Result<JobStatus> {
        return try {
            Result.success(apiService.getJobStatus(jobId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getJobImages(jobId: String): Result<List<String>> {
        return try {
            Result.success(apiService.getJobImages(jobId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
