package com.ph32395.staynow_datn.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.databinding.ItemUserStatusMessengerBinding

class UserStatusOnOfAdapter(
    private val userStatus: List<MessageFragment.UserStatus>,
    private val onClickItem: (MessageFragment.UserStatus) -> Unit
) : RecyclerView.Adapter<UserStatusOnOfAdapter.UserStatusOnOfViewHolder>() {

    private lateinit var binding: ItemUserStatusMessengerBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStatusOnOfViewHolder {
        binding = ItemUserStatusMessengerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return UserStatusOnOfViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userStatus.size
    }

    override fun onBindViewHolder(holder: UserStatusOnOfViewHolder, position: Int) {
        val item = userStatus[position]
        holder.bin(item)
        holder.itemView.setOnClickListener {
            onClickItem(item)
        }
    }

    class UserStatusOnOfViewHolder(itemView: ItemUserStatusMessengerBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val ivAvatar = itemView.ivAvatarItemTinNhan
        val status = itemView.vTrangThaiUser
        fun bin(userStatus: MessageFragment.UserStatus) {
            Glide.with(itemView.context)
                .load(userStatus.anh_daidien)
                .circleCrop()
                .into(ivAvatar)
            val statusDrawable = status.background as GradientDrawable
            statusDrawable.setColor(if (userStatus.status == "online") Color.GREEN else Color.GRAY)

        }

    }

}