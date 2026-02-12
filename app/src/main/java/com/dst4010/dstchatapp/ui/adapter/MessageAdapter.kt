package com.dst4010.dstchatapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dst4010.dstchatapp.data.ChatMessage
import com.dst4010.dstchatapp.databinding.ItemMessageImageReceivedBinding
import com.dst4010.dstchatapp.databinding.ItemMessageImageSentBinding
import com.dst4010.dstchatapp.databinding.ItemMessageTextReceivedBinding
import com.dst4010.dstchatapp.databinding.ItemMessageTextSentBinding
import com.dst4010.dstchatapp.firebase.FirebaseRepository

class MessageAdapter(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(Diff) {

    companion object {
        private const val VT_TEXT_SENT = 1
        private const val VT_TEXT_RECEIVED = 2
        private const val VT_IMAGE_SENT = 3
        private const val VT_IMAGE_RECEIVED = 4
    }

    object Diff : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        val isMe = msg.senderId == repo.currentUid()
        return when (msg.type) {
            ChatMessage.TYPE_IMAGE -> if (isMe) VT_IMAGE_SENT else VT_IMAGE_RECEIVED
            else -> if (isMe) VT_TEXT_SENT else VT_TEXT_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TEXT_SENT -> TextSentVH(ItemMessageTextSentBinding.inflate(inflater, parent, false))
            VT_TEXT_RECEIVED -> TextReceivedVH(ItemMessageTextReceivedBinding.inflate(inflater, parent, false))
            VT_IMAGE_SENT -> ImageSentVH(ItemMessageImageSentBinding.inflate(inflater, parent, false))
            VT_IMAGE_RECEIVED -> ImageReceivedVH(ItemMessageImageReceivedBinding.inflate(inflater, parent, false))
            else -> TextReceivedVH(ItemMessageTextReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is TextSentVH -> holder.binding.messageText.text = msg.text ?: ""
            is TextReceivedVH -> {
                holder.binding.senderNameText.text = msg.senderName
                holder.binding.messageText.text = msg.text ?: ""
            }
            is ImageSentVH -> {
                Glide.with(holder.binding.messageImage)
                    .load(msg.imageUrl)
                    .centerCrop()
                    .into(holder.binding.messageImage)
            }
            is ImageReceivedVH -> {
                holder.binding.senderNameText.text = msg.senderName
                Glide.with(holder.binding.messageImage)
                    .load(msg.imageUrl)
                    .centerCrop()
                    .into(holder.binding.messageImage)
            }
        }
    }

    class TextSentVH(val binding: ItemMessageTextSentBinding) : RecyclerView.ViewHolder(binding.root)
    class TextReceivedVH(val binding: ItemMessageTextReceivedBinding) : RecyclerView.ViewHolder(binding.root)
    class ImageSentVH(val binding: ItemMessageImageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ImageReceivedVH(val binding: ItemMessageImageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
}
