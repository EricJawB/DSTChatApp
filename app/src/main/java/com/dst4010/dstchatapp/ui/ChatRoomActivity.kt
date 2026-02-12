package com.dst4010.dstchatapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dst4010.dstchatapp.databinding.ActivityChatRoomBinding
import com.dst4010.dstchatapp.ui.adapter.MessageAdapter
import com.dst4010.dstchatapp.util.Result
import com.dst4010.dstchatapp.viewmodel.ChatRoomViewModel

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_ROOM_NAME = "room_name"
    }

    private lateinit var binding: ActivityChatRoomBinding
    private val vm: ChatRoomViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.uploadProgress.visibility = View.VISIBLE
            vm.sendImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: run { finish(); return }
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: "Chat"

        binding.toolbar.title = roomName
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MessageAdapter()
        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = lm
        binding.messagesRecyclerView.adapter = adapter

        vm.startListening(roomId)

        binding.sendButton.setOnClickListener {
            val text = binding.messageEditText.text.toString()
            vm.sendText(text)
            binding.messageEditText.setText("")
        }

        binding.imageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) vm.onUserTyping()
            }
        })

        vm.messages.observe(this) { msgs ->
            adapter.submitList(msgs) {
                if (msgs.isNotEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(msgs.size - 1)
                }
            }
        }

        vm.typingText.observe(this) { txt ->
            if (txt.isNullOrBlank()) {
                binding.typingIndicator.visibility = View.GONE
            } else {
                binding.typingIndicator.text = txt
                binding.typingIndicator.visibility = View.VISIBLE
            }
        }

        vm.sendState.observe(this) { state ->
            if (state !is Result.Loading) {
                binding.uploadProgress.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.stopListening()
    }
}
