package com.ph32395.staynow.NoiThat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.databinding.ItemNoithatBinding

class NoiThatAdapter(
    private val context: Context,
    private val noiThatList: List<NoiThat>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<NoiThatAdapter.NoiThatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoiThatViewHolder {
        val binding = ItemNoithatBinding.inflate(LayoutInflater.from(context), parent, false)
        return NoiThatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoiThatViewHolder, position: Int) {
        val noiThat = noiThatList[position]
        holder.bind(noiThat)
    }

    override fun getItemCount(): Int = noiThatList.size

    inner class NoiThatViewHolder(private val binding: ItemNoithatBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isSelected = false // Biến lưu trạng thái chọn
        init {
            binding.root.setOnClickListener {

                // Đổi trạng thái selected
                isSelected = !isSelected
                it.isSelected = isSelected
                //nếu trạng thái đang là true thì mới gọi listener

                if (it.isSelected) {
                    // Gọi listener để truyền dữ liệu về Activity
                    listener.onNoiThatSelected(noiThatList[adapterPosition], it.isSelected)
                }
            }
        }

        fun bind(noiThat: NoiThat) {
            binding.furnitureName.text = noiThat.Ten_noithat
            Glide.with(context)
                .load(noiThat.Icon_noithat)
                .into(binding.furnitureImage)

            // Set trạng thái selected
            binding.root.isSelected = false
        }
    }
}
