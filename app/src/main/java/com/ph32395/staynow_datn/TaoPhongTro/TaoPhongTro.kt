package com.ph32395.staynow_datn.TaoPhongTro

import DichVuAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.DichVu.DichVuAddServiceUtil
import com.ph32395.staynow_datn.DichVu.DichVuViewModel
import com.ph32395.staynow_datn.GioiTinh.GioiTinhViewModel
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongViewModel
import com.ph32395.staynow_datn.Maps.RetrofitInstance
import com.ph32395.staynow_datn.Maps.SuggestionResponse
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.NoiThat.GioiTinhAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.NoiThat.NoiThatAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThatViewModel
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.ThongTin.ThongTinAdapter
import com.ph32395.staynow_datn.ThongTin.ThongTinViewModel
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.TienNghi.TienNghiAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghiViewModel
import com.ph32395.staynow_datn.databinding.ActivityTaoPhongTroBinding
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class TaoPhongTro : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding: ActivityTaoPhongTroBinding
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var noiThatViewModel: NoiThatViewModel
    private lateinit var TienNghiAdapter: TienNghiAdapter
    private lateinit var tienNghiViewModel: TienNghiViewModel
    private lateinit var DichVuAdapter: DichVuAdapter
    private lateinit var dichVuViewModel: DichVuViewModel
    private lateinit var thongTinAdapter: ThongTinAdapter
    private lateinit var thongTinViewModel: ThongTinViewModel
    private lateinit var loaiPhongAdapter: LoaiPhongAdapter
    private lateinit var loaiPhongViewModel: LoaiPhongViewModel
    private lateinit var gioitinhViewModel: GioiTinhViewModel
    private lateinit var gioitinhAdapter: GioiTinhAdapter


    private var listPhiDichVu = mutableListOf<PhiDichVu>()
    val listDichVu: List<DichVu> = mutableListOf()

    // Khai bao bien luu tru du lieu thong tin

    private val pricesMapThongTin = mutableMapOf<String, Pair<ThongTin, Int>>()


    private lateinit var mAuth: FirebaseAuth
    private lateinit var userId: String


    // Khai báo list để lưu thông tin dịch vụ và tiện nghi đã chọn
    private val selectedDichVuList = mutableListOf<DichVu>()  // Lưu dịch vụ cùng giá
    private val selectedTienNghiList = mutableListOf<TienNghi>()  // Lưu tiện nghi
    private val selectedNoiThatList = mutableListOf<NoiThat>()  // Lưu nội thất

    private lateinit var imageAdapter: ChoiceImageAdapter
    private var mutableUriList: MutableList<Uri> = mutableListOf()

    var Dc_quanhuyen = ""
    var Dc_tinhtp = ""
    var ward = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var addressDetail = ""
    var fullAddressct = ""
    var fullAddressDeltail = ""
    var Ma_loaiphong = ""
    var Ma_gioiTinh = ""
    var TrangThaiPhong = false

    //Phí thông tin
    var tendichvu = ""
    var giadichvu = 0
    var icondichvu = ""
    var donvidv = ""
    var soluongdv = 0


    private lateinit var completionAnimation: LottieAnimationView
    private lateinit var loadingUtil: LoadingUtil


    private lateinit var mMap: GoogleMap
    private var lastText: String? = null
    private val delay: Long = 60
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    var addresses2 = mutableListOf<Pair<String, PhongTroModel>>()
    val suggestionRoom = mutableListOf<PhongTroModel>()
    private val apiKeys = mutableListOf<String>()
    private val TAG = "zzzzzzMapsActivityzzzzzz"
    private var currentKeyIndex = 0
    private var usageMap = mutableMapOf<String, Int>() // Theo dõi số lượng requests mỗi key

    init {
        apiKeys.forEach { usageMap[it] = 0 } // Khởi tạo bộ đếm cho mỗi key
    }

    private fun getCurrentApiKey(): String {
        return apiKeys[currentKeyIndex]
    }

    private fun rotateApiKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        Log.d("API_KEY_SWITCH", "Chuyển sang API Key: ${apiKeys[currentKeyIndex]}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivityTaoPhongTroBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        val apiKey = applicationContext.getString(R.string.api_key_1)
        val apiKey2 = applicationContext.getString(R.string.api_key_2)
        val apiKey3 = applicationContext.getString(R.string.api_key_3)
        val apiKey4 = applicationContext.getString(R.string.api_key_4)
        val apiKey5 = applicationContext.getString(R.string.api_key_5)
        val apiKey6 = applicationContext.getString(R.string.api_key_6)
        val apiKey7 = applicationContext.getString(R.string.api_key_7)
        val apiKey8 = applicationContext.getString(R.string.api_key_8)
        val apiKey9 = applicationContext.getString(R.string.api_key_9)
        val apiKey10 = applicationContext.getString(R.string.api_key_10)
        val apiKey11 = applicationContext.getString(R.string.api_key_11)
        val apiKey12 = applicationContext.getString(R.string.api_key_12)
        val apiKey13 = applicationContext.getString(R.string.api_key_13)
        val apiKey14 = applicationContext.getString(R.string.api_key_14)
        val apiKey15 = applicationContext.getString(R.string.api_key_15)
        val apiKey16 = applicationContext.getString(R.string.api_key_16)
        val apiKey17 = applicationContext.getString(R.string.api_key_17)
        val apiKey18 = applicationContext.getString(R.string.api_key_18)
        val apiKey19 = applicationContext.getString(R.string.api_key_19)
        val apiKey20 = applicationContext.getString(R.string.api_key_20)
        val apiKey21 = applicationContext.getString(R.string.api_key_21)
        val apiKey22 = applicationContext.getString(R.string.api_key_22)
        val apiKey23 = applicationContext.getString(R.string.api_key_23)
        val apiKey24 = applicationContext.getString(R.string.api_key_24)
        apiKeys.add(apiKey)
        apiKeys.add(apiKey2)
        apiKeys.add(apiKey3)
        apiKeys.add(apiKey4)
        apiKeys.add(apiKey5)
        apiKeys.add(apiKey6)
        apiKeys.add(apiKey7)
        apiKeys.add(apiKey8)
        apiKeys.add(apiKey9)
        apiKeys.add(apiKey10)
        apiKeys.add(apiKey11)
        apiKeys.add(apiKey12)
        apiKeys.add(apiKey13)
        apiKeys.add(apiKey14)
        apiKeys.add(apiKey15)
        apiKeys.add(apiKey16)
        apiKeys.add(apiKey17)
        apiKeys.add(apiKey18)
        apiKeys.add(apiKey19)
        apiKeys.add(apiKey20)
        apiKeys.add(apiKey21)
        apiKeys.add(apiKey22)
        apiKeys.add(apiKey23)
        apiKeys.add(apiKey24)

        //Goi y tim kiem start
        autoCompleteTextView = binding.autoComplete
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchSuggestions(s.toString())
//                suggestionsRoom(s.toString().trim(), listRoom)
//                suggestionQuan(s.toString().trim(), listQuan)

            }
        })
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }

        //tạo sự kiện khi ấn vào nút update_button
        binding.btngetIndex.setOnClickListener {
            doiToaDoRaViTriCuThe(it)
        }

        firestore = FirebaseFirestore.getInstance()


        completionAnimation = findViewById(R.id.completionAnimation)


        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userId = currentUser?.uid ?: ""
        Log.d("UID", "UID: $userId")

        //Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)
        loadingUtil.show()


        // Khởi tạo RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.RcTienNghi.layoutManager = GridLayoutManager(this, 3)
        binding.listViewDichVu.layoutManager = GridLayoutManager(this, 3)
        binding.listViewThongTin.layoutManager = GridLayoutManager(this, 3)
        binding.imagegeContainer.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerViewLoaiPhong.layoutManager = GridLayoutManager(this, 4)
        binding.listViewGioiTinh.layoutManager = GridLayoutManager(this, 3)

        // Khởi tạo ViewModel
        noiThatViewModel = ViewModelProvider(this).get(NoiThatViewModel::class.java)
        tienNghiViewModel = ViewModelProvider(this).get(TienNghiViewModel::class.java)
        dichVuViewModel = ViewModelProvider(this).get(DichVuViewModel::class.java)
        thongTinViewModel = ViewModelProvider(this).get(ThongTinViewModel::class.java)
        loaiPhongViewModel = ViewModelProvider(this).get(LoaiPhongViewModel::class.java)
        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)

        //lấy mã người dùng từ mAuth


        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->  // `uriList` là danh sách Uri của ảnh đã chọn
                    displaySelectedImages(uriList)

                }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.roomName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Hiển thị Snackbar khi trường nhập liệu được focus
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Hãy nhập tên phòng, ví dụ: Phòng 101", Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
//        // định dạng số tiền nhập vào
        CurrencyFormatTextWatcher.addTo(binding.roomPrice)

        // Logic khi nhấn nút "Lưu"
        binding.addRoomButton.setOnClickListener {
            TrangThaiPhong = false
            // Gọi hàm lưu phòng với trạng thái là "Lưu"
            saveRoomToFirestore(isSaved = true, trangThaiPhong = TrangThaiPhong)
            loadingUtil.show()
        }

        // Logic khi nhấn nút "Đăng"
        binding.addRoomButton2.setOnClickListener {
            TrangThaiPhong = false
            saveRoomToFirestore(isSaved = false, trangThaiPhong = TrangThaiPhong)
            loadingUtil.show()
        }


        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->
                    displaySelectedImages(uriList)
                }
        }
        // Inside your fragment or activity
        binding.fabAddDichVu.setOnClickListener {
            DichVuAddServiceUtil.showAddServiceDialog(this@TaoPhongTro) { newDichVu ->
                // Directly call the method in your adapter to add the service
                DichVuAdapter.addDichVu(newDichVu)
            }
        }


        // Quan sát LiveData từ ViewModel
        noiThatViewModel.getListNoiThat().observe(this, Observer { noiThatList ->
            if (noiThatList != null && noiThatList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                noiThatAdapter = NoiThatAdapter(this, noiThatList, this)
                binding.recyclerView.adapter = noiThatAdapter

            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu Nội Thất", Toast.LENGTH_SHORT).show()
            }
        })

        tienNghiViewModel.getListTienNghi().observe(this, Observer { TienNghiList ->
            if (TienNghiList != null && TienNghiList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                TienNghiAdapter = TienNghiAdapter(this, TienNghiList, this)
                binding.RcTienNghi.adapter = TienNghiAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu Tiện Nghi", Toast.LENGTH_SHORT).show()
            }
        })

        dichVuViewModel.getListDichVu().observe(this, Observer { dichVuList ->
            if (dichVuList != null && dichVuList.isNotEmpty()) {
                // Nếu adapter đã tồn tại, chỉ cập nhật list
                if (::DichVuAdapter.isInitialized) {
                    DichVuAdapter.updateList(dichVuList)
                } else {
                    // Nếu chưa có adapter thì tạo mới
                    DichVuAdapter = DichVuAdapter(this, dichVuList, this)
                    binding.listViewDichVu.adapter = DichVuAdapter
                }
            } else {
                Toast.makeText(this, "Không có dữ liệu Dịch Vụ ", Toast.LENGTH_SHORT).show()
            }
        })

        thongTinViewModel.getListThongTin().observe(this, Observer { thongTinList ->
            if (thongTinList != null && thongTinList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                thongTinAdapter = ThongTinAdapter(this, thongTinList, this)
                binding.listViewThongTin.adapter = thongTinAdapter
                loadingUtil.hide()
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu Thông Tin", Toast.LENGTH_SHORT).show()
            }
        })

        loaiPhongViewModel.getListLoaiPhong().observe(this, Observer { listLoaiPhong ->
            if (listLoaiPhong != null && listLoaiPhong.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                loaiPhongAdapter = LoaiPhongAdapter(this, listLoaiPhong, this)
                binding.recyclerViewLoaiPhong.adapter = loaiPhongAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu Loại Phòng", Toast.LENGTH_SHORT).show()
            }
        })
        gioitinhViewModel.getListGioiTinh().observe(this, Observer { gioiTinhList ->
            if (gioiTinhList != null && gioiTinhList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                gioitinhAdapter = GioiTinhAdapter(this, gioiTinhList, this)
                binding.listViewGioiTinh.adapter = gioitinhAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu Giới Tính", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onAllPricesEntered(prices: List<PhiDichVu>) {
        // Lưu toàn bộ danh sách vào biến
        listPhiDichVu = prices.toMutableList()
        Log.d("PhiDichVu", "Số lượng dịch vụ: ${listPhiDichVu.size}")
        soluongdv = listPhiDichVu.size
    }

    override fun onThongTinimfor(prices: List<Pair<ThongTin, Int>>) {
        prices.forEach { (thongtin, price) ->
            // Lưu thông tin dịch vụ và giá vào pricesMap
            pricesMapThongTin[thongtin.maThongTin.toString()] = Pair(thongtin, price)
        }

    }

    override fun onNoiThatSelected(noiThat: NoiThat, isSelected: Boolean) {
        if (isSelected) {
            selectedNoiThatList.add(noiThat)  // Thêm nội thất vào list nếu được chọn
        } else {
            selectedNoiThatList.remove(noiThat)  // Bỏ nội thất nếu không được chọn nữa
        }
    }

    override fun onTienNghiSelected(
        tienNghi: TienNghi,
        isSelected: Boolean
    ) {
        if (isSelected) {
            selectedTienNghiList.add(tienNghi)  // Thêm tiện nghi vào list nếu được chọn
        } else {
            selectedTienNghiList.remove(tienNghi)  // Bỏ tiện nghi nếu không được chọn nữa
        }
    }

    override fun onLoaiPhongSelected(
        loaiPhong: com.ph32395.staynow_datn.LoaiPhong.LoaiPhong,
        isSelected: Boolean
    ) {
        Ma_loaiphong = loaiPhong.maLoaiPhong.toString()
    }

    override fun onGioiTinhSelected(
        gioiTinh: com.ph32395.staynow_datn.GioiTinh.GioiTinh,
        isSelected: Boolean
    ) {
        Ma_gioiTinh = gioiTinh.maGioiTinh.toString()
    }


    private fun displaySelectedImages(uriList: List<Uri>) {
        // Chuyển uriList thành mutable list để có thể thay đổi (xóa ảnh)
        mutableUriList = uriList.toMutableList()

        // Khởi tạo adapter và truyền vào callback xóa
        imageAdapter = ChoiceImageAdapter(mutableUriList) { position ->
            // Xử lý xóa ảnh
            mutableUriList.removeAt(position) // Cập nhật lại uriList trong Activity
            imageAdapter.notifyItemRemoved(position) // Thông báo adapter rằng ảnh đã bị xóa


            Toast.makeText(this, "Ảnh đã bị xóa", Toast.LENGTH_SHORT).show()
        }
        binding.imagegeContainer.adapter = imageAdapter

    }

    private fun saveRoomToFirestore(isSaved: Boolean, trangThaiPhong: Boolean) {
        val roomName = binding.roomName.text.toString()
        val roomPrice = CurrencyFormatTextWatcher.getUnformattedValue(binding.roomPrice).toInt()
        val description = binding.description.text.toString()
        val dcQuanhuyen = Dc_quanhuyen
        val dcTinhtp = Dc_tinhtp


        //validate
        if (fullAddressDeltail.isEmpty()) {
            binding.roomAddress.error = "Địa chỉ không được để trống"
            return
        }
        if (roomName.isEmpty()) {
            binding.roomName.error = "Tên phòng trọ không được để trống"
            return
        }
        if (roomPrice == 0) {
            binding.roomPrice.error = "Giá phòng không được để trống"
            return
        }
        if(roomPrice < 0){
            Toast.makeText(this, "Giá phòng không được âm", Toast.LENGTH_SHORT).show()
        }
        if (description.isEmpty()) {
            binding.description.error = "Mô tả không được để trống"
            return
        }
        if (selectedNoiThatList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn nội thất", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTienNghiList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn tiện nghi", Toast.LENGTH_SHORT).show()
            return
        }
        //nếu chưa nhập đủ giá cho 4 dịch vụ thì thông báo
        if (soluongdv < 1) {
            Toast.makeText(this, "Vui lòng nhập giá dịch vụ", Toast.LENGTH_SHORT).show()
            return
        }
        if (pricesMapThongTin.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        // nếu chưa nhập đủ giá cho 4 thông tin thì thông báo
        if (pricesMapThongTin.size < 4) {
            Toast.makeText(this, "Vui lòng nhập giá thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (Ma_loaiphong.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại phòng", Toast.LENGTH_SHORT).show()
            return
        }
        if (Ma_gioiTinh.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show()
            return
        }
        if (mutableUriList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show()
            return
        }




        val ThoiGian_taophong = System.currentTimeMillis()
        val Ngay_capnhat = System.currentTimeMillis()
        val So_luotxemphong = 0


        val Trang_thailuu = if (isSaved) true else false
        val Trang_thaiduyet = if (isSaved) "" else "ChoDuyet"


        // Tạo danh sách để chứa URL của các ảnh đã tải lên
        val imageUrls = mutableListOf<String>()

        // Biến đếm số ảnh đã tải lên thành công
        var uploadedImagesCount = 0


        // Vòng lặp để upload từng ảnh từ danh sách mutableUriList
        for ((index, uri) in mutableUriList.withIndex()) {
            val fileName = "room/${userId}_${System.currentTimeMillis()}_$index.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            // Upload ảnh lên Firebase Storage
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Lấy URL của ảnh sau khi tải lên thành công
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        uploadedImagesCount++

                        // Nếu đã upload tất cả ảnh, tiến hành lưu dữ liệu phòng
                        if (uploadedImagesCount == mutableUriList.size) {
                            saveRoomDataToFirestore(
                                roomName,
                                roomPrice,
                                description,
                                imageUrls,
                                dcQuanhuyen,
                                dcTinhtp,
                                Trang_thailuu,
                                Trang_thaiduyet,
                                ThoiGian_taophong,
                                Ngay_capnhat,
                                So_luotxemphong,
                                isSaved,
                                trangThaiPhong
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun saveRoomDataToFirestore(
        roomName: String,
        roomPrice: Int,
        description: String,
        imageUrls: List<String>,
        dc_quanhuyen: String,
        dc_tinhtp: String,
        Trang_thailuu: Boolean,
        Trang_thaiduyet: String,
        ThoiGian_taophong: Long,
        Ngay_capnhat: Long,
        So_luotxemphong: Int,
        isSaved: Boolean,
        trangThaiPhong: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                // Chuẩn bị dữ liệu để lưu vào Firestore
                val roomData = hashMapOf(
                    "tenPhongTro" to roomName,
                    "giaPhong" to roomPrice,
                    "moTaChiTiet" to description,
                    "maNguoiDung" to userId,
                    "diaChi" to fullAddressct,
                    "diaChiChiTiet" to fullAddressDeltail,
                    "trangThaiDC" to false,
                    "dcQuanHuyen" to dc_quanhuyen,
                    "dcTinhTP" to dc_tinhtp,
                    "maLoaiNhaTro" to Ma_loaiphong,
                    "maGioiTinh" to Ma_gioiTinh,
                    "trangThaiLuu" to Trang_thailuu,
                    "trangThaiDuyet" to Trang_thaiduyet,
                    "thoiGianTaoPhong" to ThoiGian_taophong,
                    "ngayCapNhat" to Ngay_capnhat,
                    "soLuotXemPhong" to So_luotxemphong,
                    "imageUrls" to imageUrls,
                    "trangThaiPhong" to trangThaiPhong,
                    "trangThaiDC" to true
                )


                // Lưu phòng trọ
                val roomTask = firestore.collection("PhongTro").add(roomData).await()
                val maPhongTro = roomTask.id

                // Lưu tiện nghi, nội thất, dịch vụ
                saveTienNghiToFirestore(maPhongTro, selectedTienNghiList)
                saveNoiThatToFirestore(maPhongTro, selectedNoiThatList)
                savePhiDichVuToFirestore(maPhongTro)
                savePhiThongTinToFirestore(maPhongTro)

                withContext(Dispatchers.Main) {
                    // Ẩn loadingAnimation và hiển thị completionAnimation sau khi lưu thành công
                    loadingUtil.show()
                    completionAnimation.setAnimation("done.json")
                    completionAnimation.visibility = View.VISIBLE
                    completionAnimation.playAnimation()

                    // Sau khi animation hoàn thành trong 2 giây
                    completionAnimation.postDelayed({
                        completionAnimation.visibility = View.GONE

                        // Toast thông báo thành công
                        Toast.makeText(
                            this@TaoPhongTro,
                            "Tất cả dữ liệu đã được lưu thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Điều hướng sau khi lưu thành công
                        val intent = Intent(this@TaoPhongTro, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish()

                    }, 2000) // Thời gian delay 2 giây
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingUtil.hide()
                    Toast.makeText(
                        this@TaoPhongTro,
                        "Lỗi khi lưu dữ liệu: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun saveTienNghiToFirestore(
        maPhongTro: String,
        selectedTienNghi: List<TienNghi>,
    ) {
        for (tienNghi in selectedTienNghi) {
            val phongTroTienNghi = PhongTroTienNghi(
                maPhongTro = maPhongTro,
                maTienNghi = tienNghi.maTienNghi.toString()
            )

            firestore.collection("PhongTroTienNghi").add(phongTroTienNghi)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi lưu tiện nghi: ${e.message}")
                }
        }
    }

    fun saveNoiThatToFirestore(
        maPhongTro: String,
        selectedNoiThat: List<NoiThat>,
    ) {
        for (noithat in selectedNoiThat) {
            val phongTroNoiThat = PhongTroNoiThat(
                maPhongTro = maPhongTro,
                maNoiThat = noithat.maNoiThat.toString()
            )

            firestore.collection("PhongTroNoiThat").add(phongTroNoiThat)
                .addOnSuccessListener {
                    Log.d("Firestore", "Lưu tiện nghi ${noithat.tenNoiThat} thành công!")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi lưu tiện nghi: ${e.message}")
                }
        }
    }

    fun savePhiDichVuToFirestore(maPhongTro: String) {
        // Sử dụng listPhiDichVu đã lưu
        listPhiDichVu.forEach { phiDichVu ->
            // Tạo một bản copy với mã phòng trọ mới
            val newPhiDichVu = phiDichVu.copy(maPhongTro = maPhongTro)

            firestore.collection("PhiDichVu")
                .add(newPhiDichVu)
                .addOnSuccessListener {
                    Log.d("Firestore", "Thêm phí dịch vụ thành công")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Lỗi khi thêm phí dịch vụ", exception)
                }
        }
    }

    fun savePhiThongTinToFirestore(maPhongTro: String) {
        val totalTasks = pricesMapThongTin.size

        pricesMapThongTin.forEach { (maThongTin, pair) ->
            val thongtin = pair.first
            val price = pair.second

            val chiTietThongTin = ChiTietThongTin(
                soLuongDonVi = price,
                maPhongTro = maPhongTro,
                tenThongTin = thongtin.tenThongTin,
                iconThongTin = thongtin.iconThongTin,
                donVi = thongtin.donVi
            )

            firestore.collection("ChiTietThongTin")
                .add(chiTietThongTin)
                .addOnSuccessListener {

                }
                .addOnFailureListener { exception ->
                }
        }
        Log.d("PhiThongTin", "Tổng số tasks: $totalTasks")
    }

    private fun fetchSuggestions(query: String) {
        if (query.length < 2) return

        val apiKey = getCurrentApiKey()
        RetrofitInstance.api.getPlace(apiKey, query)
            .enqueue(object : Callback<SuggestionResponse> {
                override fun onResponse(
                    call: Call<SuggestionResponse>,
                    response: Response<SuggestionResponse>
                ) {
                    val currentKey = getCurrentApiKey()

                    // Kiểm tra giới hạn trong header
                    val remainingRequests =
                        response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()
                    if (remainingRequests != null) {
                        usageMap[currentKey] = 998 - remainingRequests
                        Log.d("RateLimit", "Key $currentKey còn $remainingRequests requests")
                    }

                    // Nếu vượt quá giới hạn, chuyển sang key mới
                    if (remainingRequests != null && remainingRequests <= 0) {
                        rotateApiKey()
                        fetchSuggestions(query) // Gọi lại với key mới
                        return
                    }
                    Log.d(TAG, "onResponse: response $response")
                    if (response.isSuccessful && response.body()?.status == "OK") {
                        val suggestions = response.body()?.predictions ?: emptyList()
                        Log.d(TAG, "onResponse: suggestions $suggestions")

                        // Lấy description và secondary_text từ mỗi prediction
                        val list = suggestions.map { prediction ->
                            val description = prediction.description
                            val secondaryText =
                                prediction.structured_formatting?.secondary_text ?: "N/A"
                            val district = prediction.compound?.district ?: "N/A" // Lấy district
                            val province = prediction.compound?.province ?: "N/A" // Lấy province


                            Dc_tinhtp = province
                            Dc_quanhuyen = district


                            //log quan huyen
                            Log.d(TAG, "Quan huyen: $Dc_quanhuyen")
                            Log.d(TAG, "TinhTP: $Dc_tinhtp")

                            // Gán giá trị cho fullAddressDeltail
                            fullAddressDeltail = description

                            // Kiểm tra "số" trong 10 ký tự đầu tiên của description
                            if (description.substring(0, minOf(description.length, 10))
                                    .contains("số", ignoreCase = true)
                            ) {
                                val indexOfNgo = description.indexOf("ngõ", ignoreCase = true)
                                if (indexOfNgo != -1) {
                                    // Nếu tìm thấy "ngõ", lấy từ vị trí "ngõ"
                                    fullAddressct = description.substring(indexOfNgo)
                                } else {
                                    // Nếu không tìm thấy "ngõ", mặc định lấy secondaryText
                                    fullAddressct = secondaryText
                                }
                            } else {
                                // Nếu không thỏa mãn điều kiện "số", mặc định lấy secondaryText
                                fullAddressct = secondaryText
                            }
                        }
                        Log.d(TAG, "onResponse: addressDetails $fullAddressDeltail")
                        Log.d(TAG, "onResponse: addressDetails $fullAddressct")
                        binding.roomAddress.setText(fullAddressDeltail)

                        // Set full and partial addresses

                        val adapter = ArrayAdapter(
                            this@TaoPhongTro,
                            android.R.layout.simple_list_item_1,
                            list
                        )
                        binding.autoComplete.setAdapter(adapter)
                        //địa chỉ chi tiết fullAddressDeltail
                        //địa chỉ bỏ đi địa chỉ đầu addressDetail
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
                    }
                    // Logic xử lý response như cũ
                    if (response.isSuccessful && response.body()?.status == "OK") {
                        val suggestions = response.body()?.predictions ?: emptyList()
                        val list = suggestions.map { it.description }

                        // Tạo adapter và set cho AutoCompleteTextView
                        val adapter = ArrayAdapter(
                            this@TaoPhongTro,
                            android.R.layout.simple_list_item_1,
                            list
                        )
                        binding.autoComplete.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<SuggestionResponse>, t: Throwable) {
                    Log.e("Retrofit", "API call failed: ${t.message}")
                }
            })
    }

    // Yêu cầu quyền truy cập vị trí
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Nếu quyền được cấp, lấy vị trí
                getCurrentLocation()
            } else {
                Log.e("Permission", "Quyền truy cập vị trí bị từ chối.")
            }
        }

    // Hàm lấy vị trí người dùng
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Nếu chưa có quyền truy cập, yêu cầu quyền
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")


            } else {
                Log.e("Location", "Không lấy được vị trí")
            }
        }
    }

    // Hàm được gọi khi người dùng nhấn nút Cập nhật vị trí
    fun onUpdateLocationClick(view: View) {
        // Hiện ProgressBar trong lúc đang lấy và cập nhật vị trí
//        binding.progressBar.visibility = View.VISIBLE
        getCurrentLocation()
    }

    fun doiToaDoRaViTriCuThe(view: View) {


        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Lấy các thành phần của địa chỉ
                val streetName = address.thoroughfare ?: ""       // Tên đường hoặc ngõ
                val subLocality = address.subLocality ?: ""       // Khu vực nhỏ hơn trong thành phố
                val locality = address.locality ?: ""             // Thành phố hoặc thị xã
                val adminArea = address.adminArea ?: ""           // Tỉnh/thành phố
                val countryName = address.countryName ?: ""       // Tên quốc gia
                Dc_tinhtp = address.adminArea ?: ""          // Tỉnh/Thành phố
                Dc_quanhuyen = address.subAdminArea ?: ""

                //log quan huyen
                Log.d(TAG, "Quan huyen: $Dc_quanhuyen")
                Log.d(TAG, "TinhTP: ${Dc_tinhtp}")

                // Tạo địa chỉ cụ thể kết hợp các thành phần
                val detailedAddress =
                    listOf(streetName, subLocality, locality, adminArea, countryName)
                        .filter { it.isNotEmpty() }                  // Lọc bỏ các thành phần trống
                        .joinToString(", ")                          // Ghép lại thành chuỗi, cách nhau bằng dấu phẩy

                val diachict = address.getAddressLine(0);
                // Kiểm tra nếu địa chỉ cụ thể không trống thì cập nhật Firestore
                if (detailedAddress.isNotEmpty() && diachict.isNotEmpty()) {

                    fullAddressct = detailedAddress
                    fullAddressDeltail = diachict
                    binding.roomAddress.setText(fullAddressDeltail)

                    Log.d("Location", "Địa chỉ cụ thể: $detailedAddress")
                    Log.d("Location", "Địa chỉ cụ thể chi tiets: $diachict")

                }
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Lỗi khi lấy địa chỉ: ${e.message}")
        }
    }

}




