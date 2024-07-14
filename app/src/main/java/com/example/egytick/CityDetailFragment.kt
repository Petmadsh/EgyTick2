package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.egytick.databinding.FragmentCityDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class CityDetailFragment : Fragment() {

    private var _binding: FragmentCityDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var cityImagesAdapter: CityImagesAdapter
    private lateinit var placesAdapter: PlacesAdapter

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

        // Set up RecyclerViews
        cityImagesAdapter = CityImagesAdapter()
        binding.cityImagesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.cityImagesRecyclerView.adapter = cityImagesAdapter

        placesAdapter = PlacesAdapter()
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.placesRecyclerView.adapter = placesAdapter

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
                                cityImagesAdapter.submitList(images)
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
                            val places = placeResult.map { placeDocument ->
                                Place(
                                    name = placeDocument.getString("name") ?: "",
                                    description = placeDocument.getString("description") ?: "",
                                    image = placeDocument.getString("image") ?: ""
                                )
                            }
                            placesAdapter.submitList(places)
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
