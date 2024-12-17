import android.Manifest
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R

//import android.Manifest
//import android.content.pm.PackageManager
//import android.util.Log
//import android.view.View
//import android.widget.ArrayAdapter
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.app.ActivityCompat
//import com.ph32395.staynow.Maps.RetrofitInstance
//import com.ph32395.staynow.Maps.SuggestionResponse
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.io.IOException
//
//private fun fetchSuggestions(query: String) {
//    if (query.length < 2) return
//
//    val apiKey = getCurrentApiKey()
//    _root_ide_package_.com.ph32395.staynow.Maps.RetrofitInstance.api.getPlace(apiKey, query)
//        .enqueue(object :
//            _root_ide_package_.retrofit2.Callback<_root_ide_package_.com.ph32395.staynow.Maps.SuggestionResponse> {
//            override fun onResponse(
//                call: _root_ide_package_.retrofit2.Call<_root_ide_package_.com.ph32395.staynow.Maps.SuggestionResponse>,
//                response: _root_ide_package_.retrofit2.Response<_root_ide_package_.com.ph32395.staynow.Maps.SuggestionResponse>
//            ) {
//                val currentKey = getCurrentApiKey()
//
//                // Kiểm tra giới hạn trong header
//                val remainingRequests =
//                    response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()
//                if (remainingRequests != null) {
//                    usageMap[currentKey] = 998 - remainingRequests
//                    _root_ide_package_.android.util.Log.d("RateLimit", "Key $currentKey còn $remainingRequests requests")
//                }
//
//                // Nếu vượt quá giới hạn, chuyển sang key mới
//                if (remainingRequests != null && remainingRequests <= 0) {
//                    rotateApiKey()
//                    fetchSuggestions(query) // Gọi lại với key mới
//                    return
//                }
//                _root_ide_package_.android.util.Log.d(TAG, "onResponse: response $response")
//                if (response.isSuccessful && response.body()?.status == "OK") {
//                    val suggestions = response.body()?.predictions ?: emptyList()
//                    _root_ide_package_.android.util.Log.d(TAG, "onResponse: suggestions $suggestions ")
//                    val list = suggestions.map { it.description }
//                    _root_ide_package_.android.util.Log.d(TAG, "onResponse: list $list")
//                    // Split address logic
//                    if (list.isNotEmpty()) {
//                        val fullAddress = list.first() // Take the first suggestion
//
//                        // Split the address
//                        val addressParts = fullAddress.split(" ")
//                        val fullAddressDetail = fullAddress
//
//                        // Remove the first part (house number) to create addressDetail
//                        fullAddressct = addressParts.drop(1).joinToString(" ")
//                        _root_ide_package_.android.util.Log.d(TAG, "onResponse: addressDetail $fullAddressct")
//                        _root_ide_package_.android.util.Log.d(TAG, "onResponse: addessfulldetail $fullAddressDetail")
//                        binding.roomAddress.setText(fullAddressDetail)
//
//                    }
//
//                    // Set full and partial addresses
//
//                    val adapter = _root_ide_package_.android.widget.ArrayAdapter(
//                        this@TaoPhongTro,
//                        android.R.layout.simple_list_item_1,
//                        list
//                    )
//                    binding.autoComplete.setAdapter(adapter)
//                    //địa chỉ chi tiết fullAddressDeltail
//                    //địa chỉ bỏ đi địa chỉ đầu addressDetail
//                    adapter.notifyDataSetChanged()
//                } else {
//                    _root_ide_package_.android.util.Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
//                }
//                // Logic xử lý response như cũ
//                if (response.isSuccessful && response.body()?.status == "OK") {
//                    val suggestions = response.body()?.predictions ?: emptyList()
//                    val list = suggestions.map { it.description }
//
//                    // Tạo adapter và set cho AutoCompleteTextView
//                    val adapter = _root_ide_package_.android.widget.ArrayAdapter(
//                        this@TaoPhongTro,
//                        android.R.layout.simple_list_item_1,
//                        list
//                    )
//                    binding.autoComplete.setAdapter(adapter)
//                    adapter.notifyDataSetChanged()
//                }
//            }
//
//            override fun onFailure(call: _root_ide_package_.retrofit2.Call<_root_ide_package_.com.ph32395.staynow.Maps.SuggestionResponse>, t: Throwable) {
//                _root_ide_package_.android.util.Log.e("Retrofit", "API call failed: ${t.message}")
//            }
//        })
//}
//
//// Yêu cầu quyền truy cập vị trí
//private val requestPermissionLauncher =
//    registerForActivityResult(_root_ide_package_.androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            // Nếu quyền được cấp, lấy vị trí
//            getCurrentLocation()
//        } else {
//            _root_ide_package_.android.util.Log.e("Permission", "Quyền truy cập vị trí bị từ chối.")
//        }
//    }
//
//// Hàm lấy vị trí người dùng
//private fun getCurrentLocation() {
//    if (_root_ide_package_.androidx.core.app.ActivityCompat.checkSelfPermission(
//            this,
//            _root_ide_package_.android.Manifest.permission.ACCESS_FINE_LOCATION
//        ) != _root_ide_package_.android.content.pm.PackageManager.PERMISSION_GRANTED &&
//        _root_ide_package_.androidx.core.app.ActivityCompat.checkSelfPermission(
//            this,
//            _root_ide_package_.android.Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != _root_ide_package_.android.content.pm.PackageManager.PERMISSION_GRANTED
//    ) {
//        // Nếu chưa có quyền truy cập, yêu cầu quyền
//        return
//    }
//
//    fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
//        if (location != null) {
//            latitude = location.latitude
//            longitude = location.longitude
//            _root_ide_package_.android.util.Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
//
//
//        } else {
//            _root_ide_package_.android.util.Log.e("Location", "Không lấy được vị trí")
//        }
//    }
//}
//
//// Hàm được gọi khi người dùng nhấn nút Cập nhật vị trí
//fun onUpdateLocationClick(view: _root_ide_package_.android.view.View) {
//    // Hiện ProgressBar trong lúc đang lấy và cập nhật vị trí
////        binding.progressBar.visibility = View.VISIBLE
//    getCurrentLocation()
//}
//
//fun doiToaDoRaViTriCuThe(view: _root_ide_package_.android.view.View) {
//
//
//    try {
//        val geocoder = _root_ide_package_.android.location.Geocoder(
//            this,
//            _root_ide_package_.java.util.Locale.getDefault()
//        )
//        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//
//        if (!addresses.isNullOrEmpty()) {
//            val address = addresses[0]
//
//            // Lấy các thành phần của địa chỉ
//            val streetName = address.thoroughfare ?: ""       // Tên đường hoặc ngõ
//            val subLocality = address.subLocality ?: ""       // Khu vực nhỏ hơn trong thành phố
//            val locality = address.locality ?: ""             // Thành phố hoặc thị xã
//            val adminArea = address.adminArea ?: ""           // Tỉnh/thành phố
//            val countryName = address.countryName ?: ""       // Tên quốc gia
//
//            // Tạo địa chỉ cụ thể kết hợp các thành phần
//            val detailedAddress =
//                listOf(streetName, subLocality, locality, adminArea, countryName)
//                    .filter { it.isNotEmpty() }                  // Lọc bỏ các thành phần trống
//                    .joinToString(", ")                          // Ghép lại thành chuỗi, cách nhau bằng dấu phẩy
//
//            val diachict = address.getAddressLine(0);
//            // Kiểm tra nếu địa chỉ cụ thể không trống thì cập nhật Firestore
//            if (detailedAddress.isNotEmpty() && diachict.isNotEmpty()) {
//
//                fullAddressct = detailedAddress
//                fullAddressDeltail = diachict
//                binding.roomAddress.setText(fullAddressDeltail)
//
//                _root_ide_package_.android.util.Log.d("Location", "Địa chỉ cụ thể: $detailedAddress")
//                _root_ide_package_.android.util.Log.d("Location", "Địa chỉ cụ thể chi tiets: $diachict")
//            }
//        }
//    } catch (e: _root_ide_package_.java.io.IOException) {
//        _root_ide_package_.android.util.Log.e("Geocoder", "Lỗi khi lấy địa chỉ: ${e.message}")
//    }
//}






