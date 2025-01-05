//package com.ph32395.staynow_datn.QuanLyPhongTro.UpdateRoom
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
//import com.ph32395.staynow_datn.databinding.ItemDichvuBinding
//class PhiDichVuAdapter(
//    private val context: Context,
//    private var phiDichVuList: List<PhiDichVu>,
//    private val listener: OnPhiDichVuChangeListener
//) : RecyclerView.Adapter<PhiDichVuAdapter.PhiDichVuViewHolder>() {
//
//    interface OnPhiDichVuChangeListener {
//        fun onPhiDichVuChanged(phiDichVuList: List<PhiDichVu>)
//    }
//
//    inner class PhiDichVuViewHolder(private val binding: ItemDichvuBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(phiDichVu: PhiDichVu) {
//            binding.apply {
//                // Hiển thị thông tin dịch vụ
//                dichvuName.text = phiDichVu.tenDichVu
//
//                // Hiển thị giá và đơn vị
//                val formattedPrice = String.format("%,.0f", phiDichVu.soTien)
//                giaDichvu.text = "$formattedPrice đ / ${phiDichVu.donVi}"
//
//                // Load icon
//                Glide.with(context)
//                    .load(phiDichVu.iconDichVu)
//                    .into(dichvuImage)
//
//                // Xử lý sự kiện click để chỉnh sửa
//                itemDichvu.setOnClickListener {
//                    showEditDialog(phiDichVu, adapterPosition)
//                }
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhiDichVuViewHolder {
//        val binding = ItemDichvuBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return PhiDichVuViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PhiDichVuViewHolder, position: Int) {
//        holder.bind(phiDichVuList[position])
//    }
//
//    override fun getItemCount() = phiDichVuList.size
//
//    private fun showEditDialog(phiDichVu: PhiDichVu, position: Int) {
//        val dialogView = LayoutInflater.from(context)
//            .inflate(R.layout.dialog_select_price_unit, null)
//
//        val editText = dialogView.findViewById<EditText>(R.id.editPrice)
//        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerUnit)
//
//        // Thiết lập giá trị hiện tại
//        editText.setText(phiDichVu.soTien.toString())
//
//        // Thiết lập spinner với danh sách đơn vị
//        val donViList = getDonViList(phiDichVu.tenDichVu) // Hàm lấy danh sách đơn vị từ DB
//        val adapter = ArrayAdapter(
//            context,
//            android.R.layout.simple_spinner_item,
//            donViList
//        )
//        spinner.adapter = adapter
//
//        // Set đơn vị hiện tại
//        val currentUnitIndex = donViList.indexOf(phiDichVu.donVi)
//        if (currentUnitIndex != -1) {
//            spinner.setSelection(currentUnitIndex)
//        }
//
//        // Áp dụng định dạng tiền tệ
//        CurrencyFormatTextWatcher.addTo(editText)
//
//        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE).apply {
//            titleText = "Cập nhật giá cho ${phiDichVu.tenDichVu}"
//            customView = dialogView
//            confirmText = "Cập nhật"
//
//            setConfirmClickListener { dialog ->
//                val newPrice = CurrencyFormatTextWatcher
//                    .getUnformattedValue(editText)
//                    .toDouble()
//                val newUnit = spinner.selectedItem.toString()
//
//                if (newPrice > 0) {
//                    // Cập nhật giá mới
//                    val updatedPhiDichVu = phiDichVu.copy(
//                        soTien = newPrice,
//                        donVi = newUnit
//                    )
//
//                    // Cập nhật list
//                    val newList = phiDichVuList.toMutableList()
//                    newList[position] = updatedPhiDichVu
//                    phiDichVuList = newList
//
//                    // Thông báo thay đổi
//                    notifyItemChanged(position)
//                    listener.onPhiDichVuChanged(phiDichVuList)
//
//                    dialog.dismissWithAnimation()
//                } else {
//                    Toast.makeText(
//                        context,
//                        "Vui lòng nhập giá hợp lệ",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }.show()
//    }
//
//    // Hàm lấy danh sách đơn vị từ Firebase
//    private fun getDonViList(tenDichVu: String): List<String> {
//        // TODO: Implement logic to get donVi list from Firebase based on tenDichVu
//        return listOf("Tháng", "Người", "Khối", "Số")
//    }
//
//    // Hàm cập nhật danh sách
//    fun updateList(newList: List<PhiDichVu>) {
//        phiDichVuList = newList
//        notifyDataSetChanged()
//    }
//}