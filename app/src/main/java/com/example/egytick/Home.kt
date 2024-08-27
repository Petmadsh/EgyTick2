package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.egytick.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var cityAdapter: CityAdapter
    private lateinit var placesAdapter: PlacesAdapter

    private var placeList: MutableList<Place> = mutableListOf()
    private var cityList: MutableList<City> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        categoryAdapter = CategoryAdapter { category ->
            navigateToPlaceList(category)
        }
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter

        cityAdapter = CityAdapter { cityName ->
            navigateToCityDetail(cityName)
        }
        binding.citiesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.citiesRecyclerView.adapter = cityAdapter

        placesAdapter = PlacesAdapter { placeId, cityId ->
            navigateToPlaceDetail(placeId, cityId)
        }
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.placesRecyclerView.adapter = placesAdapter

        fetchCategories()
        fetchCities()
        fetchPlaces()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterPlaces(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPlaces(newText)
                return true
            }
        })
    }

    private fun fetchCategories() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.map { document ->
                    Category(
                        title = document.getString("title") ?: "",
                        image = document.getString("image") ?: ""
                    )
                }
                categoryAdapter.submitList(categories)
            }
    }

    private fun fetchCities() {
        firestore.collection("cities")
            .get()
            .addOnSuccessListener { result ->
                cityList = result.map { document ->
                    City(
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        image = document.getString("image") ?: ""
                    )
                }.toMutableList()
                cityAdapter.submitList(cityList)
            }
    }

    private fun fetchPlaces() {
        firestore.collection("cities")
            .get()
            .addOnSuccessListener { result ->
                for (cityDocument in result) {
                    val cityId = cityDocument.id
                    cityDocument.reference.collection("places")
                        .get()
                        .addOnSuccessListener { placesResult ->
                            for (placeDocument in placesResult) {
                                val place = Place(
                                    name = placeDocument.getString("name") ?: "",
                                    description = placeDocument.getString("description") ?: "",
                                    image = placeDocument.getString("image") ?: "",
                                    category = placeDocument.getString("category") ?: "",
                                    placeId = placeDocument.id,
                                    cityId = cityId
                                )
                                placeList.add(place)
                            }
                        }
                }
            }
    }

    private fun filterPlaces(query: String?) {
        if (query.isNullOrEmpty()) {
            binding.citiesRecyclerView.visibility = View.VISIBLE
            binding.placesRecyclerView.visibility = View.GONE
            binding.placesTitle.visibility = View.GONE
            binding.tvNoPlacesFound.visibility = View.GONE
            placesAdapter.submitList(emptyList())
        } else {
            val filteredList = placeList.filter { place ->
                place.name.contains(query, ignoreCase = true)
            }

            if (filteredList.isNotEmpty()) {
                binding.citiesRecyclerView.visibility = View.GONE
                binding.placesRecyclerView.visibility = View.VISIBLE
                binding.placesTitle.visibility = View.VISIBLE
                binding.tvNoPlacesFound.visibility = View.GONE
                placesAdapter.submitList(filteredList)
            } else {
                binding.citiesRecyclerView.visibility = View.GONE
                binding.placesRecyclerView.visibility = View.GONE
                binding.placesTitle.visibility = View.GONE
                binding.tvNoPlacesFound.visibility = View.VISIBLE
                placesAdapter.submitList(emptyList())
            }
        }
    }

    private fun navigateToPlaceList(category: String) {
        val fragment = PlaceListFragment().apply {
            arguments = Bundle().apply {
                putString("category", category)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCityDetail(cityName: String) {
        val fragment = CityDetailFragment().apply {
            arguments = Bundle().apply {
                putString("cityName", cityName)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToPlaceDetail(placeId: String, cityId: String) {
        val fragment = PlaceDetailFragment().apply {
            arguments = Bundle().apply {
                putString("placeId", placeId)
                putString("cityId", cityId)
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
