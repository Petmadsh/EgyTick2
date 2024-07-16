package com.example.egytick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.egytick.databinding.FragmentPlaceListBinding
import com.google.firebase.firestore.FirebaseFirestore

class PlaceListFragment : Fragment() {

    private var _binding: FragmentPlaceListBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var placesAdapter: PlacesAdapter
    private var category: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = arguments?.getString("category")
        firestore = FirebaseFirestore.getInstance()
        placesAdapter = PlacesAdapter { placeId, cityId ->
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

        binding.placesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.placesRecyclerView.adapter = placesAdapter

        loadPlaces()
    }

    private fun loadPlaces() {
        if (category.isNullOrEmpty()) return

        firestore.collection("cities").get()
            .addOnSuccessListener { cityResult ->
                val allPlaces = mutableListOf<Place>()
                for (cityDocument in cityResult.documents) {
                    val cityId = cityDocument.id
                    firestore.collection("cities").document(cityId).collection("places")
                        .whereEqualTo("category", category)
                        .get()
                        .addOnSuccessListener { placesResult ->
                            val places = placesResult.documents.map { placeDocument ->
                                val place = placeDocument.toObject(Place::class.java)!!
                                place.copy(placeId = placeDocument.id, cityId = cityId)
                            }
                            allPlaces.addAll(places)
                            placesAdapter.submitList(allPlaces)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading places: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
