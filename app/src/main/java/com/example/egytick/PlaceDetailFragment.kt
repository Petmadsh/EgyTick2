package com.example.egytick

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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

        // Set click listener for the book button
        binding.bookButton.setOnClickListener {
            navigateToPrenotaFragment(placeId)
        }

        // Set click listener for the review button
        binding.reviewButton.setOnClickListener {
            navigateToReviewFragment(placeId)
        }
    }

    private fun fetchPlaceData(cityId: String, placeId: String) {
        firestore.collection("cities").document(cityId).collection("places").document(placeId).collection("details").get()
            .addOnSuccessListener { detailsResult ->
                if (!detailsResult.isEmpty) {
                    val detailsDocument = detailsResult.documents[0]
                    binding.placeName.text = detailsDocument.getString("name") ?: ""
                    binding.placeDescription.text = detailsDocument.getString("description") ?: ""
                    val imagePath = detailsDocument.getString("image") ?: ""
                    loadImage(imagePath, binding.placeImage)

                    // Load additional images if any
                    val additionalImages = detailsDocument.get("additionalImages") as? List<String> ?: emptyList()
                    addImagesToContainer(additionalImages, binding.placeImagesContainer)

                    // Set location
                    val location = detailsDocument.getGeoPoint("location")
                    if (location != null) {
                        val locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                        binding.placeLocation.text = locationText
                        binding.placeLocation.setOnClickListener {
                            openLocationInMaps(location)
                        }
                    }
                }
            }
    }

    private fun loadImage(imagePath: String, imageView: ImageView) {
        val context = imageView.context
        val resourceName = extractResourceName(imagePath)
        val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        if (imageResId != 0) {
            Glide.with(context)
                .load(imageResId)
                .into(imageView)
        }
    }

    private fun addImagesToContainer(images: List<String>, container: LinearLayout) {
        container.removeAllViews() // Clear existing views
        for (imagePath in images) {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400)  // Adjust width and height as needed
            layoutParams.bottomMargin = 8
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            val context = imageView.context
            val resourceName = extractResourceName(imagePath)
            val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .into(imageView)
            }

            container.addView(imageView)
        }
    }

    private fun openLocationInMaps(location: GeoPoint) {
        val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
