package com.ph32395.staynow.hieunt.base

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<M : Any, VH : BaseViewHolder<M, *>> : RecyclerView.Adapter<VH>() {
    val listData = mutableListOf<M>()

    protected abstract fun viewHolder(viewType: Int, parent: ViewGroup): VH

    protected abstract fun layout(position: Int): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        viewHolder(viewType, parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindData(listData[position])
    }

    override fun getItemViewType(position: Int): Int = layout(position)

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun addList(list: List<M>) {
        listData.clear()
        listData.addAll(list)
        notifyDataSetChanged()
    }

    fun addListObserver(newList: List<M>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = listData.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                listData[oldItemPosition] == newList[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                listData[oldItemPosition] == newList[newItemPosition]
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listData.clear()
        listData.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}

