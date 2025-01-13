package com.ph32395.staynow_datn.SuaPhongTro

import DichVuAdapter
import android.Manifest
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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.DichVu.DichVuAddServiceUtil
import com.ph32395.staynow_datn.GioiTinh.GioiTinh
import com.ph32395.staynow_datn.GioiTinh.GioiTinhViewModel
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhong
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongViewModel
import com.ph32395.staynow_datn.Maps.RetrofitInstance
import com.ph32395.staynow_datn.Maps.SuggestionResponse
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.NoiThat.GioiTinhAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.NoiThat.NoiThatAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThatViewModel
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.ChoiceImageAdapter
import com.ph32395.staynow_datn.TaoPhongTro.NhaTroViewModel
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.TaoPhongTro.SimpleHomeAdapter
import com.ph32395.staynow_datn.TaoPhongTro.TaoPhongTro
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.ThongTin.ThongTinAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.TienNghi.TienNghiAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghiViewModel
import com.ph32395.staynow_datn.databinding.ActivitySuaPhongTroBinding
import com.ph32395.staynow_datn.databinding.ActivitySuaPhongTroDonBinding
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.isNotEmpty
import kotlin.text.isNotEmpty
import kotlin.text.toIntOrNull


class SuaPhongTroDon : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivitySuaPhongTroDonBinding
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var noiThatViewModel: NoiThatViewModel
    private lateinit var TienNghiAdapter: TienNghiAdapter
    private lateinit var tienNghiViewModel: TienNghiViewModel
    private lateinit var dichVuAdapter: DichVuAdapter
    private lateinit var thongTinAdapter: ThongTinAdapter
    private lateinit var gioitinhViewModel: GioiTinhViewModel
    private lateinit var gioitinhAdapter: GioiTinhAdapter
    private lateinit var loaiPhongAdapter: LoaiPhongAdapter
    private lateinit var loaiPhongViewModel: LoaiPhongViewModel

    private lateinit var roomDetailsViewModel: RoomDetailsViewModel
    private lateinit var roomId: String

    private var listPhiDichVu = mutableListOf<PhiDichVu>()
    private val dichVuDonViMap = mutableMapOf<String, List<String>>()

    private var pricesMapThongTin = mutableMapOf<String, Pair<ThongTin, Int>>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userId: String

    // Khai báo list để lưu thông tin dịch vụ và tiện nghi đã chọn
    private val selectedTienNghiList = mutableListOf<TienNghi>()  // Lưu tiện nghi
    private val selectedNoiThatList = mutableListOf<NoiThat>()  // Lưu nội thất

    private lateinit var imageAdapter: ChoiceImageAdapter
    private var mutableUriList: MutableList<Uri> = mutableListOf()

    var Ma_gioiTinh = ""
    var TrangThaiPhong = false
    var trangThaiDuyet = ""
    var trangThaiLuu = false
    var trangThaiDC = true
    var soLuotXemPhong = ""

    //Phí thông tin
    var tendichvu = ""
    var soluongdv = 0

    //địa chỉ
    var Dc_quanhuyen = ""
    var Dc_tinhtp = ""

    //mã nhà trọ
    var maNhaTro = ""
    var diaChi = ""
    var diaChiChiTiet = ""
    var maLoaiPhong = ""


    private lateinit var completionAnimation: LottieAnimationView
    private lateinit var loadingUtil: LoadingUtil

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
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


    // Đầu tiên, tạo các biến để lưu giá trị cần select
    private var pendingGioiTinhSelection: String? = null
    private var pendingLoaiPhongSelection: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivitySuaPhongTroDonBinding.inflate(layoutInflater)
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

        //laays tu intent
        roomId =  intent.getStringExtra("roomId").toString()


        //Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)
        loadingUtil.show()


        // Khởi tạo ViewModel
        roomDetailsViewModel = ViewModelProvider(this).get(RoomDetailsViewModel::class.java)

        roomDetailsViewModel.fetchRoomDetails(roomId)

        // Quan sát dữ liệu phòng
        roomDetailsViewModel.roomDetails.observe(this) { roomDetails ->
            roomDetails?.let { populateRoomDetails(it) }
        }

