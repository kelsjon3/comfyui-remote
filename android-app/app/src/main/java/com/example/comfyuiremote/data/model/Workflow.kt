package com.example.comfyuiremote.data.model

import com.google.gson.annotations.SerializedName

data class Workflow(
    @SerializedName("file_name") val fileName: String,
    val name: String,
    @SerializedName("last_modified") val lastModified: String
)
