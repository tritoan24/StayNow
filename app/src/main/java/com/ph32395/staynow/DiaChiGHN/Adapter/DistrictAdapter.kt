package com.ph32395.staynow.DiaChiGHN.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.Ward

class DistrictAdapter(
    private val districts: List<District>,
    private val onItemClick: (District) -> Unit
) : RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder>() {

    private var filteredDistricts = districts.toList()

    fun filter(query: String) {
        filteredDistricts = if (query.isEmpty()) {
            districts
        } else {
            districts.filter { it.DistrictName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistrictViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return DistrictViewHolder(view)
    }

    override fun onBindViewHolder(holder: DistrictViewHolder, position: Int) {
        holder.bind(filteredDistricts[position])
    }

    override fun getItemCount(): Int = filteredDistricts.size

    inner class DistrictViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(android.R.id.text1)

        fun bind(district: District) {
            textView.text = district.DistrictName
            itemView.setOnClickListener { onItemClick(district) }
        }
    }
}

