package com.ph32395.staynow_datn.QuanLyNhaTro

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.databinding.ItemNhaTroNhaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NhaTroAdapter(
    private val listNhaTro: List<NhaTroModel>
) : RecyclerView.Adapter<NhaTroAdapter.NhaTroAdapterViewHolder>() {

    private lateinit var binding: ItemNhaTroNhaBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NhaTroAdapterViewHolder {

        binding = ItemNhaTroNhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return NhaTroAdapterViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listNhaTro.size
    }

    override fun onBindViewHolder(holder: NhaTroAdapterViewHolder, position: Int) {
        val item = listNhaTro[position]

        holder.bin(item, binding)

        holder.itemView.setOnClickListener {
            val bottomSheetCreateAndUpdateNhaTro = BottomSheetCreateAndUpdateNhaTro(item)
            val context = holder.itemView.context
            if (context is FragmentActivity) {
                bottomSheetCreateAndUpdateNhaTro.show(
                    context.supportFragmentManager,
                    bottomSheetCreateAndUpdateNhaTro.tag
                )
            }

        }

    }

    class NhaTroAdapterViewHolder(itemView: ItemNhaTroNhaBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        @SuppressLint("SetTextI18n")
        fun bin(item: NhaTroModel, itemView: ItemNhaTroNhaBinding) {
            itemView.tvTenNhaTro.text = "Tên nhà: ${item.tenNhaTro}"
            itemView.tvDiaChi.text = "Địa chỉ: ${item.diaChiChiTiet}"
            itemView.tvTenLoaiNhaTro.text = "Loại nhà: ${item.tenLoaiNhaTro}"
            itemView.tvNgayTao.text = "Ngày tạo: ${convertTimestampToDate(item.ngayTao)}"
        }

        fun convertTimestampToDate(timestamp: Long): String {
            // Chọn định dạng ngày bạn muốn
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            // Chuyển đổi timestamp thành đối tượng Date
            val date = Date(timestamp)
            // Định dạng đối tượng Date thành chuỗi
            return dateFormat.format(date)
        }

    }

}