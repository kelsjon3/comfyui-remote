package com.example.comfyuiremote.data.model

import com.google.gson.annotations.SerializedName

data class JobStartResponse(
    @SerializedName("job_id") val jobId: String,
    val status: String
)
