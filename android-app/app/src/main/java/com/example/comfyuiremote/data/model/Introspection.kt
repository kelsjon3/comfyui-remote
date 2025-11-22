package com.example.comfyuiremote.data.model

import com.google.gson.annotations.SerializedName

data class WorkflowNodeInput(
    val name: String,
    val type: String,
    val default: Any? = null,
    @SerializedName("is_seed") val isSeed: Boolean = false
)

data class WorkflowNode(
    val id: String,
    val type: String,
    val label: String?,
    val inputs: List<WorkflowNodeInput>
)

data class WorkflowIntrospection(
    val nodes: List<WorkflowNode>
)
