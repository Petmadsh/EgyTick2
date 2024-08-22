package com.example.egytick.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.egytick.R
import com.example.egytick.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Set onClickListener for Register TextView
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for Login Button
        binding.btnLogin.setOnClickListener {
            if (validateLoginDetails()) {
                loginUser()
            }
        }

        // Setup password visibility toggle
        binding.tilPassword.setEndIconOnClickListener {
            togglePasswordVisibility(binding.etPassword)
        }
    }

    private fun togglePasswordVisibility(editText: com.example.egytick.utils.EditText) {
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        editText.text?.let {
            editText.setSelection(it.length)
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etPassword.text.toString().trim { it <= ' ' }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null && firebaseUser.isEmailVerified) {
                        // If email is verified, proceed to MainActivity
                        showErrorSnackBar("You are logged in successfully.", false)

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("user_id", firebaseUser.uid)
                        intent.putExtra("email_id", firebaseUser.email)
                        startActivity(intent)
                        finish()
                    } else {
                        // If email is not verified, show a message and sign the user out
                        showErrorSnackBar("Please verify your email address before logging in.", true)
                        FirebaseAuth.getInstance().signOut()
                    }
                } else {
                    showErrorSnackBar(task.exception?.message.toString(), true)
                }
            }
    }

    private fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        if (errorMessage) {
            snackBarView.setBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorSnackBarError))
        } else {
            snackBarView.setBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorSnackBarSuccess))
        }

        snackBar.show()
    }
}