//// Khởi tạo FusedLocationProviderClient
//fusedLocationClient =
//LocationServices.getFusedLocationProviderClient(this)
//val apiKey24 = applicationContext.getString(R.string.api_key_1)
//apiKeys.add(apiKey24)
//
////Goi y tim kiem start
//autoCompleteTextView = binding.autoComplete
//autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
//    override fun afterTextChanged(s: android.text.Editable?) {}
//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        fetchSuggestions(s.toString())
//    }
//})
//if (ActivityCompat.checkSelfPermission(
//this,
//Manifest.permission.ACCESS_FINE_LOCATION
//) != PackageManager.PERMISSION_GRANTED
//) {
//    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//} else {
//    getCurrentLocation()
//}
//
////tạo sự kiện khi ấn vào nút update_button
//binding.btngetIndex.setOnClickListener {
//    doiToaDoRaViTriCuThe(it)
//}


//a2

//private lateinit var mMap: GoogleMap
//private var lastText: String? = null
//private val delay: Long = 60
//private val handler = Handler(Looper.getMainLooper())
//private lateinit var autoCompleteTextView: AutoCompleteTextView
//var addresses2 = mutableListOf<Pair<String, PhongTroModel>>()
//val suggestionRoom = mutableListOf<PhongTroModel>()
//private val apiKeys = mutableListOf<String>()
//private val TAG = "zzzzzzMapsActivityzzzzzz"
//private var currentKeyIndex = 0
//private var usageMap = mutableMapOf<String, Int>() // Theo dõi số lượng requests mỗi key
//
//init {
//    apiKeys.forEach { usageMap[it] = 0 } // Khởi tạo bộ đếm cho mỗi key
//}
//
//private fun getCurrentApiKey(): String {
//    return apiKeys[currentKeyIndex]
//}
//
//private fun rotateApiKey() {
//    currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
//    Log.d("API_KEY_SWITCH", "Chuyển sang API Key: ${apiKeys[currentKeyIndex]}")
//}



