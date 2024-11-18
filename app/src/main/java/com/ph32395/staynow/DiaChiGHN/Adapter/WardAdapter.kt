package com.ph32395.staynow.DiaChiGHN.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.DiaChiGHN.Model.Ward


class WardAdapter(
    private val wards: List<Ward>,
    private val onItemClick: (Ward) -> Unit
) : RecyclerView.Adapter<WardAdapter.WardViewHolder>() {

    private var filteredWards = wards.toList()

    fun filter(query: String) {
        filteredWards = if (query.isEmpty()) {
            wards
        } else {
            wards.filter { it.WardName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return WardViewHolder(view)
    }

    override fun onBindViewHolder(holder: WardViewHolder, position: Int) {
        holder.bind(filteredWards[position])
    }

    override fun getItemCount(): Int = filteredWards.size

    inner class WardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(android.R.id.text1)

        fun bind(ward: Ward) {
            textView.text = ward.WardName
            itemView.setOnClickListener { onItemClick(ward) }
        }
    }
}