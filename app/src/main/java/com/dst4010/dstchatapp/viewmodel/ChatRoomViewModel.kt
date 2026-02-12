package com.dst4010.dstchatapp.viewmodel

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dst4010.dstchatapp.data.ChatMessage
import com.dst4010.dstchatapp.firebase.FirebaseRepository
import com.dst4010.dstchatapp.util.Result
import kotlinx.coroutines.launch

class ChatRoomViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _sendState = MutableLiveData<Result<Unit>>()
    val sendState: LiveData<Result<Unit>> = _sendState

    private val _typingText = MutableLiveData<String?>()
    val typingText: LiveData<String?> = _typingText

    private var unsubMessages: (() -> Unit)? = null
    private var unsubTyping: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private var typingOffRunnable: Runnable? = null

    private var roomId: String? = null

    fun bindRoom(roomId: String) {
        this.roomId = roomId
    }

    fun startListening(roomId: String) {
        bindRoom(roomId)

        if (unsubMessages == null) {
            unsubMessages = repo.listenMessages(
                roomId = roomId,
                onUpdate = { _messages.postValue(it) },
                onError = { /* ignore for UI simplicity */ }
            )
        }

        if (unsubTyping == null) {
            unsubTyping = repo.listenTyping(
                roomId = roomId,
                onTyping = { uids ->
                    viewModelScope.launch {
                        if (uids.isEmpty()) {
                            _typingText.postValue(null)
                        } else {
                            // For UX, show the first typist's name.
                            val name = repo.getOrFetchDisplayName(uids.first())
                            _typingText.postValue("$name is typing…")
                        }
                    }
                },
                onError = { _typingText.postValue(null) }
            )
        }
    }

    fun stopListening() {
        val rid = roomId
        // best-effort: stop typing when leaving
        if (rid != null) {
            viewModelScope.launch { repo.setTyping(rid, false) }
        }
        unsubMessages?.invoke(); unsubMessages = null
        unsubTyping?.invoke(); unsubTyping = null
    }

    fun sendText(text: String) {
        val rid = roomId ?: return
        if (text.trim().isEmpty()) return

        _sendState.value = Result.Loading
        viewModelScope.launch {
            _sendState.value = repo.sendText(rid, text)
        }
    }

    fun sendImage(uri: Uri) {
        val rid = roomId ?: return
        _sendState.value = Result.Loading
        viewModelScope.launch {
            _sendState.value = repo.sendImage(rid, uri)
        }
    }

    fun onUserTyping() {
        val rid = roomId ?: return
        // Set typing on immediately.
        viewModelScope.launch { repo.setTyping(rid, true) }

        // Debounce: turn off typing after 1500ms of inactivity.
        typingOffRunnable?.let { handler.removeCallbacks(it) }
        typingOffRunnable = Runnable {
            viewModelScope.launch { repo.setTyping(rid, false) }
        }
        handler.postDelayed(typingOffRunnable!!, 1500)
    }
}
