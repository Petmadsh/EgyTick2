package com.example.egytick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CityAdapter(private val cities: List<City>) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.textViewName.text = city.name
        holder.textViewDescription.text = city.description
        Glide.with(holder.imageView.context)
            .load(city.image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = cities.size

    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
    }
}
