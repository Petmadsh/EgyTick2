package com.example.egytick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.egytick.databinding.ItemPlaceBinding

class PlacesAdapter : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    private var places: List<Place> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    fun submitList(places: List<Place>) {
        this.places = places
        notifyDataSetChanged()
    }

    class PlaceViewHolder(private val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.placeName.text = place.name
            binding.placeDescription.text = place.description

            val context = binding.placeImage.context
            val resourceName = extractResourceName(place.image)
            val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .centerCrop()
                    .into(binding.placeImage)
            }
        }
    }
}
