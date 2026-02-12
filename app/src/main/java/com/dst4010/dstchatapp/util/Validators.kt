package com.dst4010.dstchatapp.util

object Validators {
    fun requireNotBlank(value: String, field: String): String? {
        return if (value.trim().isEmpty()) "$field is required" else null
    }
}
