package com.example.egytick

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.egytick.databinding.FragmentPrenotaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class PrenotaFragment : Fragment() {

    private var _binding: FragmentPrenotaBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedDate: Calendar? = null

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

        // Set up spinners with adapters
        val visitorTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.visitor_type_array,
            android.R.layout.simple_spinner_item
        )
        visitorTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVisitorType.adapter = visitorTypeAdapter

        val visitorCategoryAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.visitor_category_array,
            android.R.layout.simple_spinner_item
        )
        visitorCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVisitorCategory.adapter = visitorCategoryAdapter

        val placeId = arguments?.getString("placeId") ?: return

        if (selectedDate == null) {
            selectedDate = Calendar.getInstance()
        }

        showDatePickerDialog()

        binding.btnConfirmBooking.setOnClickListener {
            checkExistingBooking(placeId)
        }
    }

    private fun showDatePickerDialog() {
        val year = selectedDate?.get(Calendar.YEAR) ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = selectedDate?.get(Calendar.MONTH) ?: Calendar.getInstance().get(Calendar.MONTH)
        val day = selectedDate?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            Toast.makeText(requireContext(), "Selected date: ${selectedDay}/${selectedMonth + 1}/${selectedYear}", Toast.LENGTH_SHORT).show()
        }, year, month, day)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun checkExistingBooking(placeId: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email ?: return
        val selectedDate = this.selectedDate?.time ?: return

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormatter.format(selectedDate)

        firestore.collection("bookings")
            .whereEqualTo("email", email)
            .whereEqualTo("placeId", placeId)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                val existingBooking = querySnapshot.documents.any { document ->
                    val bookingDate = document.getDate("date")
                    dateFormatter.format(bookingDate) == formattedDate
                }
                if (existingBooking) {
                    Toast.makeText(requireContext(), "You already have a ticket for this location on the selected date", Toast.LENGTH_LONG).show()
                } else {
                    createBooking(placeId)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error checking existing tickets: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createBooking(placeId: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email ?: ""
        val visitorType = binding.spVisitorType.selectedItem.toString()
        val visitorCategory = binding.spVisitorCategory.selectedItem.toString()
        val selectedDate = this.selectedDate?.time ?: run {
            Toast.makeText(requireContext(), "Select a date", Toast.LENGTH_LONG).show()
            showDatePickerDialog()
            return
        }

        val bookingDetails = hashMapOf(
            "email" to email,
            "visitorType" to visitorType,
            "visitorCategory" to visitorCategory,
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
