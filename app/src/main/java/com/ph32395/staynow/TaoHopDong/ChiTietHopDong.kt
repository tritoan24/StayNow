package com.ph32395.staynow.TaoHopDong

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.databinding.ActivityChiTietHopDongBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChiTietHopDong : AppCompatActivity() {
    private var _binding: ActivityChiTietHopDongBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Safely inflate the binding
        _binding = ActivityChiTietHopDongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Additional setup
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Get contract ID from intent
        val contractId = intent.getStringExtra("CONTRACT_ID")

        // Fetch contract details
        contractId?.let { fetchContractDetails(it) }


    }


    @SuppressLint("SetTextI18n")
    private fun renderContractDetails(contractData: HopDong) {
        binding.apply {
            // Contract Information
            tvContractCode.text = "Hợp đồng số: ${contractData.maHopDong}"

            // Landlord Information
            tvLandlordName.text = contractData.chuNha.hoTen
            tvLandlordAddress.text = contractData.chuNha.diaChi
            tvLandlordPhone.text = contractData.chuNha.soDienThoai
            tvLandlordCccd.text = contractData.chuNha.soCCCD
            tvLandlordBirth.text = contractData.chuNha.ngaySinh
            tvLandlordDatecccd.text = contractData.chuNha.ngayCapCCCD

            // Tenant Information
            tvTenantName.text = contractData.nguoiThue.hoTen
            tvTenantAddress.text = contractData.nguoiThue.diaChi
            tvTenantPhone.text = contractData.nguoiThue.soDienThoai
            tvTenantCccd.text = contractData.nguoiThue.soCCCD
            tvTenantBirth.text = contractData.nguoiThue.ngaySinh
            tvTenantDatecccd.text = contractData.nguoiThue.ngayCapCCCD

            // Room Information
            tvRoomName.text = "Tên Phòng: ${contractData.thongtinphong.tenPhong}"
            tvRoomAddress.text =
                "Bên A đồng ý cho bên B thuê 01 phòng ở tại địa chỉ: ${contractData.thongtinphong.diaChiPhong}"

            // Financial Information
            tvRentPrice.text =
                "Giá Thuê: ${formatCurrency(contractData.thongTinTaiChinh.giaThue)}/tháng"
            tvDeposit.text =
                "Tiền Cọc: ${formatCurrency(contractData.thongTinTaiChinh.tienCoc)}/tháng"

            //thông tin giá dịch vụ
            val phiDichVu = contractData.thongTinTaiChinh.phiDichVu
            //in ra thong tin các phí dịch vụ
            for (i in phiDichVu.indices) {
                val tv = TextView(this@ChiTietHopDong)
                tv.text =
                    "${phiDichVu[i].tenDichVu}: ${formatCurrency(phiDichVu[i].giaTien)}/${phiDichVu[i].donVi}"
                tv.textSize = 14f
                tv.setPadding(0, 0, 0, 10)
                tvServiceFee.addView(tv)
            }


            //thời hạn thuê
            tvContractTime.text =
                "Hợp đồng có giá trị kể từ ngày ${contractData.ngayBatDau} đến ngày ${contractData.ngayKetThuc} với thời hạn thuê là ${contractData.thoiHanThue}"


            // Amenities and Furniture
            tvAmenities.text = "Tiện Nghi: ${contractData.tienNghi.joinToString(", ")}"
            tvFurniture.text = "Nội Thất: ${contractData.noiThat.joinToString(", ")}"


            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = dateFormatter.parse(contractData.ngayTao)
            val calendar = Calendar.getInstance()
            if (parsedDate != null) {
                calendar.time = parsedDate
            }

            tvdatecreatecontract.text = "Hôm nay, ngày ${calendar.get(Calendar.DAY_OF_MONTH)}" +
                    " tháng ${calendar.get(Calendar.MONTH) + 1} " +
                    "năm ${calendar.get(Calendar.YEAR)}"


            //điều khoản hợp đồng
            val formattedText = Html.fromHtml(
                contractData.dieuKhoan,
                Html.FROM_HTML_MODE_COMPACT
            ) // Chuyển HTML sang Spanned
            tvContractDieukhoan.append(formattedText)
            tvContractNote.text = "Ghi chú: ${contractData.ghiChu}"


            btnBack.setOnClickListener {
                finish()
            }

        }
    }

    private fun fetchContractDetails(contractId: String) {
        firestore.collection("HopDong").document(contractId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val contractData = HopDong(
                        // Thông tin hợp đồng
                        maHopDong = document.getString("maHopDong") ?: "Chưa cập nhật",
                        ngayTao = document.getString("ngayTao") ?: "Chưa cập nhật",
                        ngayBatDau = document.getString("ngayBatDau") ?: "Chưa cập nhật",
                        ngayKetThuc = document.getString("ngayKetThuc") ?: "Chưa cập nhật",
                        thoiHanThue = document.getString("thoiHanThue") ?: "Chưa cập nhật",
                        dieuKhoan = document.getString("dieuKhoan") ?: "Chưa cập nhật",
                        ghiChu = document.getString("ghiChu") ?: "Chưa cập nhật",

                        // Thông tin chủ nhà
                        chuNha = PersonInfo(
                            hoTen = (document.get("chuNha") as? Map<*, *>)?.get("hoTen") as? String
                                ?: "Chưa cập nhật",
                            diaChi = (document.get("chuNha") as? Map<*, *>)?.get("diaChi") as? String
                                ?: "Chưa cập nhật",
                            soDienThoai = (document.get("chuNha") as? Map<*, *>)?.get("soDienThoai") as? String
                                ?: "Chưa cập nhật",
                            soCCCD = (document.get("chuNha") as? Map<*, *>)?.get("soCCCD") as? String
                                ?: "Chưa cập nhật",
                            ngayCapCCCD = (document.get("chuNha") as? Map<*, *>)?.get("ngayCapCCCD") as? String
                                ?: "Chưa cập nhật",
                            ngaySinh = (document.get("chuNha") as? Map<*, *>)?.get("ngaySinh") as? String
                                ?: "Chưa cập nhật"
                        ),

                        // Thông tin người thuê
                        nguoiThue = PersonInfo(
                            hoTen = (document.get("nguoiThue") as? Map<*, *>)?.get("hoTen") as? String
                                ?: "Chưa cập nhật",
                            diaChi = (document.get("nguoiThue") as? Map<*, *>)?.get("diaChi") as? String
                                ?: "Chưa cập nhật",
                            soDienThoai = (document.get("nguoiThue") as? Map<*, *>)?.get("soDienThoai") as? String
                                ?: "Chưa cập nhật",
                            soCCCD = (document.get("nguoiThue") as? Map<*, *>)?.get("soCCCD") as? String
                                ?: "Chưa cập nhật",
                            ngayCapCCCD = (document.get("nguoiThue") as? Map<*, *>)?.get("ngayCapCCCD") as? String
                                ?: "Chưa cập nhật",
                            ngaySinh = (document.get("nguoiThue") as? Map<*, *>)?.get("ngaySinh") as? String
                                ?: "Chưa cập nhật"
                        ),

                        // Thông tin phòng
                        thongtinphong = RoomInfo(
                            tenPhong = (document.get("thongtinphong") as? Map<*, *>)?.get("tenPhong") as? String
                                ?: "Chưa cập nhật",
                            diaChiPhong = (document.get("thongtinphong") as? Map<*, *>)?.get("diaChiPhong") as? String
                                ?: "Chưa cập nhật",
                            thongTinChiTiet = (document.get("thongtinphong.thongTinChiTiet") as? List<Map<*, *>>)?.map {
                                RoomDetail(
                                    ten = it["ten"] as? String ?: "",
                                    giaTri = it["giaTri"] as? Long ?: 0L,
                                    donVi = it["donVi"] as? String ?: ""
                                )
                            } ?: listOf()
                        ),

                        // Thông tin tài chính
                        thongTinTaiChinh = FinancialInfo(
                            giaThue = (document.get("thongTinTaiChinh") as? Map<*, *>)?.get("giaThue") as? Double
                                ?: 0.0,
                            tienCoc = (document.get("thongTinTaiChinh") as? Map<*, *>)?.get("tienCoc") as? Double
                                ?: 0.0,
                            soNguoio = (document.get("thongTinTaiChinh") as? Map<*, *>)?.get("soNguoiO") as? Int
                                ?: 0,
                            phiDichVu = (document.get("thongTinTaiChinh.phiDichVu") as? List<Map<*, *>>)?.map {
                                UtilityFee(
                                    tenDichVu = it["tenDichVu"] as? String ?: "",
                                    giaTien = it["giaTien"] as? Double ?: 0.0,
                                    donVi = it["donVi"] as? String ?: ""
                                )
                            } ?: listOf()
                        ),

                        // Tiện nghi và nội thất
                        tienNghi = (document.get("tienNghi") as? List<*>)?.map { it.toString() }
                            ?: listOf(),
                        noiThat = (document.get("noiThat") as? List<*>)?.map { it.toString() }
                            ?: listOf()

                    )

                    // Render contract details
                    renderContractDetails(contractData)
                } else {
                    Toast.makeText(this, "Không tìm thấy hợp đồng", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Lỗi: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun formatCurrency(value: Double): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 0
        formatter.currency = java.util.Currency.getInstance("VND")
        return formatter.format(value)
    }
}
