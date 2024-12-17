package com.ph32395.staynow_datn.TaoPhongTro

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.R
class ChoiceImageAdapter(
    private val imageUris: MutableList<Uri>, // Sử dụng MutableList để có thể thay đổi dữ liệu
    private val onDeleteClick: (Int) -> Unit // Callback để xóa ảnh
) : RecyclerView.Adapter<ChoiceImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton) // Nút xóa
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choice_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position]
        Glide.with(holder.imageView.context)
            .load(uri)
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            removeImageAt(position)
        }
    }

    override fun getItemCount() = imageUris.size

    fun removeImageAt(position: Int) {
        if (position >= 0 && position < imageUris.size) {
            imageUris.removeAt(position)
            notifyItemRemoved(position)

            // Đảm bảo chỉ số mới được cập nhật đúng nếu bạn có sử dụng phương thức xóa sau đó
            notifyItemRangeChanged(position, imageUris.size)
        } else {
            Log.e("ChoiceImageAdapter", "Invalid index: $position")
        }
    }

}
