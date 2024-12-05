package com.ph32395.staynow.DichVu

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoPhongTro.PhiDichVu
import com.ph32395.staynow.databinding.ItemDichvuBinding
class DichVuAdapter(
    private val context: Context,
    private val dichVuList: List<DichVu>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<DichVuAdapter.DichVuViewHolder>() {

    private val pricesMap = mutableMapOf<Int, Pair<Int, String>>() // Lưu giá và đơn vị

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DichVuViewHolder {
        val binding = ItemDichvuBinding.inflate(LayoutInflater.from(context), parent, false)
        return DichVuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DichVuViewHolder, position: Int) {
        val dichvu = dichVuList[position]
        holder.bind(dichvu, position)
    }

    override fun getItemCount(): Int = dichVuList.size

    inner class DichVuViewHolder(private val binding: ItemDichvuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dichvu: DichVu, position: Int) {
            binding.dichvuName.text = dichvu.Ten_dichvu
            val priceInfo = pricesMap[position]
            if (priceInfo != null) {
                val formattedPrice = String.format("%,d", priceInfo.first)
                binding.giaDichvu.text = "$formattedPrice đ / ${priceInfo.second}"
            } else {
                binding.giaDichvu.text = "Chưa nhập giá"
            }

            Glide.with(context)
                .load(dichvu.Icon_dichvu)
                .into(binding.dichvuImage)

            binding.itemDichvu.setOnClickListener {
                showInputDialog(dichvu, position)
            }
        }
    }

    private fun showInputDialog(dichvu: DichVu, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_price_unit, null)
        val editText = dialogView.findViewById<EditText>(R.id.editPrice)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerUnit)

        // Thiết lập giá trị đã chọn trước đó (nếu có)
        val existingPriceInfo = pricesMap[position]
        if (existingPriceInfo != null) {
            // Set giá đã nhập trước đó
            editText.setText(existingPriceInfo.first.toString())

            // Set đơn vị đã chọn trước đó
            val unitList = dichvu.Don_vi
            val adapter = ArrayAdapter(context, com.airbnb.lottie.R.layout.support_simple_spinner_dropdown_item, unitList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Tìm vị trí của đơn vị đã chọn trong list và set cho Spinner
            val selectedUnitPosition = unitList.indexOf(existingPriceInfo.second)
            if (selectedUnitPosition != -1) {
                spinner.setSelection(selectedUnitPosition)
            }
        } else {
            // Trường hợp chưa có giá trị
            val unitList = dichvu.Don_vi
            val adapter = ArrayAdapter(context, com.airbnb.lottie.R.layout.support_simple_spinner_dropdown_item, unitList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Nhập giá tiền cho ${dichvu.Ten_dichvu}")
            .setCustomView(dialogView)
            .setConfirmText("Xác nhận")
            .setConfirmClickListener { sDialog ->
                val inputText = editText.text.toString()
                val selectedUnit = spinner.selectedItem.toString()

                if (inputText.isNotEmpty() && selectedUnit.isNotEmpty()) {
                    val price = inputText.toIntOrNull()
                    if (price != null) {
                        // Lưu giá và đơn vị vào pricesMap
                        pricesMap[position] = price to selectedUnit
                        notifyItemChanged(position)

                        // Kiểm tra xem đã nhập đủ giá chưa
                        if (pricesMap.size == dichVuList.size) {
                            val priceList = dichVuList.mapIndexed { index, dichVu ->
                                PhiDichVu(
                                    Ma_phongtro = "",
                                    Ten_dichvu = dichVu.Ten_dichvu,
                                    Don_vi = pricesMap[index]?.second ?: "",
                                    Icon_dichvu = dichVu.Icon_dichvu,
                                    So_tien = pricesMap[index]?.first ?: 0
                                )
                            }
                            listener.onAllPricesEntered(priceList)
                        }
                        sDialog.dismissWithAnimation()
                    } else {
                        Toast.makeText(context, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
