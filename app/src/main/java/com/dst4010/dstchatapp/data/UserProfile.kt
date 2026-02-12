package com.dst4010.dstchatapp.data

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Timestamp? = null
)
