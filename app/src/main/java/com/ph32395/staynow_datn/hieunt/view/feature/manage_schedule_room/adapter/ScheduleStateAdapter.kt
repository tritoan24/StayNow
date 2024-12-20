package com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter

import android.annotation.SuppressLint
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ItemScheduleStateBinding
import com.ph32395.staynow_datn.hieunt.base.BaseAdapter
import com.ph32395.staynow_datn.hieunt.base.BaseViewHolder
import com.ph32395.staynow_datn.hieunt.model.ScheduleStateModel
import com.ph32395.staynow_datn.hieunt.widget.layoutInflate

class ScheduleStateAdapter(
    private val onClickState: (Int) -> Unit
) : BaseAdapter<ScheduleStateModel, ScheduleStateAdapter.ScheduleStateVH>() {
    inner class ScheduleStateVH(binding: ItemScheduleStateBinding) : BaseViewHolder<ScheduleStateModel, ItemScheduleStateBinding>(binding) {
        @SuppressLint("SetTextI18n")
        override fun bindData(data: ScheduleStateModel) {
            super.bindData(data)
            binding.apply {
                tvState.text = "${data.name} (${data.count})"
                vSelect.visibility = if (data.isSelected) VISIBLE else INVISIBLE
            }
        }

        override fun onItemClickListener(data: ScheduleStateModel) {
            super.onItemClickListener(data)
            setSelectedState(bindingAdapterPosition)
            onClickState.invoke(bindingAdapterPosition)
        }
    }

    override fun viewHolder(viewType: Int, parent: ViewGroup): ScheduleStateVH = ScheduleStateVH(
        ItemScheduleStateBinding.inflate(parent.layoutInflate(), parent, false)
    )

    override fun layout(position: Int): Int = R.layout.item_schedule_state

    fun setSelectedState(position: Int) {
        val oldIndex = listData.indexOfFirst { it.isSelected }
        for (item in listData) {
            item.isSelected = false
        }
        listData[position].apply { isSelected = true }
        if (oldIndex != -1) {
            notifyItemChanged(oldIndex)
        }
        notifyItemChanged(position)
    }
}