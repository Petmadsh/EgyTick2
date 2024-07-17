package com.example.egytick.activities

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.egytick.R
import com.example.egytick.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Set onClickListener for Reset Password Button
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
            } else {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showErrorSnackBar("Email sent successfully to reset your password.", false)
                        } else {
                            showErrorSnackBar(task.exception?.message.toString(), true)
                        }
                    }
            }
        }
    }

    private fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        val color = if (errorMessage) {
            ContextCompat.getColor(this, R.color.colorSnackBarError)
        } else {
            ContextCompat.getColor(this, R.color.colorSnackBarSuccess)
        }

        snackBarView.setBackgroundColor(color)
        snackBar.show()
    }
}
