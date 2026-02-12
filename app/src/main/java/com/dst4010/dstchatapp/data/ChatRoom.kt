package com.dst4010.dstchatapp.data

import com.google.firebase.Timestamp

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val createdBy: String = ""
)
