package com.dst4010.dstchatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dst4010.dstchatapp.firebase.FirebaseRepository
import com.dst4010.dstchatapp.util.Result
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _state = MutableLiveData<Result<Unit>>()
    val state: LiveData<Result<Unit>> = _state

    fun signIn(email: String, password: String) {
        _state.value = Result.Loading
        viewModelScope.launch {
            _state.value = repo.signIn(email, password)
        }
    }

    fun register(displayName: String, email: String, password: String) {
        _state.value = Result.Loading
        viewModelScope.launch {
            _state.value = repo.register(displayName, email, password)
        }
    }

    fun isSignedIn(): Boolean = repo.isSignedIn()
}
