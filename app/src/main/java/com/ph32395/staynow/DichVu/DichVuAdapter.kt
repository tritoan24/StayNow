package com.ph32395.staynow.DichVu

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.databinding.ItemDichvuBinding
class DichVuAdapter(
    private val context: Context,
    private val dichVuList: List<DichVu>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<DichVuAdapter.DichVuViewHolder>() {

    private val pricesMap = mutableMapOf<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DichVuViewHolder {
        val binding = ItemDichvuBinding.inflate(LayoutInflater.from(context), parent, false)
        return DichVuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DichVuViewHolder, position: Int) {
        val dichvu = dichVuList[position]
        holder.bind(dichvu, position) // Truyền position vào hàm bind
    }

    override fun getItemCount(): Int = dichVuList.size

    inner class DichVuViewHolder(private val binding: ItemDichvuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dichvu: DichVu, position: Int) {
            binding.dichvuName.text = dichvu.Ten_dichvu
            binding.donviDichvu.text = if (dichvu.Don_vi.isEmpty()) "" else "/${dichvu.Don_vi}"
            Glide.with(context)
                .load(dichvu.Icon_dichvu)
                .into(binding.dichvuImage)

            // Hiển thị giá nếu đã có giá
            val price = pricesMap[position] // Lấy giá từ map tạm
            if (price != null) {
                binding.giaDichvu.text = "$price đ"
            }

            binding.itemDichvu.setOnClickListener {
                showInputDialog(dichvu, position)
            }
        }
    }

    private fun showInputDialog(dichvu: DichVu, position: Int) {
        val editText = EditText(context)
        editText.hint = "Nhập giá tiền"
        editText.inputType = InputType.TYPE_CLASS_NUMBER

        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Nhập giá tiền cho ${dichvu.Ten_dichvu}")
            .setConfirmText("Xác nhận")
            .setCustomView(editText) // Thêm EditText vào dialog
            .setConfirmClickListener { sDialog ->
                val inputText = editText.text.toString()
                if (inputText.isNotEmpty()) {
                    val price = inputText.toIntOrNull()
                    if (price != null) {
                        // Lưu giá vào map
                        pricesMap[position] = price

                        // Cập nhật lại giao diện
                        notifyItemChanged(position)


                        // Kiểm tra xem đã nhập đủ giá cho tất cả dịch vụ chưa
                        if (pricesMap.size == dichVuList.size) {
                            val priceList = dichVuList.mapIndexed { index, dichVu ->
                                dichVu to (pricesMap[index] ?: 0)
                            }
                            listener.onAllPricesEntered(priceList)
                        }
                        sDialog.dismissWithAnimation()
                    } else {
                        Toast.makeText(context, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Vui lòng nhập giá tiền", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
