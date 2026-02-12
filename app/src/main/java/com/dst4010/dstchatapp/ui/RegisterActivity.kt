package com.dst4010.dstchatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dst4010.dstchatapp.databinding.ActivityRegisterBinding
import com.dst4010.dstchatapp.util.Result
import com.dst4010.dstchatapp.util.Validators
import com.dst4010.dstchatapp.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            Validators.requireNotBlank(name, "Display name")?.let { showError(it); return@setOnClickListener }
            Validators.requireNotBlank(email, "Email")?.let { showError(it); return@setOnClickListener }
            Validators.requireNotBlank(password, "Password")?.let { showError(it); return@setOnClickListener }

            vm.register(name, email, password)
        }

        binding.backToLoginButton.setOnClickListener {
            finish()
        }

        vm.state.observe(this) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE

                    val intent = Intent(this, ChatListActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
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
}
