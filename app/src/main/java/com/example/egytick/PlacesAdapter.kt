package com.example.egytick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class PlacesAdapter(private val onPlaceClick: (String, String) -> Unit) :
    RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    private var places: List<Place> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view, onPlaceClick)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount() = places.size

    fun submitList(places: List<Place>) {
        this.places = places
        notifyDataSetChanged()
    }

    class PlaceViewHolder(itemView: View, private val onPlaceClick: (String, String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val placeImage: ImageView = itemView.findViewById(R.id.placeImage)
        private val placeName: TextView = itemView.findViewById(R.id.placeName)
        private val placeDescription: TextView = itemView.findViewById(R.id.placeDescription)

        fun bind(place: Place) {
            placeName.text = place.name
            placeDescription.text = place.description
            val context = itemView.context
            val imageName = extractResourceName(place.image)
            val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resourceId != 0) {
                Glide.with(context).load(resourceId).into(placeImage)
            }
            itemView.setOnClickListener {
                onPlaceClick(place.placeId, place.cityId)
            }
        }
    }
}
