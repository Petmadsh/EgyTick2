package com.example.egytick

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.egytick.databinding.FragmentTicketsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Locale

class Tickets : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        loadBookings()
    }

    private fun loadBookings() {
        val currentUser = firebaseAuth.currentUser ?: return
        firestore.collection("bookings").whereEqualTo("email", currentUser.email).get()
            .addOnSuccessListener { result ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                if (result.isEmpty) {
                    showNoTicketsMessage()
                } else {
                    binding.bookingsContainer.removeAllViews()
                    for (document in result.documents) {
                        addBookingToContainer(document)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (isAdded && _binding != null) {
                    Toast.makeText(requireContext(), "Error loading bookings: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun addBookingToContainer(document: DocumentSnapshot) {
        if (!isAdded || _binding == null) return

        val bookingView = layoutInflater.inflate(R.layout.item_booking, binding.bookingsContainer, false)
        val placeNameTextView = bookingView.findViewById<TextView>(R.id.placeName)
        val qrCodeImageView = bookingView.findViewById<ImageView>(R.id.qrCodeImage)
        val cancelButton = bookingView.findViewById<Button>(R.id.cancelButton)
        val bookingDateTextView = bookingView.findViewById<TextView>(R.id.bookingDate)

        val placeId = document.getString("placeId")
        val bookingTimestamp = document.getTimestamp("date")

        if (placeId != null) {
            getPlaceName(placeId) { placeName ->
                if (isAdded && _binding != null) {
                    placeNameTextView.text = placeName
                }
            }
        } else {
            placeNameTextView.text = "Unknown Place"
        }

        val formattedDate: String = if (bookingTimestamp != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            dateFormat.format(bookingTimestamp.toDate())
        } else {
            "Unknown Date"
        }
        bookingDateTextView.text = formattedDate

        val bookingData = document.data?.toMutableMap() ?: mutableMapOf()
        bookingData["date"] = formattedDate

        val bookingDataString = bookingData.entries.joinToString(", ") { "${it.key}=${it.value}" }
        val qrCodeBitmap = generateQRCode(bookingDataString)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        cancelButton.setOnClickListener {
            if (isAdded && _binding != null) {
                showConfirmationDialog(document.id, bookingView)
            }
        }

        binding.bookingsContainer.addView(bookingView)
    }

    private fun getPlaceName(placeId: String, callback: (String) -> Unit) {
        firestore.collection("cities").get()
            .addOnSuccessListener { citiesResult ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                var placeFound = false
                var pendingRequests = citiesResult.documents.size

                for (cityDocument in citiesResult.documents) {
                    val cityId = cityDocument.id
                    firestore.collection("cities").document(cityId).collection("places").document(placeId).get()
                        .addOnSuccessListener { placeDocument ->
                            if (!isAdded || _binding == null) return@addOnSuccessListener

                            pendingRequests--
                            if (placeDocument.exists() && !placeFound) {
                                val placeName = placeDocument.getString("name") ?: "Unknown Place"
                                callback(placeName)
                                placeFound = true
                            }
                            if (pendingRequests == 0 && !placeFound) {
                                callback("Unknown Place")
                            }
                        }
                        .addOnFailureListener {
                            if (!isAdded || _binding == null) return@addOnFailureListener

                            pendingRequests--
                            if (pendingRequests == 0 && !placeFound) {
                                callback("Unknown Place")
                            }
                        }
                }
            }
            .addOnFailureListener {
                if (isAdded && _binding != null) {
                    callback("Unknown Place")
                }
            }
    }

    private fun generateQRCode(data: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            null
        }
    }

    private fun showConfirmationDialog(bookingId: String, bookingView: View) {
        if (!isAdded || _binding == null) return

        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                if (isAdded && _binding != null) {
                    deleteBooking(bookingId, bookingView)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showNoTicketsMessage() {
        if (!isAdded || _binding == null) return

        val noTicketsTextView = TextView(context).apply {
            text = "There are no tickets"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        binding.bookingsContainer.addView(noTicketsTextView)
    }

    private fun deleteBooking(bookingId: String, bookingView: View) {
        firestore.collection("bookings").document(bookingId).delete()
            .addOnSuccessListener {
                if (isAdded && _binding != null) {
                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show()
                    binding.bookingsContainer.removeView(bookingView)
                }
            }
            .addOnFailureListener { e ->
                if (isAdded && _binding != null) {
                    Toast.makeText(requireContext(), "Error cancelling booking: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
