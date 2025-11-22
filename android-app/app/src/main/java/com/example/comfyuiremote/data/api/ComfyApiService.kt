package com.example.comfyuiremote.data.api

import com.example.comfyuiremote.data.model.CheckpointsResponse
import com.example.comfyuiremote.data.model.JobResponse
import com.example.comfyuiremote.data.model.LorasResponse
import com.example.comfyuiremote.data.model.RunWorkflowRequest
import com.example.comfyuiremote.data.model.Workflow
import com.example.comfyuiremote.data.model.WorkflowIntrospection
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ComfyApiService {

    @GET("/workflows")
    suspend fun getWorkflows(): List<Workflow>

    @GET("/workflow/{name}")
    suspend fun getWorkflow(@Path("name") name: String): Any

    @GET("/workflow/{name}/introspect")
    suspend fun introspectWorkflow(@Path("name") name: String): WorkflowIntrospection

    @POST("/run")
    suspend fun runWorkflow(@Body request: RunWorkflowRequest): JobResponse

    @GET("/history")
    suspend fun getHistory(): List<JobResponse>
    
    @GET("/checkpoints")
    suspend fun getCheckpoints(): CheckpointsResponse
    
    @GET("/loras")
    suspend fun getLoras(): LorasResponse
}
