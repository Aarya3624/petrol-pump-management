package com.aarya.csaassistant.model


import kotlinx.serialization.Contextual
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class UserProfile @OptIn(ExperimentalTime::class) constructor(
    val id: String,

    @Contextual
    val updated_at: Instant?,
    val username: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val website: String? = null
)