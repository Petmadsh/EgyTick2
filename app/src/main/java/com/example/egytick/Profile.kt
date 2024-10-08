package com.example.egytick

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.egytick.activities.LoginActivity
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
            confirmUpdateProfile()
        }

        binding.tvChangePassword.setOnClickListener {
            togglePasswordFields()
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout()
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

    private fun confirmUpdateProfile() {
        AlertDialog.Builder(requireContext())
            .setTitle("Update Profile")
            .setMessage("Are you sure you want to update your profile?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                if (validateProfileDetails()) {
                    updateUserProfile()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                logoutUser()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
            TextUtils.isEmpty(binding.etEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please enter email.", true)
                false
            }
            binding.etPassword.visibility == View.VISIBLE && TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please enter password.", true)
                false
            }
            binding.etPassword.visibility == View.VISIBLE && TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Please confirm password.", true)
                false
            }
            binding.etPassword.visibility == View.VISIBLE && binding.etPassword.text.toString().trim { it <= ' ' } != binding.etConfirmPassword.text.toString().trim { it <= ' ' } -> {
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
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etPassword.text.toString().trim { it <= ' ' }

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )

        val currentEmail = currentUser.email

        firestore.collection("users").document(currentUser.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                if (email != currentEmail) {
                    currentUser.updateEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUserBookings(currentEmail, email)
                            if (binding.etPassword.visibility == View.VISIBLE) {
                                currentUser.updatePassword(password).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showErrorSnackBar("Profile updated successfully.", false)
                                    } else {
                                        showErrorSnackBar("Error updating password: ${task.exception?.message}", true)
                                    }
                                }
                            } else {
                                showErrorSnackBar("Profile updated successfully.", false)
                            }
                        } else {
                            showErrorSnackBar("Error updating email: ${task.exception?.message}", true)
                        }
                    }
                } else {
                    if (binding.etPassword.visibility == View.VISIBLE) {
                        currentUser.updatePassword(password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showErrorSnackBar("Profile updated successfully.", false)
                            } else {
                                showErrorSnackBar("Error updating password: ${task.exception?.message}", true)
                            }
                        }
                    } else {
                        showErrorSnackBar("Profile updated successfully.", false)
                    }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error updating profile: ${e.message}", true)
            }
    }

    private fun updateUserBookings(currentEmail: String?, newEmail: String) {
        if (currentEmail == null) return

        firestore.collection("bookings").whereEqualTo("email", currentEmail).get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    firestore.collection("bookings").document(document.id)
                        .update("email", newEmail)
                        .addOnFailureListener { e ->
                            showErrorSnackBar("Error updating booking: ${e.message}", true)
                        }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error loading bookings: ${e.message}", true)
            }
    }

    private fun togglePasswordFields() {
        if (binding.etPassword.visibility == View.GONE) {
            binding.etPassword.visibility = View.VISIBLE
            binding.etConfirmPassword.visibility = View.VISIBLE
        } else {
            binding.etPassword.visibility = View.GONE
            binding.etConfirmPassword.visibility = View.GONE
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showErrorSnackBar(message: String, isError: Boolean) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
