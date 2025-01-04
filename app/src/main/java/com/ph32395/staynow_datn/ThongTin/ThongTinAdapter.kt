package com.ph32395.staynow_datn.ThongTin

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin
import com.ph32395.staynow_datn.databinding.ItemThongtinBinding
import java.text.DecimalFormat

class ThongTinAdapter(
    private val context: Context,
    private var thongtinList: List<ThongTin>,
    private val listener: AdapterTaoPhongTroEnteredListenner,
    private val existingChiTietList: List<ChiTietThongTin>? = null
) : RecyclerView.Adapter<ThongTinAdapter.ThongTinViewHolder>() {

     val pricesMap = mutableMapOf<Int, Long>()
    init {
        existingChiTietList?.forEach { chiTiet ->
            val matchingIndex = thongtinList.indexOfFirst { it.tenThongTin == chiTiet.tenThongTin }
            if (matchingIndex != -1) {
                pricesMap[matchingIndex] = chiTiet.soLuongDonVi.toLong() // Gán soLuongDonVi vào pricesMap
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThongTinViewHolder {
        val binding = ItemThongtinBinding.inflate(LayoutInflater.from(context), parent, false)
        return ThongTinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThongTinViewHolder, position: Int) {
        val thongtin = thongtinList[position]
//        Log.d("ThongTinAdapter", "Binding item: ${thongtin.Ten_thongtin}, Price: ${pricesMap[position]}")
        holder.bind(thongtin, position)
    }

    override fun getItemCount(): Int = thongtinList.size

    inner class ThongTinViewHolder(private val binding: ItemThongtinBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(thongTin: ThongTin, position: Int) {
            binding.thongtinName.text = thongTin.tenThongTin
            binding.donviThongTin.text = thongTin.donVi.takeIf { it.isNotEmpty() }?.let { "/$it" } ?: ""
            Glide.with(context)
                .load(thongTin.iconThongTin)
                .into(binding.thongtinImage)

            // Kiểm tra và hiển thị giá nếu có
            val price = pricesMap[position] // Lấy giá từ map tạm
            if (price != null) {
                binding.giaThongTin.text = "$price"
            }
            if (price != null) {
                val priceStr = price.toString() // Chuyển đổi giá trị thành chuỗi để kiểm tra độ dài
                if (priceStr.length >= 4) {
                    // Định dạng chuỗi số thành định dạng có dấu phân cách
                    val formattedPrice = DecimalFormat("#,###").format(price)
                    binding.giaThongTin.text = "$formattedPrice"
                } else {
                    binding.giaThongTin.text = "$price"
                }
            }
            binding.itemDichvu.setOnClickListener {
                showInputDialog(thongTin, position)
            }

        }
    }
    fun updateData(newList: List<ThongTin>) {
        thongtinList = newList
        notifyDataSetChanged() // Làm mới danh sách hiển thị
    }
    private fun showInputDialog(thongTin: ThongTin, position: Int) {
        val editText = EditText(context)
        editText.hint = "Nhập giá trị"
        editText.inputType = InputType.TYPE_CLASS_NUMBER

        // Nếu đã có giá trị trước đó, hiển thị sẵn
        editText.setText(pricesMap[position]?.toString() ?: "")

        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Nhập giá trị cho ${thongTin.tenThongTin}")
            .setConfirmText("Xác nhận")
            .setCustomView(editText) // Thêm EditText vào dialog
            .setConfirmClickListener { sDialog ->
                val inputText = editText.text.toString()
                if (inputText.isNotEmpty()) {
                    val price = inputText.toIntOrNull()
                    if (price != null) {
                        // Lưu giá vào map
                        pricesMap[position] = price.toLong()

                        // Cập nhật lại TextView giá tiền trong RecyclerView
                        notifyItemChanged(position)

                        if(pricesMap.size == thongtinList.size) {
                            val priceList = thongtinList.mapIndexed { index, thongTin ->
                                thongTin to (pricesMap[index] ?: 0).toInt() // Chuyển về Int
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


    fun getCurrentThongTin(): List<Pair<ThongTin, Int>> {
        return thongtinList.mapIndexed { index, thongTin ->
            val price = pricesMap[index]?.toInt() ?: 0
            Pair(thongTin, price)
        }
    }
}