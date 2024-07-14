package com.example.egytick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.egytick.databinding.ItemCityImageBinding

class CityImagesAdapter : RecyclerView.Adapter<CityImagesAdapter.CityImageViewHolder>() {

    private var images: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityImageViewHolder {
        val binding = ItemCityImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    fun submitList(images: List<String>) {
        this.images = images
        notifyDataSetChanged()
    }

    class CityImageViewHolder(private val binding: ItemCityImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imagePath: String) {
            val context = binding.cityImage.context
            val resourceName = extractResourceName(imagePath)
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
