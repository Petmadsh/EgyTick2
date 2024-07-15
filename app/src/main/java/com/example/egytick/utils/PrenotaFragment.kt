package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.egytick.databinding.FragmentPrenotaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PrenotaFragment : Fragment() {

    private var _binding: FragmentPrenotaBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrenotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val placeId = arguments?.getString("placeId") ?: return

        binding.btnConfirmBooking.setOnClickListener {
            createBooking(placeId)
        }
    }

    private fun createBooking(placeId: String) {
        val currentUser = firebaseAuth.currentUser ?: return

        val email = currentUser.email ?: ""
        val visitorType = binding.spVisitorType.selectedItem.toString()
        val numberOfVisitors = binding.etNumberOfVisitors.text.toString().toIntOrNull() ?: 0
        val selectedDate = binding.datePicker.run {
            Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
        }

        val bookingDetails = hashMapOf(
            "email" to email,
            "visitorType" to visitorType,
            "numberOfVisitors" to numberOfVisitors,
            "date" to selectedDate,
            "placeId" to placeId
        )

        firestore.collection("bookings")
            .add(bookingDetails)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Booking successful", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error creating booking: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
