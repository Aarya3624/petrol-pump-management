package com.aarya.csaassistant.utils

import java.util.UUID

data class Employee(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val photoUrl: String? = null
)