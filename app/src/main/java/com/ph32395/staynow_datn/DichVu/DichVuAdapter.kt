
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
import java.text.NumberFormat
import java.util.Locale

class DichVuAdapter(
    private val context: Context,
    private var dichVuList: List<DichVu>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<DichVuAdapter.DichVuViewHolder>() {

    val pricesMap = mutableMapOf<Int, Pair<Double, String>>()
    //new code
    private var defaultUnitList: List<String> = emptyList()
    fun setDefaultUnitList(unitList: List<String>) {
        Log.d("DichVuAdapter", "Setting default unit list: $unitList")
        defaultUnitList = unitList
        notifyDataSetChanged()
    }

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
            binding.dichvuName.text = dichvu.tenDichVu
            val priceInfo = pricesMap[position]
            if (priceInfo != null) {
                val formattedPrice = String.format("%,.0f", priceInfo.first)
                binding.giaDichvu.text = "$formattedPrice đ / ${priceInfo.second}"
                Log.d("DichVuAdapter", "bind: $formattedPrice")
                Log.d("DichVuAdapter", "bind: $formattedPrice đ / ${priceInfo.second}")

            } else {
                binding.giaDichvu.text = "Chưa nhập giá"
            }

            Glide.with(context)
                .load(dichvu.iconDichVu)
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
                maPhongTro = "",
                tenDichVu = dichVu.tenDichVu,
                donVi = priceInfo?.second ?: "",
                iconDichVu = dichVu.iconDichVu,
                soTien = priceInfo?.first ?: 0.0
            )
        }
        listener.onAllPricesEntered(priceList)
    }
    //new
    fun updatePrices(prices: List<PhiDichVu>) {
        prices.forEachIndexed { index, phiDichVu ->
            pricesMap[index] = phiDichVu.soTien to phiDichVu.donVi
        }
        notifyDataSetChanged()
    }
    private fun showInputDialog(dichvu: DichVu, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_price_unit, null)
        val editText = dialogView.findViewById<EditText>(R.id.editPrice)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerUnit)

        // Tạo danh sách đơn vị (đảm bảo không trùng lặp)
        val unitList = (defaultUnitList + dichvu.donVi).distinct()
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, unitList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Kiểm tra giá trị đã tồn tại
        val existingPriceInfo = pricesMap[position]
        existingPriceInfo?.let {
            // Set giá đã nhập trước đó
            // Format số để bỏ phần thập phân .0
            val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            val formattedNumber = numberFormat.format(it.first)
            editText.setText(formattedNumber)

            // Set đơn vị đã chọn trước đó
            val selectedUnitPosition = unitList.indexOf(it.second)
            if (selectedUnitPosition != -1) {
                spinner.setSelection(selectedUnitPosition)
            }
        }

        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Nhập giá tiền cho ${dichvu.tenDichVu}")
            .setCustomView(dialogView)
            .setConfirmText("Xác nhận")
            .setConfirmClickListener { sDialog ->
                // Sử dụng hàm getUnformattedValue từ CurrencyFormatTextWatcher
                val inputValue = CurrencyFormatTextWatcher.getUnformattedValue(editText)
                val selectedUnit = spinner.selectedItem.toString()

                if (inputValue > 0 && selectedUnit.isNotEmpty()) {
                    pricesMap[position] = inputValue to selectedUnit
                    notifyItemChanged(position)

                    if (pricesMap.size == dichVuList.size) {
                        val priceList = dichVuList.mapIndexed { index, dichVu ->
                            PhiDichVu(
                                maPhongTro = "",
                                tenDichVu = dichVu.tenDichVu,
                                donVi = pricesMap[index]?.second ?: "",
                                iconDichVu = dichVu.iconDichVu,
                                soTien = pricesMap[index]?.first ?: 0.0
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
        pricesMap[updatedList.size - 1] = Pair(0.0, "")

        // Thông báo cho listener
        updatePriceList(updatedList)
    }

    fun getCurrentPhiDichVu(): List<PhiDichVu> {
        return dichVuList.mapIndexed { index, dichVu ->
            val priceInfo = pricesMap[index]
            PhiDichVu(
                maPhongTro = "",
                tenDichVu = dichVu.tenDichVu,
                donVi = priceInfo?.second ?: "",
                iconDichVu = dichVu.iconDichVu,
                soTien = priceInfo?.first ?: 0.0
            )
        }
    }
    fun getPhiDichVuList(): List<PhiDichVu> {
        return getCurrentPhiDichVu()
    }

}
