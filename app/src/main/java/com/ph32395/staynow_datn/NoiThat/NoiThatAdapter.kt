package com.ph32395.staynow_datn.NoiThat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.databinding.ItemNoithatBinding
class NoiThatAdapter(
    private val context: Context,
    private val noiThatList: List<NoiThat>,
    private val listener: AdapterTaoPhongTroEnteredListenner,
    private val selectedItems: MutableSet<String> = mutableSetOf()
) : RecyclerView.Adapter<NoiThatAdapter.NoiThatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoiThatViewHolder {
        val binding = ItemNoithatBinding.inflate(LayoutInflater.from(context), parent, false)
        return NoiThatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoiThatViewHolder, position: Int) {
        val noiThat = noiThatList[position]
        // Kiểm tra xem nội thất này có trong danh sách đã chọn không
        val isItemSelected = selectedItems.contains(noiThat.maNoiThat.toString())
        holder.bind(noiThat, isItemSelected)
    }

    override fun getItemCount(): Int = noiThatList.size


    inner class NoiThatViewHolder(private val binding: ItemNoithatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val noiThat = noiThatList[adapterPosition]
                val maNoiThat = noiThat.maNoiThat.toString()

                // Toggle selection
                if (selectedItems.contains(maNoiThat)) {
                    selectedItems.remove(maNoiThat)
                    binding.root.isSelected = false
                    listener.onNoiThatSelected(noiThat, false)
                } else {
                    selectedItems.add(maNoiThat)
                    binding.root.isSelected = true
                    listener.onNoiThatSelected(noiThat, true)
                }
                notifyItemChanged(adapterPosition)
            }
        }

        fun bind(noiThat: NoiThat, isSelected: Boolean) {
            binding.apply {
                furnitureName.text = noiThat.tenNoiThat
                Glide.with(context)
                    .load(noiThat.iconNoiThat)
                    .into(furnitureImage)

                // Set trạng thái selected dựa trên danh sách đã chọn
                root.isSelected = isSelected
            }
        }
    }
}