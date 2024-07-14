package com.example.egytick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.egytick.databinding.ItemCategoryBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories: List<Category> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun submitList(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.categoryTitle.text = category.title
            val context = binding.categoryImage.context
            val resourceName = extractResourceName(category.image)
            val imageResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.categoryImage)
            }
        }
    }
}
