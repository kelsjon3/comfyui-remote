package com.example.comfyuiremote.data.model

import com.google.gson.annotations.SerializedName

data class SeedControl(
    val mode: String = "random", // "fixed" or "random"
    val value: Long? = null
)

data class RunWorkflowRequest(
    @SerializedName("workflow_name") val workflowName: String,
    val inputs: Map<String, Any> = emptyMap(),
    @SerializedName("seed_control") val seedControl: SeedControl = SeedControl()
)

data class JobResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("workflow_name") val workflowName: String,
    val status: String,
    @SerializedName("resolved_inputs") val resolvedInputs: Map<String, Any>,
    @SerializedName("resolved_seed") val resolvedSeed: Long,
    @SerializedName("image_url") val imageUrl: String? = null
)
