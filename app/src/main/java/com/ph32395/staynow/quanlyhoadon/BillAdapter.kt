package com.ph32395.staynow.quanlyhoadon

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.databinding.ItemBillBinding
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.utils.showConfirmDialog
import java.text.NumberFormat
import java.util.Locale

class BillAdapter(
    private val status: InvoiceStatus,
    private var isLandlord: Boolean,
    private val listener: OnInvoiceStatusUpdateListener?
) :
    RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private var billList: List<InvoiceMonthlyModel> = listOf()

    interface OnInvoiceStatusUpdateListener {
        fun onInvoiceStatusUpdate(invoiceId: String, newStatus: InvoiceStatus)
    }

    // Tạo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    // Liên kết dữ liệu vào item
    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billList[position]
        holder.bind(bill, status, isLandlord)
    }

    // Trả về số lượng item trong list
    override fun getItemCount(): Int = billList.size

    // Cập nhật danh sách hóa đơn
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(bills: List<InvoiceMonthlyModel>) {
        this.billList = bills
        notifyDataSetChanged()
    }

    // ViewHolder cho item hóa đơn
    inner class BillViewHolder(private val binding: ItemBillBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(bill: InvoiceMonthlyModel, status: InvoiceStatus, isLandlord: Boolean) {

            if (isLandlord) {
                binding.llBtn.visibility = View.GONE
            }

            if (status != InvoiceStatus.PENDING) {
                binding.llBtn.visibility = View.GONE
            }

            binding.tvBillId.text = "ID: ${bill.idHoaDon}"
            binding.tvCustomerName.text = bill.tenKhachHang
            binding.tvBillType.text = "Kiểu hóa đơn: ${bill.kieuHoadon}"
            binding.tvTotalAmount.text = "Tổng tiền: " + formatCurrency(bill.tongTien)
            binding.tvDate.text = "Ngày: ${bill.ngayLap}"
            binding.tvStatus.text = "Trạng thái: ${bill.trangThai}"

            binding.btnConfirm.tap {
                val intent = Intent(binding.root.context, DetailBillActivity::class.java)
                intent.putExtra("hoaDonHangThang", bill)
                binding.root.context.startActivity(intent)
            }
            binding.btnCancel.tap {
                showConfirmDialog(
                    binding.root.context,
                    "Xác nhận hủy hóa đơn",
                    "Bạn có chắc chắn muốn hủy hóa đơn này không?"
                ) {
                    listener?.onInvoiceStatusUpdate(bill.idHoaDon, InvoiceStatus.CANCELLED)
                }

            }
            itemView.tap {

            }
        }

    }

    // Định dạng tiền tệ
    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateIsLandlord(newIsLandlord: Boolean) {
        if (isLandlord != newIsLandlord) {
            isLandlord = newIsLandlord
            notifyDataSetChanged()  // Hoặc chỉ cập nhật các item cần thiết
        }
    }
}
