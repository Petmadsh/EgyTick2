package com.example.egytick

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.egytick.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = firebaseAuth.currentUser!!

        loadUserProfile()

        binding.btnUpdate.setOnClickListener {
            if (validateProfileDetails()) {
                updateUserProfile()
            }
        }
    }

    private fun loadUserProfile() {
        val userId = currentUser.uid

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.etFirstName.setText(document.getString("firstName"))
                    binding.etLastName.setText(document.getString("lastName"))
                    binding.etEmail.setText(currentUser.email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etFirstName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please enter first name.", true)
                false
            }
            TextUtils.isEmpty(binding.etLastName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please enter last name.", true)
                false
            }
            TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please enter password.", true)
                false
            }
            TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please confirm password.", true)
                false
            }
            binding.etPassword.text.toString().trim { it <= ' ' } != binding.etConfirmPassword.text.toString().trim { it <= ' ' } -> {
                showErrorSnackBar("Passwords do not match.", true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun updateUserProfile() {
        val firstName = binding.etFirstName.text.toString().trim { it <= ' ' }
        val lastName = binding.etLastName.text.toString().trim { it <= ' ' }
        val password = binding.etPassword.text.toString().trim { it <= ' ' }

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
        )

        firestore.collection("users").document(currentUser.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                currentUser.updatePassword(password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("Profile updated successfully.", false)
                    } else {
                        showErrorSnackBar("Error updating password: ${task.exception?.message}", true)
                    }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error updating profile: ${e.message}", true)
            }
    }

    private fun showErrorSnackBar(message: String, isError: Boolean) {
        // Implement your snackbar or toast here
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
