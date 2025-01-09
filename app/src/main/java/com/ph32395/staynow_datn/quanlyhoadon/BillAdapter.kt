package com.ph32395.staynow_datn.quanlyhoadon

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.databinding.ItemBillBinding
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.utils.showConfirmDialog
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.Observer

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
    @RequiresApi(Build.VERSION_CODES.O)
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
        @RequiresApi(Build.VERSION_CODES.O)
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
            binding.tvDate.text = "Ngày: " + bill.ngayTaoHoaDon
            binding.tvStatus.text = "Trạng thái: ${bill.trangThai}"

            binding.btnConfirm.tap {
                val intent = Intent(binding.root.context, DetailBillActivity::class.java)
                intent.putExtra("bill", bill)
                binding.root.context.startActivity(intent)
            }

            binding.btnCancel.tap {
                showConfirmDialog(
                    binding.root.context,
                    "Xác nhận hủy hóa đơn",
                    "Bạn có chắc chắn muốn hủy hóa đơn này không?"
                ) {
                    handlePaymentSuccess(itemView.context, bill)
                    listener?.onInvoiceStatusUpdate(bill.idHoaDon, InvoiceStatus.CANCELLED)
                }

            }
            itemView.tap {
                val intent = Intent(binding.root.context, DetailBillActivity::class.java)
                intent.putExtra("bill", bill)
                intent.putExtra("detail", "detail")
                binding.root.context.startActivity(intent)
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
            notifyDataSetChanged()
        }
    }
}

private fun handlePaymentSuccess(context: Context, bill: InvoiceMonthlyModel) {

    val notification = NotificationModel(
        tieuDe = "Thanh toán hóa đơn dịch vụ",
        tinNhan = "Hóa đơn dịch vụ với mã hóa đơn ${bill.idHoaDon} đã bị hủy",
        ngayGuiThongBao = Calendar.getInstance().time.toString(),
        thoiGian = "0",
        mapLink = null,
        daDoc = false,
        daGui = true,
        idModel = bill.idHoaDon,
        loaiThongBao = "hoadonhangthang"
    )

    val factory = ViewModelFactory(context)
    val notificationViewModel = ViewModelProvider(
        context as AppCompatActivity,
        factory
    )[NotificationViewModel::class.java]

    // Gửi thông báo đến cả hai người
    val recipientIds = listOf(bill.idNguoiGui, bill.idNguoiNhan)
    recipientIds.forEach { recipientId ->
        notificationViewModel.sendNotification(notification, recipientId)
    }
    // Giám sát trạng thái gửi thông báo
    notificationViewModel.notificationStatus.observe(context, Observer { isSuccess ->
        if (isSuccess) {
            // Thông báo thành công
            Toast.makeText(context, "Thông báo đã được gửi!", Toast.LENGTH_SHORT).show()
        } else {
            // Thông báo thất bại
            Toast.makeText(context, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT).show()
        }
    })

}
