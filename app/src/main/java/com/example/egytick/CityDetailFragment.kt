package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.egytick.databinding.FragmentCityDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class CityDetailFragment : Fragment() {

    private var _binding: FragmentCityDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Fetch city data and places
        val cityName = arguments?.getString("cityName") ?: ""
        binding.cityName.text = cityName
        fetchCityData(cityName)
        fetchPlaces(cityName)
    }

    private fun fetchCityData(cityName: String) {
        firestore.collection("cities").whereEqualTo("name", cityName).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    firestore.collection("cities").document(document.id).collection("citydata").get()
                        .addOnSuccessListener { cityDataResult ->
                            for (cityDataDocument in cityDataResult) {
                                binding.cityDescription.text = cityDataDocument.getString("description") ?: ""
                                val images = cityDataDocument.get("image") as? List<String> ?: emptyList()
                                addImagesToContainer(images, binding.cityImagesContainer)
                            }
                        }
                }
            }
    }

    private fun fetchPlaces(cityName: String) {
        firestore.collection("cities").whereEqualTo("name", cityName).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    firestore.collection("cities").document(document.id).collection("places").get()
                        .addOnSuccessListener { placeResult ->
                            for (placeDocument in placeResult) {
                                val placeName = placeDocument.getString("name") ?: ""
                                val placeDescription = placeDocument.getString("description") ?: ""
                                val placeImage = placeDocument.getString("image") ?: ""
                                addPlaceToContainer(placeName, placeDescription, placeImage, binding.placesContainer)
                            }
                        }
                }
            }
    }

    private fun addImagesToContainer(images: List<String>, container: LinearLayout) {
        for (imagePath in images) {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(1100, 900)  // Adjust width and height as needed
            layoutParams.marginEnd = 8
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

    private fun addPlaceToContainer(name: String, description: String, image: String, container: LinearLayout) {
        val placeView = layoutInflater.inflate(R.layout.item_place, container, false)

        val placeNameTextView = placeView.findViewById<com.example.egytick.utils.TextViewBold>(R.id.placeName)
        val placeDescriptionTextView = placeView.findViewById<com.example.egytick.utils.TextView>(R.id.placeDescription)
        val placeImageView = placeView.findViewById<ImageView>(R.id.placeImage)

        placeNameTextView.text = name
        placeDescriptionTextView.text = description

        val context = placeImageView.context
        val resourceName = extractResourceName(image)
        val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        if (imageResId != 0) {
            Glide.with(context)
                .load(imageResId)
                .centerCrop()
                .into(placeImageView)
        }

        container.addView(placeView)
    }

    private fun extractResourceName(path: String): String {
        return path.substringAfterLast('/').substringBeforeLast('.')
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
