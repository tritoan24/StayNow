package com.ph32395.staynow_datn.fragment.contract_tenant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.TaoHopDong.ChiTietHopDong
import com.ph32395.staynow_datn.TaoHopDong.ContractStatus
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import com.ph32395.staynow_datn.TaoHopDong.HopDong
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.TaoHopDong.UpdateHopDong
import com.ph32395.staynow_datn.databinding.ItemContractBinding
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.utils.showConfirmDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ContractAdapter(
    private val viewmodel: ContractViewModel,
    private val type: ContractStatus,
    private val isLandlord: Boolean,
    private val onStatusUpdated: (contractId: String, newStatus: ContractStatus) -> Unit
) : RecyclerView.Adapter<ContractAdapter.ContractViewHolder>() {
    private var contractList: List<HopDong> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val binding =
            ItemContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContractViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val contract = contractList[position]
        holder.bind(contract, type, isLandlord)

    }

    override fun getItemCount(): Int = contractList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateContractList(newList: List<HopDong>) {
        contractList = newList
        notifyDataSetChanged()
    }

    inner class ContractViewHolder(itemView: ItemContractBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val tvContractId: TextView = itemView.tvContractCode
        private val tvRoomName: TextView = itemView.tvRoomName
        private val tvRoomAddress: TextView = itemView.tvRoomAddress
        private val tvStartDate: TextView = itemView.tvStartDate
        private val tvEndDate: TextView = itemView.tvEndDate
        private val tvRentDuration: TextView = itemView.tvRentDuration
        private val tvRemainingTime: TextView = itemView.tvRemainingTime
        private val llBtn: LinearLayout = itemView.llBtn

        private val btnXacNhan: TextView = itemView.btnConfirm
        private val btnCancel: TextView = itemView.btnCancel

        //công add
        private val btnEditHopDongPending = itemView.btnEditHopDong
        private val btnHuyHopDongPending = itemView.btnHuyHopDong

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(contract: HopDong, type: ContractStatus, isLandlord: Boolean) {


            tvContractId.text = "Mã Hợp Đồng: ${contract.maHopDong}"
            tvRoomName.text = "Tên phòng: ${contract.thongtinphong.tenPhong}"
            tvRoomAddress.text = "Địa chỉ phòng: ${contract.thongtinphong.diaChiPhong}"
            tvStartDate.text = "Ngày Bắt Đầu: ${contract.ngayBatDau}"
            tvEndDate.text = "Ngày Kết Thúc: ${contract.ngayKetThuc}"
            tvRentDuration.text = "Thời Gian Thuê: ${contract.thoiHanThue}"

            when (type) {
                ContractStatus.ACTIVE -> {
                    tvRemainingTime.text = calculateRemainingDays(contract.ngayKetThuc)

                    // Kiểm tra nếu ngày hết hạn đã qua, thay đổi trạng thái hợp đồng thành EXPIRED
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val parsedEndDate = LocalDate.parse(contract.ngayKetThuc, dateFormatter)

                    if (parsedEndDate.isBefore(LocalDate.now())) {
                        onStatusUpdated(contract.maHopDong, ContractStatus.EXPIRED)
                        updateContractList(contractList)
                    }
                }

                ContractStatus.PENDING -> {
                    tvRemainingTime.visibility = View.GONE
                    llBtn.visibility =
                        if (contract.hoaDonHopDong.trangThai == InvoiceStatus.PENDING) View.VISIBLE else View.GONE

                    if (isLandlord) {
                        llBtn.visibility = View.GONE
                        //cong add
                        btnEditHopDongPending.visibility = View.VISIBLE
                        btnHuyHopDongPending.visibility = View.VISIBLE
                    } else {
                        //cong add
                        Log.e("TAGzzz", "bind: not nguoi chu")
                        btnEditHopDongPending.visibility = View.GONE
                        btnHuyHopDongPending.visibility = View.GONE
                    }


                    //công add start
                    btnEditHopDongPending.setOnClickListener {
                        Log.e("TAGzzz", "bind: btn sửa hợp đồng Pending")
                        val bottomSheetUpdateHopDong = UpdateHopDong(
                            contract.thongtinphong.maPhongTro,
                            contract.nguoiThue.maNguoiDung,
                            contract
                        )
                        val context = itemView.context
                        if (context is FragmentActivity) {
                            bottomSheetUpdateHopDong.show(
                                context.supportFragmentManager,
                                bottomSheetUpdateHopDong.tag
                            )
                        }
                    }
                    btnHuyHopDongPending.setOnClickListener {
                        Log.e("TAGzzz", "bind: btn Hủy hợp đồng Pending")
                        showConfirmDialog(
                            itemView.context,
                            "Hủy Hợp Đồng",
                            "Bạn có chắc chắn muốn hủy hợp đồng này?"
                        ) {
                            FirebaseFirestore.getInstance().collection("HopDong")
                                .document(contract.maHopDong)
                                .update("trangThai", "TERMINATED")
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        itemView.context,
                                        "Hủy thành công",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val notificationModel = NotificationModel(
                                        tieuDe = "Hợp đồng đã bị hủy",
                                        tinNhan = "Hợp đồng phòng ${contract.thongtinphong.tenPhong} đã bị hủy bở chủ nhà ${contract.chuNha.hoTen}",
                                        ngayGuiThongBao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                            Date()
                                        ),
                                        thoiGian = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                            Date()
                                        ),
                                        loaiThongBao = "HuyHopDong",
                                        mapLink = null,
                                        thoiGianGuiThongBao = System.currentTimeMillis(),
                                        idModel = contract.maHopDong,
                                    )
                                    val factory = ViewModelFactory(itemView.context)
                                    val viewModelNotification = ViewModelProvider(
                                        itemView.context as AppCompatActivity,
                                        factory
                                    )[NotificationViewModel::class.java]
                                    viewModelNotification.sendNotification(notificationModel, contract.nguoiThue.maNguoiDung)
                                }
                                .addOnFailureListener {
                                    Log.e("TAG_Update contract", "bind: ${it.message.toString()}")
                                }
                        }
                    }
                    //Công add end

                    btnXacNhan.tap {

                        val intent = Intent(itemView.context, BillContractActivity::class.java)

                        intent.putExtra("hoaDonHopDong", contract.hoaDonHopDong)
                        intent.putExtra("hopDong", contract)

                        itemView.context.startActivity(intent)
                    }
                    btnCancel.tap {
                        showConfirmDialog(
                            itemView.context,
                            "Hủy Hợp Đồng",
                            "Bạn có chắc chắn muốn hủy hợp đồng này?"
                        ) {
                            onStatusUpdated(
                                contract.maHopDong,
                                ContractStatus.TERMINATED
                            )
                            updateContractList(contractList)
                            handlePaymentSuccess(itemView.context, contract)
                        }

                    }
                }

                ContractStatus.EXPIRED -> {
                    tvRemainingTime.text = "Hợp đồng đã hết hạn"
                }

                ContractStatus.TERMINATED -> {
                    tvRemainingTime.text = "Hợp đồng đã bị hủy"
                }

                ContractStatus.PROCESSING -> {
                    tvRemainingTime.text = "Hợp đồng đang được xử lý"
                    llBtn.visibility =
                        if (contract.hoaDonHopDong.trangThai == InvoiceStatus.PROCESSING) View.VISIBLE else View.GONE
                }
            }

            // sự kiện ấn vào item
            itemView.tap {
                val intent = Intent(itemView.context, ChiTietHopDong::class.java)
                intent.putExtra("CONTRACT_ID", contract.maHopDong)
                intent.putExtra("detail", "detail")
                itemView.context.startActivity(intent)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateRemainingDays(endDate: String): String {
        return try {
            // Định dạng ngày theo "dd/MM/yyyy"
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val parsedEndDate = LocalDate.parse(endDate, dateFormatter)

            // Tính khoảng cách ngày giữa hiện tại và endDate
            val daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), parsedEndDate)

            // So sánh khoảng cách ngày
            when {
                daysDifference > 0 -> "Còn $daysDifference ngày đến hạn"
                daysDifference == 0L ->
                    "Hôm nay là ngày hết hạn!"

                else -> "Hợp đồng đã quá hạn ${-daysDifference} ngày"
            }
        } catch (e: Exception) {
            "Lỗi định dạng ngày"
        }
    }
}

private fun handlePaymentSuccess(context: Context, contract: HopDong) {

    val notification = NotificationModel(
        tieuDe = "Thanh toán hóa đơn hợp đồng",
        tinNhan = "Hóa đơn hợp đồng với mã hóa đơn ${contract.hoaDonHopDong.idHoaDon} đã bị hủy",
        ngayGuiThongBao = Calendar.getInstance().time.toString(),
        thoiGian = "0",
        mapLink = null,
        daDoc = false,
        daGui = true,
        idModel = contract.maHopDong,
        loaiThongBao = "hoadonhopdong"
    )

    val factory = ViewModelFactory(context)
    val notificationViewModel = ViewModelProvider(
        context as AppCompatActivity,
        factory
    )[NotificationViewModel::class.java]

    // Gửi thông báo đến cả hai người
    val recipientIds = listOf(contract.nguoiThue.maNguoiDung, contract.chuNha.maNguoiDung)
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
