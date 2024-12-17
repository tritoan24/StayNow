package com.ph32395.staynow_datn.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.ph32395.staynow_datn.R

class ImageRecyclerViewAdapter(private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<ImageRecyclerViewAdapter.ImageViewHolder>() {

    private val images = mutableListOf<String>()
    private var selectedItemPosition: Int? = null

    fun setImages(newImages: List<String>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(holder.itemView.context).load(imageUrl).into(holder.imageView)

        // Kiểm tra nếu item được chọn và log thông tin
        if (position == selectedItemPosition) {
            holder.imageView.setBackgroundResource(R.drawable.selected_border)
        } else {
            holder.imageView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Xử lý sự kiện nhấn vào ảnh
        holder.itemView.setOnClickListener {
            selectedItemPosition = position
            notifyItemChanged(position) // Cập nhật lại UI
            onItemClick(imageUrl) // Gọi sự kiện khi ảnh được chọn
        }
    }

    override fun getItemCount() = images.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageView) // Sử dụng ShapeableImageView để bo góc
    }
}