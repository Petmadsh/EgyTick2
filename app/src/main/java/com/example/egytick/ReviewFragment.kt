package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        // Fetch reviews data for the place and update UI
        val placeId = arguments?.getString("placeId") ?: return
        fetchReviewsData(placeId)

        // Set up listeners for rating changes
        binding.cleanlinessRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }
        binding.facilitiesRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }
        binding.staffRating.setOnRatingBarChangeListener { _, _, _ -> updateAverageUserRating() }

        // Set up click listener for the submit button
        binding.submitButton.setOnClickListener {
            submitReview(placeId)
        }
    }

    private fun fetchReviewsData(placeId: String) {
        firestore.collection("cities").get()
            .addOnSuccessListener { citiesSnapshot ->
                for (cityDocument in citiesSnapshot.documents) {
                    val cityId = cityDocument.id
                    firestore.collection("cities").document(cityId)
                        .collection("places").document(placeId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val totalReviews = document.getLong("totalReviews") ?: 0
                                val totalRating = document.getDouble("totalRating") ?: 0.0

                                if (totalReviews > 0) {
                                    val overallAverageRating = totalRating / totalReviews
                                    binding.overallAverageRatingText.text = "Overall average rating: %.1f".format(overallAverageRating)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            showErrorSnackBar("Error loading overall average rating: ${e.message}", true)
                        }
                }
            }
    }

    private fun updateAverageUserRating() {
        val cleanlinessRating = binding.cleanlinessRating.rating
        val facilitiesRating = binding.facilitiesRating.rating
        val staffRating = binding.staffRating.rating

        val averageRating = (cleanlinessRating + facilitiesRating + staffRating) / 3.0
        binding.averageUserRatingText.text = "Your average rating: %.1f".format(averageRating)
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
                updatePlaceRating(placeId, userAverageRating)
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error submitting review: ${e.message}", true)
            }
    }

    private fun updatePlaceRating(placeId: String, newRating: Double) {
        firestore.collection("cities").get()
            .addOnSuccessListener { citiesSnapshot ->
                for (cityDocument in citiesSnapshot.documents) {
                    val cityId = cityDocument.id
                    val placeRef = firestore.collection("cities").document(cityId)
                        .collection("places").document(placeId)

                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(placeRef)

                        val totalReviews = snapshot.getLong("totalReviews") ?: 0
                        val totalRating = snapshot.getDouble("totalRating") ?: 0.0

                        val newTotalReviews = totalReviews + 1
                        val newTotalRating = totalRating + newRating

                        val newAverageRating = newTotalRating / newTotalReviews

                        transaction.update(placeRef, "totalReviews", newTotalReviews)
                        transaction.update(placeRef, "totalRating", newTotalRating)
                        transaction.update(placeRef, "averageRating", newAverageRating)
                    }.addOnSuccessListener {
                        fetchReviewsData(placeId)  // Refresh the reviews data
                    }.addOnFailureListener { e ->
                        showErrorSnackBar("Error updating place rating: ${e.message}", true)
                    }
                }
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
