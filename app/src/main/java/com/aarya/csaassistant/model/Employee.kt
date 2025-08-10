package com.aarya.csaassistant.model

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val full_name: String,
    val avatar_url: String?,
    val role: String
)
