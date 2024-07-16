package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.egytick.databinding.FragmentPlaceDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Fetch place data
        val placeId = arguments?.getString("placeId") ?: return
        val cityId = arguments?.getString("cityId") ?: return
        fetchPlaceData(cityId, placeId)

        // Set click listener for the review button
        binding.reviewButton.setOnClickListener {
            navigateToReviewFragment(placeId)
        }

        // Set click listener for the book button
        binding.bookButton.setOnClickListener {
            navigateToPrenotaFragment(placeId)
        }
    }

    private fun fetchPlaceData(cityId: String, placeId: String) {
        firestore.collection("cities").document(cityId).collection("places").document(placeId).collection("details").get()
            .addOnSuccessListener { detailsResult ->
                if (!detailsResult.isEmpty) {
                    val detailsDocument = detailsResult.documents[0]
                    binding.placeDescription.text = detailsDocument.getString("placeDescription") ?: ""

                    // Load additional images if any
                    val additionalImages = detailsDocument.get("images") as? List<String> ?: emptyList()
                    addImagesToContainer(additionalImages, binding.placeImagesContainer)

                    // Set location
                    val location = detailsDocument.getGeoPoint("location")
                    if (location != null) {
                        val locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                        binding.placeLocation.text = locationText
                    }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error loading place data: ${e.message}", true)
            }
    }


    private fun loadImage(imageName: String, imageView: ImageView) {
        val context = imageView.context
        val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        if (imageResId != 0) {
            Glide.with(context)
                .load(imageResId)
                .into(imageView)
        }
    }

    private fun addImagesToContainer(images: List<String>, container: LinearLayout) {
        container.removeAllViews() // Clear existing views
        for (imageName in images) {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(400, 400)  // Adjust width and height as needed
            layoutParams.marginEnd = 8
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            val context = imageView.context
            val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .into(imageView)
            }

            container.addView(imageView)
        }
    }

    private fun navigateToReviewFragment(placeId: String) {
        val fragment = ReviewFragment().apply {
            arguments = Bundle().apply {
                putString("placeId", placeId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToPrenotaFragment(placeId: String) {
        val fragment = PrenotaFragment().apply {
            arguments = Bundle().apply {
                putString("placeId", placeId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
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
