package com.example.egytick.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
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

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
            } else {
                Log.d("ForgotPasswordActivity", "Attempting to send reset email")
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ForgotPasswordActivity", "Email sent successfully")
                            showErrorSnackBar("Email sent successfully to reset your password.", false)

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }, 2500)
                        } else {
                            Log.e("ForgotPasswordActivity", "Error: ${task.exception?.message}")
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
