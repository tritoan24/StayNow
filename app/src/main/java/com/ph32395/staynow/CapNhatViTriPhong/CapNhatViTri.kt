package com.ph32395.staynow.CapNhatViTriPhong

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.databinding.ActivityCapNhatViTriBinding
import java.util.Locale

class CapNhatViTri : AppCompatActivity() {

    private lateinit var binding: ActivityCapNhatViTriBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private val Trang_thaidc = true;
    private var phongTroId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo View Binding
        binding = ActivityCapNhatViTriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //tạo sự kiện khi ấn vào nút update_button
        binding.updateButton.setOnClickListener {
            doiToaDoRaViTriCuThe(it)
        }

        // Nhận ID phòng trọ từ Intent

        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền truy cập
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Nếu đã có quyền, lấy vị trí
            getCurrentLocation()
        }
        //Giúp tôi đổi từ tọa độ ra vị trí cụ thể
        binding.diaChi.setText("Địa chỉ của bạn")




        // Sử dụng View Binding để tham chiếu tới view
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Yêu cầu quyền truy cập vị trí
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Nếu quyền được cấp, lấy vị trí
            getCurrentLocation()
        } else {
            Log.e("Permission", "Quyền truy cập vị trí bị từ chối.")
        }
    }

    // Hàm lấy vị trí người dùng
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền truy cập, yêu cầu quyền
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")

            } else {
                Log.e("Location", "Không lấy được vị trí")
            }
        }
    }

    // Cập nhật vị trí vào Firestore
    private fun updateLocationInFirestore(diachi: String, diachict: String) {

        phongTroId = intent.getStringExtra("PHONG_TRO_ID")
        if (phongTroId != null) {

            val PhongTro = firestore.collection("PhongTro").document(phongTroId!!)

            PhongTro.update(mapOf(
                "Dia_chi" to diachi,
                "Dia_chichitiet" to diachict,
                "Trang_thai_dc" to Trang_thaidc
            )).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cập nhật vị trí thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Lỗi cập nhật vị trí", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }

        }else {
            Toast.makeText(this, "Không tìm thấy ID phòng trọ", Toast.LENGTH_SHORT).show()
            return
        }


    }

    // Hàm được gọi khi người dùng nhấn nút Cập nhật vị trí
    fun onUpdateLocationClick(view: View) {
        // Hiện ProgressBar trong lúc đang lấy và cập nhật vị trí
        binding.progressBar.visibility = View.VISIBLE
        getCurrentLocation()
    }
    fun doiToaDoRaViTriCuThe(view: View) {
        val latitude = binding.latitude.text.toString().toDoubleOrNull()
        val longitude = binding.longitude.text.toString().toDoubleOrNull()

        if (latitude == null || longitude == null) {
            binding.diaChi.setText("Vị trí không hợp lệ")
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val diachict = address.getAddressLine(0);

            // Lấy các thành phần của địa chỉ
            val streetName = address.thoroughfare ?: ""       // Tên đường hoặc ngõ
            val subLocality = address.subLocality ?: ""       // Khu vực nhỏ hơn trong thành phố
            val locality = address.locality ?: ""             // Thành phố hoặc thị xã
            val adminArea = address.adminArea ?: ""           // Tỉnh/thành phố
            val countryName = address.countryName ?: ""       // Tên quốc gia

            // Tạo địa chỉ cụ thể kết hợp các thành phần
            val detailedAddress = listOf(streetName, subLocality, locality, adminArea, countryName)
                .filter { it.isNotEmpty() }                  // Lọc bỏ các thành phần trống
                .joinToString(", ")                          // Ghép lại thành chuỗi, cách nhau bằng dấu phẩy

            binding.diaChi.setText(detailedAddress)
            binding.diaChiDayDu.setText(address.getAddressLine(0))

            //cap nhat vi tri vao firestore
            updateLocationInFirestore(detailedAddress, diachict)
        } else {
            binding.diaChi.setText("Không tìm thấy địa chỉ")
        }
    }


}