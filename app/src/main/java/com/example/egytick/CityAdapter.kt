package com.example.egytick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.egytick.databinding.ItemCityBinding

class CityAdapter : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    private var cities: List<City> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position])
    }

    override fun getItemCount(): Int = cities.size

    fun submitList(cities: List<City>) {
        this.cities = cities
        notifyDataSetChanged()
    }

    class CityViewHolder(private val binding: ItemCityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: City) {
            binding.cityName.text = city.name
            binding.cityDescription.text = city.description
            val context = binding.cityImage.context
            val resourceName = extractResourceName(city.image)
            val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .centerCrop()
                    .into(binding.cityImage)
            }
        }
    }
}
