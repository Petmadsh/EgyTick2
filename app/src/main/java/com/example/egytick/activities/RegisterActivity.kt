package com.example.egytick.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.enableEdgeToEdge
import com.example.egytick.R
import com.example.egytick.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        binding.tvLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            if (validateRegisterDetails()) {
                registerUser()
            }
        }

        binding.tilPassword.setEndIconOnClickListener {
            togglePasswordVisibility(binding.etPassword)
        }
        binding.tilConfirmPassword.setEndIconOnClickListener {
            togglePasswordVisibility(binding.etConfirmPassword)
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

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etFirstName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }
            TextUtils.isEmpty(binding.etLastName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }
            TextUtils.isEmpty(binding.etEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }
            binding.etPassword.text.toString().trim { it <= ' ' } != binding.etConfirmPassword.text.toString().trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }
        val firstName: String = binding.etFirstName.text.toString().trim { it <= ' ' }
        val lastName: String = binding.etLastName.text.toString().trim { it <= ' ' }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user

                    firebaseUser?.let {
                        sendEmailVerification(firebaseUser)
                    }

                } else {
                    showErrorSnackBar(task.exception?.message.toString(), true)
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showErrorSnackBar("Verification email sent to ${user.email}. Please verify your email before logging in.", false)

                    saveUserData(user)
                } else {
                    showErrorSnackBar("Failed to send verification email.", true)
                }
            }
    }

    private fun saveUserData(user: FirebaseUser) {
        val firstName: String = binding.etFirstName.text.toString().trim { it <= ' ' }
        val lastName: String = binding.etLastName.text.toString().trim { it <= ' ' }
        val email: String? = user.email

        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )

        user.uid.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Failed to save user data: ${e.message}", true)
                }
        }
    }
}