// Khởi tạo dichVuAdapter
        dichVuAdapter = DichVuAdapter(this, emptyList(), this)
        binding.listViewDichVu.adapter = dichVuAdapter

        gioitinhAdapter = GioiTinhAdapter(this, emptyList(),this)
        binding.listViewGioiTinh.adapter = gioitinhAdapter

        loaiPhongAdapter = LoaiPhongAdapter(this, emptyList(),this)
        binding.recyclerViewLoaiPhong.adapter = loaiPhongAdapter

        // Khởi tạo thongTinAdapter
        thongTinAdapter = ThongTinAdapter(this, emptyList(), this)
        binding.listViewThongTin.adapter = thongTinAdapter

        // Khởi tạo NoiThatAdapter
        noiThatAdapter = NoiThatAdapter(this, emptyList(), this)
        binding.recyclerView.adapter = noiThatAdapter

        // Khởi tạo TienNghiAdapter
        TienNghiAdapter = TienNghiAdapter(this, emptyList(), this)
        binding.RcTienNghi.adapter = TienNghiAdapter


        // Khởi tạo RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.RcTienNghi.layoutManager = GridLayoutManager(this, 3)
        binding.listViewDichVu.layoutManager = GridLayoutManager(this, 3)
        binding.listViewThongTin.layoutManager = GridLayoutManager(this, 3)
        binding.imagegeContainer.layoutManager = GridLayoutManager(this, 4)
        binding.listViewGioiTinh.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewLoaiPhong.layoutManager = GridLayoutManager(this, 4)



        // Khởi tạo ViewModel
        noiThatViewModel = ViewModelProvider(this).get(NoiThatViewModel::class.java)
        tienNghiViewModel = ViewModelProvider(this).get(TienNghiViewModel::class.java)
        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)
        loaiPhongViewModel = ViewModelProvider(this).get(LoaiPhongViewModel::class.java)





        binding.btnBack.setOnClickListener {
            finish()
        }
                // Logic khi nhấn nút "Lưu"
        binding.addRoomButton2.setOnClickListener {
            updateRoomInFirestore()
            loadingUtil.show()
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

//nút lưu ở đây
        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->
                    displaySelectedImages(uriList)
                }
        }
        // Inside your fragment or activity
        binding.fabAddDichVu.setOnClickListener {
            DichVuAddServiceUtil.showAddServiceDialog(this@SuaPhongTroDon) { newDichVu ->
                // Directly call the method in your adapter to add the service
                dichVuAdapter.addDichVu(newDichVu)
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


        // Tương tự, thêm observer cho GioiTinh
        gioitinhViewModel.getListGioiTinh().observe(this) { listGioiTinh ->
            if (listGioiTinh != null && listGioiTinh.isNotEmpty()) {
                gioitinhAdapter = GioiTinhAdapter(this, listGioiTinh, this)
                binding.listViewGioiTinh.adapter = gioitinhAdapter

                // Thực hiện select sau khi adapter đã được khởi tạo
                pendingGioiTinhSelection?.let { maGioiTinh ->
                    gioitinhAdapter.selectById(maGioiTinh)
                }
            } else {
                Toast.makeText(this, "Không có dữ liệu Giới Tính", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onAllPricesEntered(prices: List<PhiDichVu>) {
        // Lưu toàn bộ danh sách vào biến
        listPhiDichVu = prices.toMutableList()
        Log.d("PhiDichVu", "Số lượng dịch vụ: ${listPhiDichVu.size}")
        soluongdv = listPhiDichVu.size
        Log.d("PhiDichVu", "Danh sách dịch vụ: $listPhiDichVu")
    }

    override fun onThongTinimfor(prices: List<Pair<ThongTin, Int>>) {
        pricesMapThongTin.clear() // Clear map cũ trước khi thêm dữ liệu mới
        prices.forEach { (thongtin, price) ->
            pricesMapThongTin[thongtin.tenThongTin] = Pair(thongtin, price)
        }
        Log.d("ThongTin", "Số lượng thông tin: ${pricesMapThongTin.size}")
        Log.d("ThongTin", "Danh sách thông tin: $pricesMapThongTin")
    }

    override fun onNoiThatSelected(noiThat: NoiThat, isSelected: Boolean) {
        if (isSelected && !selectedNoiThatList.any { it.maNoiThat == noiThat.maNoiThat }) {
            selectedNoiThatList.add(noiThat)
        } else if (!isSelected) {
            selectedNoiThatList.removeAll { it.maNoiThat == noiThat.maNoiThat }
        }
    }

    override fun onTienNghiSelected(tienNghi: TienNghi, isSelected: Boolean) {
        if (isSelected && !selectedTienNghiList.any { it.maTienNghi == tienNghi.maTienNghi }) {
            selectedTienNghiList.add(tienNghi)
        } else if (!isSelected) {
            selectedTienNghiList.removeAll { it.maTienNghi == tienNghi.maTienNghi }
        }
    }


    override fun onLoaiPhongSelected(
        loaiPhong: com.ph32395.staynow_datn.LoaiPhong.LoaiPhong,
        isSelected: Boolean
    ) {
        maLoaiPhong = loaiPhong.maLoaiPhong.toString()
    }

    override fun onNhaTroSelected(nhaTro: NhaTroModel, isSelected: Boolean) {
        if (isSelected) {
        }
    }

    override fun onGioiTinhSelected(
        gioiTinh: GioiTinh,
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

    private fun populateRoomDetails(roomDetails: PhongTroModel) {
        pendingGioiTinhSelection = roomDetails.maGioiTinh
        pendingLoaiPhongSelection = roomDetails.maLoaiNhaTro

        binding.roomName.setText(roomDetails.tenPhongTro)
        binding.description.setText(roomDetails.moTaChiTiet)

        // Format số tiền trước khi gán vào EditText
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val formattedPrice = numberFormat.format(roomDetails.giaPhong)
        binding.roomPrice.setText(formattedPrice)

        binding.description.setText(roomDetails.moTaChiTiet)
        CurrencyFormatTextWatcher.addTo(binding.roomPrice)
        Log.d("Gia Phong", "Gia Phong formatted: $formattedPrice")

        // Điền thông tin địa chỉ
        diaChi = roomDetails.diaChi
        diaChiChiTiet = roomDetails.diaChiChiTiet
        maNhaTro = roomDetails.maNhaTro
        Dc_quanhuyen = roomDetails.dcQuanHuyen
        Dc_tinhtp = roomDetails.dcTinhTP
        maLoaiPhong = roomDetails.maLoaiNhaTro
        Ma_gioiTinh = roomDetails.maGioiTinh
        TrangThaiPhong = roomDetails.trangThaiPhong
        trangThaiDuyet = roomDetails.trangThaiDuyet
        trangThaiLuu = roomDetails.trangThaiLuu
        soLuotXemPhong = roomDetails.soLuotXemPhong.toString()



        roomDetailsViewModel.getListDichVu()
        // 2. Gọi API để lấy danh sách dịch vụ
        roomDetailsViewModel.getListDichVu()

        // 3. Observe danh sách đơn vị mặc định trước
        roomDetailsViewModel.listDichVu.observe(this) { dichVuList ->
            if (dichVuList != null && dichVuList.isNotEmpty()) {
                // Lưu đơn vị của từng dịch vụ vào Map
                dichVuList.forEach { dichVu ->
                    dichVuDonViMap[dichVu.tenDichVu] = dichVu.donVi
                }
                Log.d("DichVu_Debug", "Đã lưu Map đơn vị: $dichVuDonViMap")
            }
        }

        roomDetailsViewModel.services.observe(this) { services ->
            if (services.isNotEmpty()) {
                // Cập nhật adapter với danh sách phí dịch vụ
                dichVuAdapter.updateList(services.map { service ->
                    val donViList = dichVuDonViMap[service.tenDichVu] ?: listOf(service.donVi)
                    DichVu(
                        maDichVu = "",
                        tenDichVu = service.tenDichVu,
                        iconDichVu = service.iconDichVu,
                        donVi = donViList,
                        trangThai = true
                    )
                })

                // Cập nhật giá tiền
                dichVuAdapter.pricesMap.clear()
                services.forEachIndexed { index, service ->
                    dichVuAdapter.pricesMap[index] = service.soTien to service.donVi
                }

                // Cập nhật listPhiDichVu ngay sau khi load dữ liệu
                onAllPricesEntered(dichVuAdapter.getCurrentPhiDichVu())

                loadingUtil.hide()
            }
        }
        roomDetailsViewModel.imfor.observe(this) { detailsList ->
            if (detailsList.isNotEmpty()) {
                // Cập nhật Adapter
                thongTinAdapter.updateData(detailsList.map { chiTiet ->
                    ThongTin(
                        maThongTin = "",
                        tenThongTin = chiTiet.tenThongTin.toString(),
                        iconThongTin = chiTiet.iconThongTin.toString(),
                        donVi = chiTiet.donVi.toString(),
                        trangThai = true
                    )
                })

                // Cập nhật giá trị
                thongTinAdapter.pricesMap.clear()
                detailsList.forEachIndexed { index, chiTiet ->
                    thongTinAdapter.pricesMap[index] = chiTiet.soLuongDonVi.toLong()
                }

                // Cập nhật pricesMapThongTin ngay sau khi load dữ liệu
                onThongTinimfor(thongTinAdapter.getCurrentThongTin())

                thongTinAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Không có chi tiết thông tin nào", Toast.LENGTH_SHORT).show()
            }
        }

        // Khởi tạo observers
        roomDetailsViewModel.furniture.observe(this) { selectedFurniture ->
            selectedFurniture?.let {
                // Cập nhật adapter
                noiThatAdapter.updateSelectedItems(it)
                // Cập nhật selectedNoiThatList
                selectedNoiThatList.clear()
                selectedNoiThatList.addAll(noiThatAdapter.getSelectedNoiThat())
            }
        }

        roomDetailsViewModel.amenities.observe(this) { selectedAmenities ->
            selectedAmenities?.let {
                // Cập nhật adapter
                TienNghiAdapter.updateSelectedItems(it)
                // Cập nhật selectedTienNghiList
                selectedTienNghiList.clear()
                selectedTienNghiList.addAll(TienNghiAdapter.getSelectedTienNghi())
            }
        }
        // Gọi hàm khởi tạo dữ liệu
        roomDetailsViewModel.initializeSelectedLists(roomId)

        // Quan sát lỗi
        roomDetailsViewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
        // Trong observer của LoaiPhong, thực hiện select sau khi adapter đã được khởi tạo
        loaiPhongViewModel.getListLoaiPhong().observe(this) { listLoaiPhong ->
            if (listLoaiPhong != null && listLoaiPhong.isNotEmpty()) {
                loaiPhongAdapter = LoaiPhongAdapter(this, listLoaiPhong, this)
                binding.recyclerViewLoaiPhong.adapter = loaiPhongAdapter

                // Thực hiện select sau khi adapter đã được khởi tạo
                pendingLoaiPhongSelection?.let { maLoaiPhong ->
                    loaiPhongAdapter.selectById(maLoaiPhong)
                }
            } else {
                Toast.makeText(this, "Không có dữ liệu Loại Phòng", Toast.LENGTH_SHORT).show()
            }
        }

        Log.d("DiaChi", "Giới tính : $Ma_gioiTinh")
        Log.d( "DiaChi", "Mã nhà trọ : $maNhaTro")
        gioitinhAdapter.selectById(Ma_gioiTinh)
        loaiPhongAdapter.selectById(maLoaiPhong)
        binding.roomAddress.setText(diaChiChiTiet)





        // Hiển thị danh sách ảnh
        displaySelectedImages(roomDetails.imageUrls.map { Uri.parse(it) })
    }

    private fun updateRoomInFirestore() {
        val roomName = binding.roomName.text.toString()
        val roomPrice = CurrencyFormatTextWatcher.getUnformattedValue(binding.roomPrice).toInt()
        val description = binding.description.text.toString()
        if (!validateInputs(roomName, roomPrice, description)) return

        loadingUtil.show()

        // Check for duplicate room names
        checkDuplicateRoomName(roomName) { isDuplicate ->
            if (isDuplicate) {
                loadingUtil.hide()
                binding.roomName.error = "Tên phòng đã tồn tại trong nhà trọ này"
                return@checkDuplicateRoomName
            }

            handleImageUpload()
        }
    }

    private fun validateInputs(roomName: String, roomPrice: Int, description: String): Boolean {
        when {
            roomName.isEmpty() -> {
                binding.roomName.error = "Tên phòng trọ không được để trống"
                return false
            }
            roomPrice == 0 -> {
                binding.roomPrice.error = "Giá phòng không được để trống"
                return false
            }
            roomPrice < 0 -> {
                Toast.makeText(this, "Giá phòng không được âm", Toast.LENGTH_SHORT).show()
                return false
            }
            description.isEmpty() -> {
                binding.description.error = "Mô tả không được để trống"
                return false
            }
            selectedNoiThatList.isEmpty() -> {
                Toast.makeText(this, "Vui lòng chọn nội thất", Toast.LENGTH_SHORT).show()
                return false
            }
            selectedTienNghiList.isEmpty() -> {
                Toast.makeText(this, "Vui lòng chọn tiện nghi", Toast.LENGTH_SHORT).show()
                return false
            }
            soluongdv < 1 -> {
                Toast.makeText(this, "Vui lòng nhập giá dịch vụ", Toast.LENGTH_SHORT).show()
                return false
            }
            pricesMapThongTin.isEmpty() || pricesMapThongTin.size < 4 -> {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return false
            }
            Ma_gioiTinh.isEmpty() -> {
                Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show()
                return false
            }
            mutableUriList.isEmpty() -> {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun checkDuplicateRoomName(roomName: String, callback: (Boolean) -> Unit) {
        firestore.collection("PhongTro")
            .whereEqualTo("maNhaTro", maNhaTro)
            .whereEqualTo("tenPhongTro", roomName)
            .get()
            .addOnSuccessListener { documents ->
                val duplicateExists = documents.documents.any { it.id != roomId }
                callback(duplicateExists)
            }
            .addOnFailureListener { e ->
                loadingUtil.hide()
                Toast.makeText(this, "Lỗi khi kiểm tra tên phòng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleImageUpload() {
        if (mutableUriList.isEmpty()) {
            updateRoomDataInFirestore(emptyList())
            return
        }

        val newImageUrls = mutableListOf<String>()
        var uploadedCount = 0

        mutableUriList.forEachIndexed { index, uri ->
            when {
                isNetworkUrl(uri) -> {
                    newImageUrls.add(uri.toString())
                    uploadedCount++
                    checkUploadCompletion(uploadedCount, newImageUrls)
                }
                else -> uploadLocalImage(uri, index, newImageUrls, uploadedCount) { count ->
                    uploadedCount = count
                    checkUploadCompletion(uploadedCount, newImageUrls)
                }
            }
        }
    }

    private fun isNetworkUrl(uri: Uri): Boolean =
        uri.scheme?.toLowerCase() in listOf("http", "https")

    private fun uploadLocalImage(
        uri: Uri,
        index: Int,
        newImageUrls: MutableList<String>,
        currentCount: Int,
        onUploadComplete: (Int) -> Unit
    ) {
        val fileName = "room/${userId}_${System.currentTimeMillis()}_$index.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

        storageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                newImageUrls.add(downloadUri.toString())
                onUploadComplete(currentCount + 1)
            }
            .addOnFailureListener { e ->
                loadingUtil.hide()
                Toast.makeText(this, "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUploadCompletion(uploadedCount: Int, newImageUrls: List<String>) {
        if (uploadedCount == mutableUriList.size) {
            updateRoomDataInFirestore(newImageUrls)
        }
    }

    private fun updateRoomDataInFirestore(imageUrls: List<String>) {
        // Show loading trên main thread
        CoroutineScope(Dispatchers.Main).launch {
            loadingUtil.show()

            // Chuyển sang IO thread để xử lý dữ liệu
            withContext(Dispatchers.IO) {
                try {
                    // 1. Xóa dữ liệu cũ
                    val collections = listOf(
                        "PhongTroTienNghi",
                        "PhongTroNoiThat",
                        "PhiDichVu",
                        "ChiTietThongTin"
                    )

                    collections.forEach { collection ->
                        val snapshot = firestore.collection(collection)
                            .whereEqualTo("maPhongTro", roomId)
                            .get()
                            .await()

                        val batch = firestore.batch()
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().await()
                    }

                    // 2. Tạo batch mới để thêm dữ liệu
                    val updateBatch = firestore.batch()

                    // 3. Cập nhật dữ liệu chính của phòng
                    updateMainRoomData(updateBatch, imageUrls)

                    // 4. Thêm dữ liệu mới
                    updateRelatedCollections(updateBatch)

                    // 5. Thực thi batch update
                    updateBatch.commit().await()

                    // Chuyển về main thread để cập nhật UI
                    withContext(Dispatchers.Main) {
                        handleSuccessfulUpdate()
                    }
                } catch (e: Exception) {
                    // Chuyển về main thread để hiển thị lỗi
                    withContext(Dispatchers.Main) {
                        handleUpdateError(e)
                    }
                }
            }
        }
    }


    private fun updateMainRoomData(batch: WriteBatch, imageUrls: List<String>) {
        val roomRef = firestore.collection("PhongTro").document(roomId)
        val roomData = hashMapOf(
            "tenPhongTro" to binding.roomName.text.toString(),
            "giaPhong" to CurrencyFormatTextWatcher.getUnformattedValue(binding.roomPrice).toInt(),
            "moTaChiTiet" to binding.description.text.toString(),
            "maNguoiDung" to userId,
            "diaChi" to diaChi,
            "diaChiChiTiet" to diaChiChiTiet,
            "dcQuanHuyen" to Dc_quanhuyen,
            "dcTinhTP" to Dc_tinhtp,
            "maLoaiNhaTro" to maLoaiPhong,
            "maNhaTro" to maNhaTro,
            "maGioiTinh" to Ma_gioiTinh,
            "trangThaiLuu" to trangThaiLuu,
            "trangThaiDuyet" to trangThaiDuyet,
            "ngayCapNhat" to System.currentTimeMillis(),
            "soLuotXemPhong" to soLuotXemPhong.toInt(),
            "trangThaiDC" to trangThaiDC,
            "trangThaiPhong" to TrangThaiPhong,
            "imageUrls" to imageUrls
        )
        batch.update(roomRef, roomData)
    }

    private fun updateRelatedCollections(batch: WriteBatch) {
        // Add TienNghi
        selectedTienNghiList.forEach { tienNghi ->
            val newRef = firestore.collection("PhongTroTienNghi").document()
            batch.set(newRef, hashMapOf(
                "maPhongTro" to roomId,
                "maTienNghi" to tienNghi.maTienNghi.toString()
            ))
        }

        // Add NoiThat
        selectedNoiThatList.forEach { noithat ->
            val newRef = firestore.collection("PhongTroNoiThat").document()
            batch.set(newRef, hashMapOf(
                "maPhongTro" to roomId,
                "maNoiThat" to noithat.maNoiThat.toString()
            ))
        }

        // Add PhiDichVu
        listPhiDichVu.forEach { phiDichVu ->
            val newRef = firestore.collection("PhiDichVu").document()
            batch.set(newRef, hashMapOf(
                "maPhongTro" to roomId,
                "tenDichVu" to phiDichVu.tenDichVu,
                "donVi" to phiDichVu.donVi,
                "soTien" to phiDichVu.soTien,
                "iconDichVu" to phiDichVu.iconDichVu
            ))
        }

        // Add ThongTin
        pricesMapThongTin.forEach { (_, pair) ->
            val (thongtin, price) = pair
            val newRef = firestore.collection("ChiTietThongTin").document()
            batch.set(newRef, hashMapOf(
                "soLuongDonVi" to price,
                "maPhongTro" to roomId,
                "tenThongTin" to thongtin.tenThongTin,
                "iconThongTin" to thongtin.iconThongTin,
                "donVi" to thongtin.donVi
            ))
        }
    }

    private suspend fun deleteExistingData(): Task<Void> {
        val collectionsToDelete = listOf(
            "PhongTroTienNghi",
            "PhongTroNoiThat",
            "PhiDichVu",
            "ChiTietThongTin"
        )

        val queryTasks = collectionsToDelete.map { collection ->
            firestore.collection(collection)
                .whereEqualTo("maPhongTro", roomId)
                .get()
        }

        return Tasks.whenAll(queryTasks).continueWithTask { task ->
            val batch = firestore.batch()

            // Xử lý an toàn hơn với null check
            task.result?.let { results ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    val querySnapshots = results as List<QuerySnapshot>

                    querySnapshots.forEach { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            batch.delete(document.reference)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Lỗi khi cast kết quả: ${e.message}")
                }
            }

            batch.commit()
        }
    }

    private fun handleSuccessfulUpdate() {
        loadingUtil.hide()
        completionAnimation.apply {
            setAnimation("done.json")
            visibility = View.VISIBLE
            playAnimation()

            postDelayed({
                visibility = View.GONE
                Toast.makeText(
                    this@SuaPhongTroDon,
                    "Cập nhật phòng trọ thành công!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }, 2000)
        }
    }

    private fun handleUpdateError(e: Exception) {
        loadingUtil.hide()
        Toast.makeText(
            this@SuaPhongTroDon,
            "Lỗi khi cập nhật dữ liệu: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
        Log.e("Firestore", "Lỗi khi cập nhật dữ liệu: ${e.message}")
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
                            diaChiChiTiet = description

                            // Kiểm tra "số" trong 10 ký tự đầu tiên của description
                            if (description.substring(0, minOf(description.length, 10))
                                    .contains("số", ignoreCase = true)
                            ) {
                                val indexOfNgo = description.indexOf("ngõ", ignoreCase = true)
                                if (indexOfNgo != -1) {
                                    // Nếu tìm thấy "ngõ", lấy từ vị trí "ngõ"
                                    diaChi = description.substring(indexOfNgo)
                                } else {
                                    // Nếu không tìm thấy "ngõ", mặc định lấy secondaryText
                                    diaChi = secondaryText
                                }
                            } else {
                                // Nếu không thỏa mãn điều kiện "số", mặc định lấy secondaryText
                                diaChi = secondaryText
                            }
                        }
                        binding.roomAddress.setText(diaChiChiTiet)

                        // Set full and partial addresses

                        val adapter = ArrayAdapter(
                            this@SuaPhongTroDon,
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
                            this@SuaPhongTroDon,
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

                    diaChi = detailedAddress
                    diaChiChiTiet = diachict
                    binding.roomAddress.setText(diaChiChiTiet)

                    Log.d("Location", "Địa chỉ cụ thể: $detailedAddress")
                    Log.d("Location", "Địa chỉ cụ thể chi tiets: $diachict")

                }
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Lỗi khi lấy địa chỉ: ${e.message}")
        }
    }
}