//
//private fun saveRoomToFirestore(isSaved: Boolean, trangThaiPhong: Boolean) {
//    val roomName = binding.roomName.text.toString()
//    val roomPrice = CurrencyFormatTextWatcher.getUnformattedValue(binding.roomPrice).toInt()
//    val description = binding.description.text.toString()
//
//    //validate
//    if (roomName.isEmpty()) {
//        binding.roomName.error = "Tên phòng trọ không được để trống"
//        return
//    }
//    if (roomPrice == 0) {
//        binding.roomPrice.error = "Giá phòng không được để trống"
//        return
//    }
//    if (description.isEmpty()) {
//        binding.description.error = "Mô tả không được để trống"
//        return
//    }
//    if (fullAddressDeltail.isEmpty()) {
//        binding.roomAddress.error = "Địa chỉ không được để trống"
//        return
//    }
//    if (selectedNoiThatList.isEmpty()) {
//        Toast.makeText(this, "Vui lòng chọn nội thất", Toast.LENGTH_SHORT).show()
//        return
//    }
//    if (selectedTienNghiList.isEmpty()) {
//        Toast.makeText(this, "Vui lòng chọn tiện nghi", Toast.LENGTH_SHORT).show()
//        return
//    }
//    //nếu chưa nhập đủ giá cho 4 dịch vụ thì thông báo
//    if (soluongdv < 1) {
//        Toast.makeText(this, "Vui lòng nhập giá dịch vụ", Toast.LENGTH_SHORT).show()
//        return
//    }
//    if (pricesMapThongTin.isEmpty()) {
//        Toast.makeText(this, "Vui lòng nhập giá thông tin", Toast.LENGTH_SHORT).show()
//        return
//    }
//    // nếu chưa nhập đủ giá cho 4 thông tin thì thông báo
//    if (pricesMapThongTin.size < 4) {
//        Toast.makeText(this, "Vui lòng nhập giá thông tin", Toast.LENGTH_SHORT).show()
//        return
//    }
//    if (Ma_loaiphong.isEmpty()) {
//        Toast.makeText(this, "Vui lòng chọn loại phòng", Toast.LENGTH_SHORT).show()
//        return
//    }
//    if (Ma_gioiTinh.isEmpty()) {
//        Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show()
//        return
//    }
//    if (mutableUriList.isEmpty()) {
//        Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show()
//        return
//    }
//
//
//    // Hiển thị hoạt ảnh loading khi bắt đầu tải dữ liệu
//    loadingAnimation.visibility = View.VISIBLE
//    loadingAnimation.setAnimation("loading.json")
//    loadingAnimation.playAnimation()
//
//
//    val ThoiGian_taophong = System.currentTimeMillis()
//    val Ngay_capnhat = System.currentTimeMillis()
//    val So_luotxemphong = 0
//
//
//    val Trang_thailuu = if (isSaved) true else false
//    val Trang_thaiduyet = if (isSaved) "" else "ChoDuyet"
//
//
//    // Tạo danh sách để chứa URL của các ảnh đã tải lên
//    val imageUrls = mutableListOf<String>()
//
//    // Biến đếm số ảnh đã tải lên thành công
//    var uploadedImagesCount = 0
//
//
//    // Vòng lặp để upload từng ảnh từ danh sách mutableUriList
//    for ((index, uri) in mutableUriList.withIndex()) {
//        val fileName = "room/${userId}_${System.currentTimeMillis()}_$index.jpg"
//        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)
//
//        // Upload ảnh lên Firebase Storage
//        storageRef.putFile(uri)
//            .addOnSuccessListener { taskSnapshot ->
//                // Lấy URL của ảnh sau khi tải lên thành công
//                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
//                    imageUrls.add(downloadUri.toString())
//                    uploadedImagesCount++
//
//                    // Nếu đã upload tất cả ảnh, tiến hành lưu dữ liệu phòng
//                    if (uploadedImagesCount == mutableUriList.size) {
//                        saveRoomDataToFirestore(
//                            roomName,
//                            roomPrice,
//                            description,
//                            imageUrls,
//                            Trang_thailuu,
//                            Trang_thaiduyet,
//                            ThoiGian_taophong,
//                            Ngay_capnhat,
//                            So_luotxemphong,
//                            isSaved,
//                            trangThaiPhong
//                        )
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//    }
//}


