package com.example.egytick.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.enableEdgeToEdge
import com.example.egytick.R
import com.example.egytick.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbarRegisterActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        }

        binding.toolbarRegisterActivity.setNavigationOnClickListener { onBackPressed() }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            if (validateRegisterDetails()) {
                registerUser()
            }
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
                showErrorSnackBar(resources.getString(R.string.succ_msg_register), false)
                true
            }
        }
    }

    private fun registerUser() {
        // Get the text from edit text and trim the space
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }
        val firstName: String = binding.etFirstName.text.toString().trim { it <= ' ' }
        val lastName: String = binding.etLastName.text.toString().trim { it <= ' ' }

        // Create an instance and create a register a user with email and password.
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // If the registration is successfully done
                if (task.isSuccessful) {
                    // Firebase registered user
                    val firebaseUser = task.result?.user
                    val registeredEmail = firebaseUser?.email

                    showErrorSnackBar("You are registered successfully.", false)

                    // Save user data in Firestore
                    val user = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email
                    )

                    firebaseUser?.uid?.let {
                        FirebaseFirestore.getInstance().collection("users").document(it)
                            .set(user)
                            .addOnSuccessListener {
                                // Navigate to the main activity
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", registeredEmail)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showErrorSnackBar("Failed to save user data: ${e.message}", true)
                            }
                    }
                } else {
                    // If the registering is not successful then show an error message.
                    showErrorSnackBar(task.exception?.message.toString(), true)
                }
            }
    }

}
