package com.ph32395.staynow.fragment.contract_tenant

import com.ph32395.staynow.TaoHopDong.HopDong
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.TaoHopDong.ContractViewModel
import com.ph32395.staynow.databinding.ItemContractBinding

class ContractAdapter(
    private var contractList: List<HopDong> = emptyList(),
    private val viewmodel: ContractViewModel
) : RecyclerView.Adapter<ContractAdapter.ContractViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val binding =
            ItemContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContractViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val contract = contractList[position]
        holder.bind(contract)

    }

    override fun getItemCount(): Int = contractList.size
    // Tạo phương thức để cập nhật danh sách hợp đồng khi có thay đổi
    fun updateContractList(newList: List<HopDong>) {
        contractList = newList
        notifyDataSetChanged() // Thông báo adapter cập nhật lại danh sách
    }
    inner class ContractViewHolder(itemView: ItemContractBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        val tvContractId: TextView = itemView.tvContractCode
        val tvRoomName: TextView = itemView.tvRoomName
        val tvStartDate: TextView = itemView.tvStartDate
        val tvEndDate: TextView = itemView.tvEndDate
        val tvRentDuration: TextView = itemView.tvRentDuration
        val tvRemainingTime: TextView = itemView.tvRemainingTime

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(contract: HopDong) {
            tvContractId.text = "Mã Hợp Đồng: ${contract.maHopDong}"
            tvRoomName.text = "Tên phòng: ${contract.hoaDonHopDong.tenPhong}"
            tvStartDate.text = "Ngày Bắt Đầu: ${contract.ngayBatDau}"
            tvEndDate.text = "Ngày Kết Thúc: ${contract.ngayKetThuc}"
            tvRentDuration.text = "Thời Gian Thuê: ${contract.thoiHanThue}"

        }
    }
}