// ccccc

//        // Quan sát LiveData từ ViewModel
//        viewModel.noiThatList().observe(this, Observer { noiThatList ->
//            if (noiThatList != null && noiThatList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                noiThatAdapter = NoiThatAdapter(this, noiThatList, this)
//                binding.recyclerView.adapter = noiThatAdapter
//
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        tienNghiViewModel.getListTienNghi().observe(this, Observer { TienNghiList ->
//            if (TienNghiList != null && TienNghiList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                TienNghiAdapter = TienNghiAdapter(this, TienNghiList, this)
//                binding.RcTienNghi.adapter = TienNghiAdapter
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        dichVuViewModel.getListDichVu().observe(this, Observer { dichVuList ->
//            if (dichVuList != null && dichVuList.isNotEmpty()) {
//                // Nếu adapter đã tồn tại, chỉ cập nhật list
//                if (::DichVuAdapter.isInitialized) {
//                    DichVuAdapter.updateList(dichVuList)
//                } else {
//                    // Nếu chưa có adapter thì tạo mới
//                    DichVuAdapter = DichVuAdapter(this, dichVuList, this)
//                    binding.listViewDichVu.adapter = DichVuAdapter
//                }
//            } else {
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        thongTinViewModel.getListThongTin().observe(this, Observer { thongTinList ->
//            if (thongTinList != null && thongTinList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                thongTinAdapter = ThongTinAdapter(this, thongTinList, this)
//                binding.listViewThongTin.adapter = thongTinAdapter
//                loadingUtil.hide()
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        loaiPhongViewModel.getListLoaiPhong().observe(this, Observer { listLoaiPhong ->
//            if (listLoaiPhong != null && listLoaiPhong.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                loaiPhongAdapter = LoaiPhongAdapter(this, listLoaiPhong, this)
//                binding.recyclerViewLoaiPhong.adapter = loaiPhongAdapter
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//        gioitinhViewModel.getListGioiTinh().observe(this, Observer { gioiTinhList ->
//            if (gioiTinhList != null && gioiTinhList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                gioitinhAdapter = GioiTinhAdapter(this, gioiTinhList, this)
//                binding.listViewGioiTinh.adapter = gioitinhAdapter
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
