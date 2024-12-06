package com.ph32395.staynow.quanlyhoadon

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.databinding.ItemContractTextBinding
import com.ph32395.staynow.hieunt.widget.tap

class ContractAdapter_QLHD : RecyclerView.Adapter<ContractAdapter_QLHD.ContractTextViewHolder>() {
    private var contractList: List<HopDong> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractTextViewHolder {
        val binding =
            ItemContractTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContractTextViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ContractTextViewHolder,
        position: Int
    ) {
        val contract = contractList[position]
        holder.bind(contract)
    }


    override fun getItemCount(): Int = contractList.size

    // Cập nhật dữ liệu trong Adapter từ LiveData
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(contracts: List<HopDong>) {
        contractList = contracts
        notifyDataSetChanged()
    }

    inner class ContractTextViewHolder(itemView: ItemContractTextBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val tvContractId: TextView = itemView.tvContractCode

        @SuppressLint("SetTextI18n")
        fun bind(contract: HopDong) {

            tvContractId.text = "HD:${contract.maHopDong}"

            // sự kiện ấn vào item
            itemView.tap {
                val intent = Intent(itemView.context, HistoryBillContactActivity::class.java)
                intent.putExtra("CONTRACT_ID", contract.maHopDong)
                intent.putExtra("idHoaDon", contract.hoaDonHopDong.idHoaDon)
                itemView.context.startActivity(intent)

            }

        }
    }

}