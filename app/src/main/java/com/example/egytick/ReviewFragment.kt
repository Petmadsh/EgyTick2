package com.example.egytick

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.egytick.databinding.FragmentReviewBinding
import com.google.firebase.firestore.FirebaseFirestore

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val placeId = arguments?.getString("placeId") ?: return
        fetchReviewsData(placeId)

        binding.cleanlinessRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }
        binding.facilitiesRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }
        binding.staffRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }

        binding.submitButton.setOnClickListener {
            confirmSubmitReview(placeId)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchReviewsData(placeId: String) {
        firestore.collection("reviews").whereEqualTo("placeId", placeId).get()
            .addOnSuccessListener { documents ->
                var totalReviews = 0
                var totalRating = 0.0

                for (document in documents) {
                    val rating = document.getDouble("averageRating") ?: 0.0
                    totalRating += rating
                    totalReviews++
                }

                if (totalReviews > 0) {
                    val overallAverageRating = totalRating / totalReviews
                    binding.overallAverageRatingText.text = "Place average rating: %.1f".format(overallAverageRating)
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error loading place average rating: ${e.message}", true)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAverageUserRating() {
        val cleanlinessRating = binding.cleanlinessRating.rating
        val facilitiesRating = binding.facilitiesRating.rating
        val staffRating = binding.staffRating.rating

        val averageRating = (cleanlinessRating + facilitiesRating + staffRating) / 3.0
        binding.averageUserRatingText.text = "Your average rating: %.1f".format(averageRating)
    }

    private fun confirmSubmitReview(placeId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Submit Review")
            .setMessage("Are you sure you want to submit your review?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                submitReview(placeId)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun submitReview(placeId: String) {
        val cleanlinessRating = binding.cleanlinessRating.rating.toDouble()
        val facilitiesRating = binding.facilitiesRating.rating.toDouble()
        val staffRating = binding.staffRating.rating.toDouble()

        val userAverageRating = (cleanlinessRating + facilitiesRating + staffRating) / 3.0

        val review = hashMapOf(
            "placeId" to placeId,
            "averageRating" to userAverageRating
        )

        firestore.collection("reviews").add(review)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Review submitted", Toast.LENGTH_SHORT).show()
                fetchReviewsData(placeId)
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error submitting review: ${e.message}", true)
            }
    }

    private fun showErrorSnackBar(message: String, isError: Boolean) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
