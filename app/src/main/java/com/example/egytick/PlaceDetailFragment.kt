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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class PlaceDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var mapView: MapView
    private var location: GeoPoint? = null

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

        // Initialize MapView
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

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

                    // Display opening hours
                    val openingHours = detailsDocument.get("openingHours") as? List<String> ?: emptyList()
                    displayOpeningHours(openingHours)

                    // Display ticket prices
                    val ticketPrices = detailsDocument.get("ticketPrice") as? List<String> ?: emptyList()
                    displayTicketPrices(ticketPrices)

                    // Set location
                    location = detailsDocument.getGeoPoint("location")
                    if (location != null) {
                        mapView.getMapAsync(this)
                    }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error loading place data: ${e.message}", true)
            }
    }

    private fun displayOpeningHours(openingHours: List<String>) {
        val openingHoursText = StringBuilder("Opening Hours:\n")
        for (hours in openingHours) {
            openingHoursText.append("$hours\n")
        }
        binding.openingHours.text = openingHoursText.toString()
    }

    private fun displayTicketPrices(ticketPrices: List<String>) {
        val ticketPricesText = StringBuilder("Ticket Prices:\n")
        for (price in ticketPrices) {
            ticketPricesText.append("$price\n")
        }
        binding.ticketPrices.text = ticketPricesText.toString()
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
            val layoutParams = LinearLayout.LayoutParams(1200, 900)
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // MapView lifecycle management
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        location?.let {
            val placeLatLng = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(placeLatLng).title("Place Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15f))

            // Fetch and display weather for this location
            fetchWeatherData(it.latitude, it.longitude)
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val apiKey = "b5d27ffd2d374fe692e172137242208"
        val url = "https://api.weatherapi.com/v1/current.json?key=$apiKey&q=$latitude,$longitude&aqi=no"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showErrorSnackBar("Failed to load weather data", true)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body?.string()
                if (!response.isSuccessful || jsonResponse == null) {
                    requireActivity().runOnUiThread {
                        showErrorSnackBar("Error: ${response.message}", true)
                    }
                    return
                }

                val weatherData = Gson().fromJson(jsonResponse, WeatherApiResponse::class.java)

                requireActivity().runOnUiThread {
                    // Update the UI with weather data
                    binding.weatherTemperature.text = "${weatherData.current.temp_c}°C"
                    binding.weatherDescription.text = weatherData.current.condition.text

                    // Update humidity and wind speed
                    binding.weatherHumidity.text = "Humidity: ${weatherData.current.humidity}%"
                    binding.weatherWind.text = "Wind: ${weatherData.current.wind_kph} km/h"

                    // Load the weather icon using Glide
                    val iconUrl = "https:${weatherData.current.condition.icon}"
                    Glide.with(requireContext()).load(iconUrl).into(binding.weatherIcon)
                }
            }
        })
    }
}
