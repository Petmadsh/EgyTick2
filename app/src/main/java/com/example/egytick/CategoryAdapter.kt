package com.example.egytick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CategoryAdapter(private val onCategoryClick: (String) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories: List<Category> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    fun submitList(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View, private val onCategoryClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
        private val categoryTitle: TextView = itemView.findViewById(R.id.categoryTitle)

        private fun getImageNameFromPath(imagePath: String): String {
            return imagePath.substringAfterLast('/').substringBeforeLast('.')
        }

        fun bind(category: Category) {
            categoryTitle.text = category.title
            val context = itemView.context
            val imageName = extractResourceName(category.image)
            val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resourceId != 0) {
                Glide.with(context).load(resourceId).into(categoryImage)
            }
            itemView.setOnClickListener {
                onCategoryClick(category.title)
            }
        }
    }
}
