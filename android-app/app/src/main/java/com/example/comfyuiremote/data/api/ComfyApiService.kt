package com.example.comfyuiremote.data.api

import com.example.comfyuiremote.data.model.JobStartResponse
import com.example.comfyuiremote.data.model.JobStatus
import com.example.comfyuiremote.data.model.Workflow
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ComfyApiService {

    @GET("/workflows")
    suspend fun getWorkflows(): List<Workflow>

    @GET("/workflows/{id}")
    suspend fun getWorkflow(@Path("id") id: String): Any // Keeping Any for raw JSON for now

    @POST("/jobs/start")
    suspend fun startJob(@Query("workflow_id") workflowId: String): JobStartResponse

    @POST("/jobs/{job_id}/stop")
    suspend fun stopJob(@Path("job_id") jobId: String): JobStatus

    @GET("/jobs/{job_id}")
    suspend fun getJobStatus(@Path("job_id") jobId: String): JobStatus

    @GET("/jobs/{job_id}/images")
    suspend fun getJobImages(@Path("job_id") jobId: String): List<String>
}
