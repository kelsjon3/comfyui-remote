package com.example.comfyuiremote.data.model

import com.google.gson.annotations.SerializedName

data class JobStatus(
    @SerializedName("job_id") val jobId: String,
    val status: String,
    val details: Any? = null // Can be expanded later
)
