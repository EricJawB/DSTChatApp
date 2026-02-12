package com.dst4010.dstchatapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dst4010.dstchatapp.data.ChatRoom
import com.dst4010.dstchatapp.databinding.ItemRoomBinding

class RoomAdapter(
    private val onClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, RoomAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean = oldItem == newItem
    }

    class VH(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val room = getItem(position)
        holder.binding.roomNameText.text = room.name
        holder.binding.root.setOnClickListener { onClick(room) }
    }
}
