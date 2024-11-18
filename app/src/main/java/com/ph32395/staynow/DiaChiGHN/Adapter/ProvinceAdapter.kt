package com.ph32395.staynow.DiaChiGHN.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.DiaChiGHN.Model.Province

class ProvinceAdapter(
    private val provinces: List<Province>,
    private val onItemClick: (Province) -> Unit
) : RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder>() {

    private var filteredProvinces = provinces.toList()

    fun filter(query: String) {
        filteredProvinces = if (query.isEmpty()) {
            provinces
        } else {
            provinces.filter { it.ProvinceName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ProvinceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProvinceViewHolder, position: Int) {
        holder.bind(filteredProvinces[position])
    }

    override fun getItemCount(): Int = filteredProvinces.size

    inner class ProvinceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(android.R.id.text1)

        fun bind(province: Province) {
            textView.text = province.ProvinceName
            itemView.setOnClickListener { onItemClick(province) }
        }
    }
}
