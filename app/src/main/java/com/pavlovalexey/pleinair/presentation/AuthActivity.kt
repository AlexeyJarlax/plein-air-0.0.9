package com.pavlovalexey.pleinair.presentation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.pavlovalexey.pleinair.R

class AuthActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val signInButton: Button = findViewById(R.id.signInButton)
        val toggleButton: Button = findViewById(R.id.toggleButton)
        val statusTextView: TextView = findViewById(R.id.statusTextView)
        val errorTextView: TextView = findViewById(R.id.errorTextView)

        // Observe ViewModel LiveData or State
        viewModel.email.observe(this) { email ->
            emailEditText.setText(email)
        }

        viewModel.password.observe(this) { password ->
            passwordEditText.setText(password)
        }

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            statusTextView.text = if (isLoggedIn) "You are logged in!" else ""
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorTextView.text = errorMessage
        }

        emailEditText.addTextChangedListener {
            viewModel.onEmailChanged(it.toString())
        }

        passwordEditText.addTextChangedListener {
            viewModel.onPasswordChanged(it.toString())
        }

        signInButton.setOnClickListener {
            if (viewModel.isLoginScreen.value == true) {
                viewModel.signIn()
            } else {
                viewModel.register()
            }
        }

        toggleButton.setOnClickListener {
            viewModel.isLoginScreen.value = !viewModel.isLoginScreen.value!!
            val buttonText = if (viewModel.isLoginScreen.value!!) "Register" else "Login"
            signInButton.text = buttonText
            toggleButton.text = if (viewModel.isLoginScreen.value!!) "Don't have an account? Register" else "Already have an account? Login"
        }
    }
}