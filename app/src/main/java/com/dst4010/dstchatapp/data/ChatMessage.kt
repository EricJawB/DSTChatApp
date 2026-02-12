package com.dst4010.dstchatapp.data

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: String = TYPE_TEXT, // "text" or "image"
    val text: String? = null,
    val imageUrl: String? = null,
    val sentAt: Timestamp? = null
) {
    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
    }
}
