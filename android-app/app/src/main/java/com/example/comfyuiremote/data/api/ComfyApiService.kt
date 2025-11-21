package com.example.comfyuiremote.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface ComfyApiService {
    // TODO: Define response models

    @GET("/workflows")
    suspend fun getWorkflows(): List<Any> // Replace Any with WorkflowSummary model

    @GET("/workflows/{id}")
    suspend fun getWorkflow(@Path("id") id: String): Any // Replace Any with WorkflowDetail model

    // TODO: Add other endpoints:
    // POST /jobs/start
    // POST /jobs/{job_id}/stop
    // GET /jobs/{job_id}
    // GET /jobs/{job_id}/images
}
