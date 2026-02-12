package com.dst4010.dstchatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dst4010.dstchatapp.data.ChatRoom
import com.dst4010.dstchatapp.firebase.FirebaseRepository
import com.dst4010.dstchatapp.util.Result
import kotlinx.coroutines.launch

class RoomsViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _rooms = MutableLiveData<List<ChatRoom>>(emptyList())
    val rooms: LiveData<List<ChatRoom>> = _rooms

    private val _createRoomState = MutableLiveData<Result<Unit>>()
    val createRoomState: LiveData<Result<Unit>> = _createRoomState

    private var unsubscribe: (() -> Unit)? = null

    fun startListening() {
        if (unsubscribe != null) return
        unsubscribe = repo.listenRooms(
            onUpdate = { _rooms.postValue(it) },
            onError = { /* ignore for UI simplicity */ }
        )
    }

    fun stopListening() {
        unsubscribe?.invoke()
        unsubscribe = null
    }

    fun createRoom(name: String) {
        _createRoomState.value = Result.Loading
        viewModelScope.launch {
            _createRoomState.value = repo.createRoom(name)
        }
    }
}
