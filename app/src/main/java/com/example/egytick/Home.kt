package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Set up RecyclerViews
        categoryAdapter = CategoryAdapter()
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter

        cityAdapter = CityAdapter { cityName ->
            navigateToCityDetail(cityName)
        }
        binding.citiesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.citiesRecyclerView.adapter = cityAdapter

        // Fetch data from Firestore
        fetchCategories()
        fetchCities()
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
                val cities = result.map { document ->
                    City(
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        image = document.getString("image") ?: ""
                    )
                }
                cityAdapter.submitList(cities)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
