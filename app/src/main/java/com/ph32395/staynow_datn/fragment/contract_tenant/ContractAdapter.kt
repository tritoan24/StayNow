package com.ph32395.staynow_datn.fragment.contract_tenant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.TaoHoaDon.CreateInvoice
import com.ph32395.staynow_datn.TaoHopDong.ChiTietHopDong
import com.ph32395.staynow_datn.TaoHopDong.ContractStatus
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import com.ph32395.staynow_datn.TaoHopDong.HopDong
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.TaoHopDong.TerminationStatus
import com.ph32395.staynow_datn.databinding.ItemContractBinding
import com.ph32395.staynow_datn.hieunt.helper.Default
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.utils.showConfirmDialog
import com.ph32395.staynow_datn.utils.showReasonInputDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class ContractAdapter(
    private val viewmodel: ContractViewModel,
    private val type: ContractStatus,
    private val isLandlord: Boolean,
    private val onStatusUpdated: (contractId: String, newStatus: ContractStatus) -> Unit,
    private val onRequestTerminate: (HopDong, String?, status: TerminationStatus) -> Unit
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
        private val tvTermination: TextView = itemView.tvTerminated

        private val llBtn: LinearLayout = itemView.llBtn
        private val llBtnTermination: LinearLayout = itemView.llBtnTermination
        private val btnTerminated: Button = itemView.btnTerminated

        private val btnXacNhan: Button = itemView.btnConfirm
        private val btnCancel: Button = itemView.btnCancel
        private val btnXacNhanTer: Button = itemView.btnConfirmTermination
        private val btnCancelTer: Button = itemView.btnCancelTermination
        private val btnCreateBill: Button = itemView.btnCreateBill

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
                    if (!isLandlord) {
                        btnTerminated.visibility = View.VISIBLE
                        if (contract.yeuCauChamDut != TerminationStatus.NOT_YET) {
                            btnTerminated.visibility = View.GONE
                        }
                    } else {
                        if (contract.yeuCauChamDut == TerminationStatus.PENDING) {
                            llBtnTermination.visibility = View.VISIBLE
                            tvTermination.visibility = View.VISIBLE
                        }
                    }

                    // Kiểm tra nếu ngày hết hạn đã qua, thay đổi trạng thái hợp đồng thành EXPIRED
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val parsedEndDate = LocalDate.parse(contract.ngayKetThuc, dateFormatter)

                    if (parsedEndDate.isBefore(LocalDate.now())) {
                        onStatusUpdated(contract.maHopDong, ContractStatus.EXPIRED)
                        updateContractList(contractList)
                    }

                    btnTerminated.tap {
                        showReasonInputDialog(
                            context = itemView.context,
                            title = "Nhập lý do chấm dứt hợp đồng",
                            hint = "nhập lý do ở đây"
                        ) { reason ->
                            showConfirmDialog(
                                context = itemView.context,
                                title = "Xác nhận",
                                message = "Bạn có chắc chắn muốn yêu cầu chấm dứt hợp đồng này không?"
                            ) {
                                onRequestTerminate(contract, reason, TerminationStatus.PENDING)
                                notifyTermination(
                                    itemView.context,
                                    contract,
                                    LoaiTaiKhoan.NguoiChoThue,
                                    Default.TypeNotification.TYPE_NOTI_TERMINATED_REQUEST,
                                    "Yêu cầu chấm dứt hợp đồng",
                                    "Hợp đồng với mã hợp đồng ${contract.maHopDong} được yêu cầu chấm dứt bởi người dùng ${contract.nguoiThue.hoTen}"
                                )
                            }
                        }
                    }

                    btnXacNhanTer.tap {

                        showConfirmDialog(
                            context = itemView.context,
                            title = "Xác nhận",
                            message = "Bạn chắc chắn đồng ý chấm dứt hợp đồng này?"
                        ) {
                            llBtnTermination.visibility = View.GONE
                            tvTermination.visibility = View.GONE

                            onRequestTerminate(
                                contract,
                                null,
                                TerminationStatus.APPROVED
                            )
                            onStatusUpdated(
                                contract.maHopDong,
                                ContractStatus.TERMINATED_PROCESSING
                            )
                            notifyTermination(
                                itemView.context,
                                contract,
                                LoaiTaiKhoan.TatCa,
                                Default.TypeNotification.TYPE_NOTI_TERMINATED_CONFIRM,
                                "Xác nhận chấm dứt hợp đồng",
                                "Hợp đồng với mã hợp đồng ${contract.maHopDong} đã được xác nhận chấm dứt bởi ${contract.chuNha.hoTen} và đang được xử lý"
                            )
                        }

                    }
                    btnCancelTer.tap {
                        showConfirmDialog(
                            context = itemView.context,
                            title = "Xác nhận",
                            message = "Bạn chắc chắn từ chối chấm dứt hợp đồng này?"
                        ) {
                            llBtnTermination.visibility = View.GONE
                            tvTermination.visibility = View.GONE

                            onRequestTerminate(contract, null, TerminationStatus.DENIED)
                            notifyTermination(
                                itemView.context,
                                contract,
                                LoaiTaiKhoan.NguoiThue,
                                Default.TypeNotification.TYPE_NOTI_TERMINATED_DENY,
                                "Từ chối chấm dứt hợp đồng",
                                "Hợp đồng với mã hợp đồng ${contract.maHopDong} bị từ chối chấm dứt bởi ${contract.chuNha.hoTen}"
                            )
                        }
                    }

                }

                ContractStatus.PENDING -> {
                    tvRemainingTime.visibility = View.GONE
                    llBtn.visibility =
                        if (contract.hoaDonHopDong.trangThai == InvoiceStatus.PENDING) View.VISIBLE else View.GONE

                    if (isLandlord) {
                        llBtn.visibility = View.GONE
                    }

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
                                ContractStatus.CANCELLED
                            )
                            updateContractList(contractList)
                            notifyPayment(itemView.context, contract)
                        }

                    }
                }

                ContractStatus.EXPIRED -> {
                    tvRemainingTime.text = "Hợp đồng đã hết hạn"
                }

                ContractStatus.TERMINATED -> {
                    tvRemainingTime.text = "Hợp đồng đã chấm dứt"
                }

                ContractStatus.TERMINATED_PROCESSING -> {
                    tvRemainingTime.text = "Hợp đồng đang chờ xử lý chấm dứt"
                    if (contract.yeuCauChamDut == TerminationStatus.APPROVED) {
                        if (!contract.daTaoHoaDonChamDut) {
                            if (isLandlord) {
                                btnCreateBill.visibility = View.VISIBLE
                                btnCreateBill.tap {
                                    val intent = Intent(itemView.context, CreateInvoice::class.java)
                                    intent.putExtra("CONTRACT_ID", contract.maHopDong)
                                    intent.putExtra("chamDutHopDong", "true")
                                    itemView.context.startActivity(intent)
                                }
                            }

                        }
                    }
                }

                ContractStatus.CANCELLED -> {
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

private fun notifyPayment(context: Context, contract: HopDong) {

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

private fun notifyTermination(
    context: Context, contract: HopDong, role: LoaiTaiKhoan,
    loaiThongBao: String,
    tieuDe: String,
    tinNhan: String
) {

    val notification = NotificationModel(
        tieuDe = tieuDe,
        tinNhan = tinNhan,
        ngayGuiThongBao = Calendar.getInstance().time.toString(),
        thoiGian = "0",
        mapLink = null,
        daDoc = false,
        daGui = true,
        idModel = contract.maHopDong,
        loaiThongBao = loaiThongBao
    )

    val factory = ViewModelFactory(context)
    val notificationViewModel = ViewModelProvider(
        context as AppCompatActivity,
        factory
    )[NotificationViewModel::class.java]

    val recipientIds = when (role) {
        LoaiTaiKhoan.NguoiThue -> listOf(contract.nguoiThue.maNguoiDung)
        LoaiTaiKhoan.NguoiChoThue -> listOf(contract.chuNha.maNguoiDung)
        LoaiTaiKhoan.TatCa -> listOf(
            contract.nguoiThue.maNguoiDung,
            contract.chuNha.maNguoiDung
        ) // Gửi cho cả 2
        else -> emptyList()
    }

    recipientIds.forEach { recipientId ->
        notificationViewModel.sendNotification(notification, recipientId)

        notificationViewModel.notificationStatus.observe(context, Observer { isSuccess ->
            if (isSuccess) {
                // Thông báo thành công
                Toast.makeText(context, "Thông báo đã được gửi.", Toast.LENGTH_SHORT).show()
            } else {
                // Thông báo thất bại
                Toast.makeText(context, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
