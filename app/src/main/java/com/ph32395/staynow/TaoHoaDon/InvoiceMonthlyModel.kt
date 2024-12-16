package com.ph32395.staynow.TaoHoaDon

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Activity.RoomDetailActivity
import com.ph32395.staynow.ChucNangTimKiem.BottomSheetFilter
import com.ph32395.staynow.ChucNangTimKiem.BottomSheetFragment
import com.ph32395.staynow.Maps.GeocodeResponse
import com.ph32395.staynow.Maps.RetrofitInstance
import com.ph32395.staynow.Maps.SuggestionResponse
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.TaoHopDong.UtilityFeeDetail
import com.ph32395.staynow.databinding.ActivityMapsBinding
import com.ph32395.staynow.databinding.BottomSheetDialogDetaillRoomAboveMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class InvoiceMonthlyModel(
    val idHoaDon: String = "",
    val idNguoiNhan: String = "",
    val idNguoiGui: String = "",
    val idHopDong: String = "",
    val tenKhachHang: String = "",
    val tenPhong: String = "",
    val ngayTaoHoaDon: String = "",
    val hoaDonThang: Int = 1,
    val phiCoDinh: List<UtilityFeeDetail> = emptyList(),
    val phiBienDong: List<UtilityFeeDetail> = emptyList(),
    val tongTien: Double = 0.0,
    val trangThai: InvoiceStatus = InvoiceStatus.PENDING,
    val tienPhong: Double = 0.0,
    val tienCoc: Double = 0.0,
    val tongPhiCoDinh: Double = 0.0,
    val tongPhiBienDong: Double = 0.0,
    val tongTienDichVu: Double = 0.0,
    val kieuHoadon: String = "Hoa Don Hang Thang",
    val paymentDate: String = "",
    val soDienCu: Int = 0,
    val soNuocCu: Int = 0,
    val soDienMoi: Int = 0,
    val soNuocMoi: Int = 0,
    val soDienTieuThu: Int = 0,
    val soNuocTieuThu: Int = 0,
    val tienGiam: Double = 0.0,
    val tienThem: Double = 0.0,
    val ghiChu: String = "",
) : Serializable