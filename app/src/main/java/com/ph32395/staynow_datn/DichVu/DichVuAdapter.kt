
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.databinding.ItemDichvuBinding

class DichVuAdapter(
    private val context: Context,
    private var dichVuList: List<DichVu>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<DichVuAdapter.DichVuViewHolder>() {

    private val pricesMap = mutableMapOf<Int, Pair<Int, String>>()
    // Thêm phương thức để cập nhật danh sách
    fun updateList(newList: List<DichVu>) {
        dichVuList = newList
        notifyDataSetChanged()
    }

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
                Log.d("DichVuAdapter", "bind: $formattedPrice")
                Log.d("DichVuAdapter", "bind: $formattedPrice đ / ${priceInfo.second}")

            } else {
                binding.giaDichvu.text = "Chưa nhập giá"
            }

            Glide.with(context)
                .load(dichvu.Icon_dichvu)
                .into(binding.dichvuImage)

            binding.itemDichvu.setOnClickListener {
                showInputDialog(dichvu, position)
            }
            // Thêm nút xóa
            binding.btnDelete.setOnClickListener {
                showDeleteConfirmDialog(position)
            }
        }
    }
    private fun showDeleteConfirmDialog(position: Int) {
        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Xóa dịch vụ")
            .setConfirmClickListener { sDialog ->
                // Tạo list mới loại trừ phần tử bị xóa
                val updatedList = dichVuList.toMutableList().apply {
                    removeAt(position)
                }

                // Cập nhật adapter
                dichVuList = updatedList
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, updatedList.size)

                // Thông báo cho listener
                updatePriceList(updatedList)

                sDialog.dismissWithAnimation()
            }
            .show()
    }

    private fun updatePriceList(updatedList: List<DichVu>) {
        val priceList = updatedList.mapIndexed { index, dichVu ->
            val priceInfo = pricesMap[index]
            PhiDichVu(
                Ma_phongtro = "",
                Ten_dichvu = dichVu.Ten_dichvu,
                Don_vi = priceInfo?.second ?: "",
                Icon_dichvu = dichVu.Icon_dichvu,
                So_tien = priceInfo?.first ?: 0
            )
        }
        listener.onAllPricesEntered(priceList)
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
                val inputText = CurrencyFormatTextWatcher.getUnformattedValue(editText).toInt()
                val selectedUnit = spinner.selectedItem.toString()

                if (inputText > 0 && selectedUnit.isNotEmpty()) {
                    // Lưu giá và đơn vị vào pricesMap
                    pricesMap[position] = inputText to selectedUnit
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
            }
            .show()

// Khi khởi tạo dialog, áp dụng CurrencyFormatTextWatcher
        CurrencyFormatTextWatcher.addTo(editText)
    }

    fun addDichVu(dichVu: DichVu) {
        // Tạo list mới và thêm dịch vụ
        val updatedList = dichVuList.toMutableList().apply {
            add(dichVu)
        }

        // Cập nhật adapter
        dichVuList = updatedList
        notifyItemInserted(updatedList.size - 1)

        // Check the exact type of your pricesMap and adjust accordingly
        // This is a placeholder - you may need to adjust based on your exact implementation
        pricesMap[updatedList.size - 1] = Pair(0, "")

        // Thông báo cho listener
        updatePriceList(updatedList)
    }

}
