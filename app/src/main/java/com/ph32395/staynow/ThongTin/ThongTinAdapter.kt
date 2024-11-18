package com.ph32395.staynow.ThongTin

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.ph32395.staynow.DichVu.DichVu
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.databinding.ItemThongtinBinding
class ThongTinAdapter(
    private val context: Context,
    private val thongtinList: List<ThongTin>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<ThongTinAdapter.ThongTinViewHolder>() {

    private val pricesMap = mutableMapOf<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThongTinViewHolder {
        val binding = ItemThongtinBinding.inflate(LayoutInflater.from(context), parent, false)
        return ThongTinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThongTinViewHolder, position: Int) {
        val thongtin = thongtinList[position]
        holder.bind(thongtin, position)
    }

    override fun getItemCount(): Int = thongtinList.size

    inner class ThongTinViewHolder(private val binding: ItemThongtinBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(thongTin: ThongTin, position: Int) {
            binding.thongtinName.text = thongTin.Ten_thongtin
            binding.donviThongTin.text = thongTin.Don_vi.takeIf { it.isNotEmpty() }?.let { "/$it" } ?: ""
            Glide.with(context)
                .load(thongTin.Icon_thongtin)
                .into(binding.thongtinImage)

            // Kiểm tra và hiển thị giá nếu có
            val price = pricesMap[position] // Lấy giá từ map tạm
            if (price != null) {
                binding.giaThongTin.text = "$price"
            }

            binding.itemDichvu.setOnClickListener {
                showInputDialog(thongTin, position)
            }
        }
    }

    private fun showInputDialog(thongTin: ThongTin, position: Int) {
        val editText = EditText(context)
        editText.hint = "Nhập giá tiền"
        editText.inputType = InputType.TYPE_CLASS_NUMBER

        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Nhập giá tiền cho ${thongTin.Ten_thongtin}")
            .setConfirmText("Xác nhận")
            .setCustomView(editText) // Thêm EditText vào dialog
            .setConfirmClickListener { sDialog ->
                val inputText = editText.text.toString()
                if (inputText.isNotEmpty()) {
                    val price = inputText.toIntOrNull()
                    if (price != null) {
                        // Lưu giá vào map
                        pricesMap[position] = price

                        // Cập nhật lại TextView giá tiền trong RecyclerView
                        notifyItemChanged(position)

                        // Kiểm tra xem đã nhập đủ giá cho tất cả Thông tin chưa
                        if(pricesMap.size == thongtinList.size) {
                            val priceList = thongtinList.mapIndexed { index, thongTin ->
                                thongTin to (pricesMap[index] ?: 0)
                            }
                            listener.onThongTinimfor(priceList)
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
