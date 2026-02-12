package com.dst4010.dstchatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dst4010.dstchatapp.databinding.ActivityLoginBinding
import com.dst4010.dstchatapp.util.Result
import com.dst4010.dstchatapp.util.Validators
import com.dst4010.dstchatapp.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (vm.isSignedIn()) {
            goToRooms()
            return
        }

        binding.loginButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            Validators.requireNotBlank(email, "Email")?.let { showError(it); return@setOnClickListener }
            Validators.requireNotBlank(password, "Password")?.let { showError(it); return@setOnClickListener }

            vm.signIn(email, password)
        }

        binding.goToRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        vm.state.observe(this) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    goToRooms()
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    showError(state.message)
                }
            }
        }
    }

    private fun showError(msg: String) {
        binding.errorText.text = msg
        binding.errorText.visibility = View.VISIBLE
    }

    private fun goToRooms() {
        startActivity(Intent(this, ChatListActivity::class.java))
        finish()
    }
}
