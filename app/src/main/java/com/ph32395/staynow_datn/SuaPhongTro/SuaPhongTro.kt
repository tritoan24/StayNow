package com.ph32395.staynow_datn.SuaPhongTro

import DichVuAdapter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
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
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.NoiThat.GioiTinhAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.NoiThat.NoiThatAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThatViewModel
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroAdapter
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin
import com.ph32395.staynow_datn.TaoPhongTro.ChoiceImageAdapter
import com.ph32395.staynow_datn.TaoPhongTro.NhaTroViewModel
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.TaoPhongTro.SimpleHomeAdapter
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.ThongTin.ThongTinAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.TienNghi.TienNghiAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghiViewModel
import com.ph32395.staynow_datn.databinding.ActivitySuaPhongTroBinding
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.isNotEmpty


class SuaPhongTro : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivitySuaPhongTroBinding
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var noiThatViewModel: NoiThatViewModel
    private lateinit var TienNghiAdapter: TienNghiAdapter
    private lateinit var tienNghiViewModel: TienNghiViewModel
    private lateinit var dichVuAdapter: DichVuAdapter
    private lateinit var thongTinAdapter: ThongTinAdapter
    private lateinit var gioitinhViewModel: GioiTinhViewModel
    private lateinit var gioitinhAdapter: GioiTinhAdapter
    private lateinit var nhatroViewModel: NhaTroViewModel
    private lateinit var nhatroAdapter: SimpleHomeAdapter

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivitySuaPhongTroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        completionAnimation = findViewById(R.id.completionAnimation)
        // 1. Khởi tạo các thành phần cơ bản
        initializeBasicComponents()

        // 2. Thiết lập RecyclerViews
        setupRecyclerViews()

        // 3. Khởi tạo các ViewModel
        initializeViewModels()

        // 4. Load dữ liệu phòng và xử lý các quan sát liên quan
        setupRoomDetailsObservers()

        // 5. Thiết lập các observers khác
        setupOtherObservers()

        // 6. Thiết lập các sự kiện click
        setupClickListeners()
    }

    private fun initializeBasicComponents() {
        // Khởi tạo Firebase
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userId = currentUser?.uid ?: ""

        // Lấy roomId từ intent
        roomId = intent.getStringExtra("roomId").toString()

        // Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)
        loadingUtil.show()

        // Khởi tạo các adapter với danh sách rỗng
        dichVuAdapter = DichVuAdapter(this, emptyList(), this)
        thongTinAdapter = ThongTinAdapter(this, emptyList(), this)
        gioitinhAdapter = GioiTinhAdapter(this, emptyList(), this)
        nhatroAdapter = SimpleHomeAdapter(this, emptyList(), this)
    }

    private fun setupRecyclerViews() {
        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)
            RcTienNghi.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)
            listViewDichVu.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)
            listViewThongTin.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)
            imagegeContainer.layoutManager = GridLayoutManager(this@SuaPhongTro, 4)
            listViewGioiTinh.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)
            listViewNhaTro.layoutManager = GridLayoutManager(this@SuaPhongTro, 3)

            // Set adapters
            listViewDichVu.adapter = dichVuAdapter
            listViewThongTin.adapter = thongTinAdapter
            listViewGioiTinh.adapter = gioitinhAdapter
            listViewNhaTro.adapter = nhatroAdapter
        }
    }

    private fun initializeViewModels() {
        roomDetailsViewModel = ViewModelProvider(this).get(RoomDetailsViewModel::class.java)
        noiThatViewModel = ViewModelProvider(this).get(NoiThatViewModel::class.java)
        tienNghiViewModel = ViewModelProvider(this).get(TienNghiViewModel::class.java)
        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)
        nhatroViewModel = ViewModelProvider(this).get(NhaTroViewModel::class.java)
    }

    private fun setupRoomDetailsObservers() {
        // 1. Quan sát dữ liệu phòng chính
        roomDetailsViewModel.roomDetails.observe(this) { roomDetails ->
            roomDetails?.let { details ->
                populateRoomDetails(details)
                // Sau khi có thông tin phòng, mới load nhà trọ
                nhatroViewModel.getAllNhaTroByUserId(userId)
            }
        }

        // 2. Load dữ liệu phòng
        roomDetailsViewModel.fetchRoomDetails(roomId)

        // 3. Khởi tạo danh sách đã chọn
        roomDetailsViewModel.initializeSelectedLists(roomId)

        // 4. Quan sát các thành phần của phòng
        observeRoomComponents()
    }

    private fun observeRoomComponents() {
        // Quan sát dịch vụ
        roomDetailsViewModel.listDichVu.observe(this) { dichVuList ->
            if (dichVuList != null && dichVuList.isNotEmpty()) {
                dichVuList.forEach { dichVu ->
                    dichVuDonViMap[dichVu.tenDichVu] = dichVu.donVi
                }
            }
        }

        // Quan sát services
        roomDetailsViewModel.services.observe(this) { services ->
            if (services.isNotEmpty()) {
                updateDichVuAdapter(services)
            }
        }

        // Quan sát thông tin
        roomDetailsViewModel.imfor.observe(this) { detailsList ->
            if (detailsList.isNotEmpty()) {
                updateThongTinAdapter(detailsList)
            }
        }

        // Quan sát nội thất và tiện nghi
        setupFurnitureAndAmenitiesObservers()
    }

    private fun setupOtherObservers() {
        nhatroViewModel.listNhaTro.observe(this) { nhatroList ->
            nhatroAdapter.updateList(nhatroList)
            // Add this check - select the item after the list is updated
            if (maNhaTro.isNotEmpty()) {
                nhatroAdapter.selectById(maNhaTro)
            }
        }
        nhatroViewModel.selectedNhaTroDetails.observe(this) { nhaTro ->
            nhaTro?.let {
                updateNhaTroDetails(it)
            }
        }

        // Observe nội thất
        noiThatViewModel.getListNoiThat().observe(this) { noiThatList ->
            if (noiThatList != null && noiThatList.isNotEmpty()) {
                updateNoiThatAdapter(noiThatList)
            }
        }

        // Observe tiện nghi
        tienNghiViewModel.getListTienNghi().observe(this) { tienNghiList ->
            if (tienNghiList != null && tienNghiList.isNotEmpty()) {
                updateTienNghiAdapter(tienNghiList)
            }
        }

        // Observe giới tính
        gioitinhViewModel.getListGioiTinh().observe(this) { gioiTinhList ->
            if (gioiTinhList != null && gioiTinhList.isNotEmpty()) {
                updateGioiTinhAdapter(gioiTinhList)
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            addRoomButton2.setOnClickListener {
                updateRoomInFirestore()
                loadingUtil.show()
            }

            addImage.setOnClickListener {
                TedImagePicker.with(this@SuaPhongTro)
                    .startMultiImage { uriList ->
                        displaySelectedImages(uriList)
                    }
            }

            fabAddDichVu.setOnClickListener {
                DichVuAddServiceUtil.showAddServiceDialog(this@SuaPhongTro) { newDichVu ->
                    dichVuAdapter.addDichVu(newDichVu)
                }
            }

            setupRoomNameFocusListener()
        }

    }

//        firestore = FirebaseFirestore.getInstance()
//
//
//        completionAnimation = findViewById(R.id.completionAnimation)
//
//
//        // Khởi tạo FirebaseAuth và lấy userId
//        mAuth = FirebaseAuth.getInstance()
//        val currentUser = mAuth.currentUser
//        userId = currentUser?.uid ?: ""
//        Log.d("UID", "UID: $userId")
//
//        roomId = intent.getStringExtra("roomId").toString()
//
//
//        //Khởi tạo LoadingUtil
//        loadingUtil = LoadingUtil(this)
//        loadingUtil.show()
//
//
//        // Khởi tạo ViewModel
//        roomDetailsViewModel = ViewModelProvider(this).get(RoomDetailsViewModel::class.java)
//
//        roomDetailsViewModel.fetchRoomDetails(roomId)
//
//        nhatroAdapter = SimpleHomeAdapter(this, emptyList(), this)
//
//
//        // Quan sát dữ liệu phòng
//        roomDetailsViewModel.roomDetails.observe(this) { roomDetails ->
//            roomDetails?.let { populateRoomDetails(it) }
//
//            nhatroViewModel.getAllNhaTroByUserId(userId)
//        }
//
//// Khởi tạo dichVuAdapter
//        dichVuAdapter = DichVuAdapter(this, emptyList(), this)
//        binding.listViewDichVu.adapter = dichVuAdapter
//
//        // Khởi tạo thongTinAdapter
//        thongTinAdapter = ThongTinAdapter(this, emptyList(), this)
//        binding.listViewThongTin.adapter = thongTinAdapter
//
//        gioitinhAdapter = GioiTinhAdapter(this, emptyList(),this)
//        binding.listViewGioiTinh.adapter = gioitinhAdapter
//
//       nhatroAdapter = SimpleHomeAdapter(this, emptyList(), this)
//        binding.listViewNhaTro.adapter = nhatroAdapter
//        roomDetailsViewModel.getListDichVu()
//
//        // Khởi tạo RecyclerView
//        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
//        binding.RcTienNghi.layoutManager = GridLayoutManager(this, 3)
//        binding.listViewDichVu.layoutManager = GridLayoutManager(this, 3)
//        binding.listViewThongTin.layoutManager = GridLayoutManager(this, 3)
//        binding.imagegeContainer.layoutManager = GridLayoutManager(this, 4)
//        binding.listViewGioiTinh.layoutManager = GridLayoutManager(this, 3)
//        binding.listViewNhaTro.layoutManager = GridLayoutManager(this, 3)
//
//        // Khởi tạo ViewModel
//        noiThatViewModel = ViewModelProvider(this).get(NoiThatViewModel::class.java)
//        tienNghiViewModel = ViewModelProvider(this).get(TienNghiViewModel::class.java)
//        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)
//        nhatroViewModel = ViewModelProvider(this).get(NhaTroViewModel::class.java)
//
//
//
//
//        // 2. Gọi API để lấy danh sách dịch vụ
//        roomDetailsViewModel.getListDichVu()
//
//        // 3. Observe danh sách đơn vị mặc định trước
//        roomDetailsViewModel.listDichVu.observe(this) { dichVuList ->
//            if (dichVuList != null && dichVuList.isNotEmpty()) {
//                // Lưu đơn vị của từng dịch vụ vào Map
//                dichVuList.forEach { dichVu ->
//                    dichVuDonViMap[dichVu.tenDichVu] = dichVu.donVi
//                }
//                Log.d("DichVu_Debug", "Đã lưu Map đơn vị: $dichVuDonViMap")
//            }
//        }
//
//        roomDetailsViewModel.services.observe(this) { services ->
//            if (services.isNotEmpty()) {
//                // Cập nhật adapter với danh sách phí dịch vụ
//                dichVuAdapter.updateList(services.map { service ->
//                    val donViList = dichVuDonViMap[service.tenDichVu] ?: listOf(service.donVi)
//                    DichVu(
//                        maDichVu = "",
//                        tenDichVu = service.tenDichVu,
//                        iconDichVu = service.iconDichVu,
//                        donVi = donViList,
//                        trangThai = true
//                    )
//                })
//
//                // Cập nhật giá tiền
//                dichVuAdapter.pricesMap.clear()
//                services.forEachIndexed { index, service ->
//                    dichVuAdapter.pricesMap[index] = service.soTien to service.donVi
//                }
//
//                // Cập nhật listPhiDichVu ngay sau khi load dữ liệu
//                onAllPricesEntered(dichVuAdapter.getCurrentPhiDichVu())
//
//                loadingUtil.hide()
//            }
//        }
//        roomDetailsViewModel.imfor.observe(this) { detailsList ->
//            if (detailsList.isNotEmpty()) {
//                // Cập nhật Adapter
//                thongTinAdapter.updateData(detailsList.map { chiTiet ->
//                    ThongTin(
//                        maThongTin = "",
//                        tenThongTin = chiTiet.tenThongTin.toString(),
//                        iconThongTin = chiTiet.iconThongTin.toString(),
//                        donVi = chiTiet.donVi.toString(),
//                        trangThai = true
//                    )
//                })
//
//                // Cập nhật giá trị
//                thongTinAdapter.pricesMap.clear()
//                detailsList.forEachIndexed { index, chiTiet ->
//                    thongTinAdapter.pricesMap[index] = chiTiet.soLuongDonVi.toLong()
//                }
//
//                // Cập nhật pricesMapThongTin ngay sau khi load dữ liệu
//                onThongTinimfor(thongTinAdapter.getCurrentThongTin())
//
//                thongTinAdapter.notifyDataSetChanged()
//            } else {
//                Toast.makeText(this, "Không có chi tiết thông tin nào", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Khởi tạo observers
//        roomDetailsViewModel.furniture.observe(this) { selectedFurniture ->
//            selectedFurniture?.let {
//                // Cập nhật adapter
//                noiThatAdapter.updateSelectedItems(it)
//                // Cập nhật selectedNoiThatList
//                selectedNoiThatList.clear()
//                selectedNoiThatList.addAll(noiThatAdapter.getSelectedNoiThat())
//            }
//        }
//
//        roomDetailsViewModel.amenities.observe(this) { selectedAmenities ->
//            selectedAmenities?.let {
//                // Cập nhật adapter
//                TienNghiAdapter.updateSelectedItems(it)
//                // Cập nhật selectedTienNghiList
//                selectedTienNghiList.clear()
//                selectedTienNghiList.addAll(TienNghiAdapter.getSelectedTienNghi())
//            }
//        }
//        // Gọi hàm khởi tạo dữ liệu
//        roomDetailsViewModel.initializeSelectedLists(roomId)
//
//        // Quan sát lỗi
//        roomDetailsViewModel.error.observe(this) { errorMessage ->
//            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
//        }
////currentUser?.uid ?: ""
//
//
//        // Observe the nhatro list
//        nhatroViewModel.listNhaTro.observe(this) { nhatroList ->
//            nhatroAdapter.updateList(nhatroList)
//        }
//        nhatroViewModel.selectedNhaTroDetails.observe(this) { nhaTro ->
//            nhaTro?.let {
//                binding.apply {
//                    maNhaTro = nhaTro.maNhaTro
//                    diaChi = nhaTro.diaChi
//                    diaChiChiTiet = nhaTro.diaChiChiTiet
//                    Dc_quanhuyen = nhaTro.dcQuanHuyen
//                    Dc_tinhtp = nhaTro.dcTinhTP
//                    maLoaiPhong = nhaTro.maLoaiNhaTro
//
//
//                }
//
//                Log.d("NhaTro", "NhaTro: $nhaTro")
//            } }
//
//
//        binding.btnBack.setOnClickListener {
//            finish()
//        }
//                // Logic khi nhấn nút "Lưu"
//        binding.addRoomButton2.setOnClickListener {
//            updateRoomInFirestore()
//            loadingUtil.show()
//        }
//
//
//        binding.roomName.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                // Hiển thị Snackbar khi trường nhập liệu được focus
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Hãy nhập tên phòng, ví dụ: Phòng 101", Snackbar.LENGTH_LONG
//                )
//                    .show()
//            }
//        }
////        // định dạng số tiền nhập vào
//        CurrencyFormatTextWatcher.addTo(binding.roomPrice)
//
////nút lưu ở đây
//        binding.addImage.setOnClickListener {
//            TedImagePicker.with(this)
//                .startMultiImage { uriList ->
//                    displaySelectedImages(uriList)
//                }
//        }
//        // Inside your fragment or activity
//        binding.fabAddDichVu.setOnClickListener {
//            DichVuAddServiceUtil.showAddServiceDialog(this@SuaPhongTro) { newDichVu ->
//                // Directly call the method in your adapter to add the service
//                dichVuAdapter.addDichVu(newDichVu)
//            }
//        }
//
//
//        // Quan sát LiveData từ ViewModel
//        noiThatViewModel.getListNoiThat().observe(this, Observer { noiThatList ->
//            if (noiThatList != null && noiThatList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                noiThatAdapter = NoiThatAdapter(this, noiThatList, this)
//                binding.recyclerView.adapter = noiThatAdapter
//
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu Nội Thất", Toast.LENGTH_SHORT).show()
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
//                Toast.makeText(this, "Không có dữ liệu Tiện Nghi", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//
//        nhatroViewModel.listNhaTro.observe(this) { nhaTroList ->
//
//            // Tạo adapter mới với danh sách mới
//            nhatroAdapter = SimpleHomeAdapter(this, nhaTroList, this)
//            binding.listViewNhaTro.adapter = nhatroAdapter
//
//
//
//        }
//
//
//        gioitinhViewModel.getListGioiTinh().observe(this, Observer { gioiTinhList ->
//            if (gioiTinhList != null && gioiTinhList.isNotEmpty()) {
//                // Cập nhật RecyclerView khi có dữ liệu
//                gioitinhAdapter = GioiTinhAdapter(this, gioiTinhList, this)
//                binding.listViewGioiTinh.adapter = gioitinhAdapter
//            } else {
//                // Hiển thị thông báo nếu không có dữ liệu
//                Toast.makeText(this, "Không có dữ liệu Giới Tính", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//    }

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
            nhatroViewModel.updateSelectedNhaTro(nhaTro)
        }
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

    private fun populateRoomDetails(roomDetails: PhongTroModel) {
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
        maLoaiPhong = roomDetails.maLoaiNhaTro
        Ma_gioiTinh = roomDetails.maGioiTinh
        TrangThaiPhong = roomDetails.trangThaiPhong
        trangThaiDuyet = roomDetails.trangThaiDuyet
        trangThaiLuu = roomDetails.trangThaiLuu
        soLuotXemPhong = roomDetails.soLuotXemPhong.toString()


        Log.d("DiaChi", "Giới tính : $Ma_gioiTinh")
        Log.d( "DiaChi", "Mã nhà trọ : $maNhaTro")
        gioitinhAdapter.selectById(Ma_gioiTinh)
        nhatroAdapter.selectById(maNhaTro)

        // Instead, trigger a refresh of the nhatro list if needed
        if (maNhaTro.isNotEmpty()) {
            nhatroViewModel.getAllNhaTroByUserId(userId)
        }
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
            maNhaTro.isEmpty() -> {
                Toast.makeText(this, "Vui lòng chọn nhà trọ", Toast.LENGTH_SHORT).show()
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
                    this@SuaPhongTro,
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
            this@SuaPhongTro,
            "Lỗi khi cập nhật dữ liệu: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
        Log.e("Firestore", "Lỗi khi cập nhật dữ liệu: ${e.message}")
    }


    /////
    private fun updateDichVuAdapter(services: List<PhiDichVu>) {
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

        // Cập nhật listPhiDichVu
        onAllPricesEntered(dichVuAdapter.getCurrentPhiDichVu())
        loadingUtil.hide()
    }

    private fun updateThongTinAdapter(detailsList: List<ChiTietThongTin>) {
        // Cập nhật Adapter với danh sách thông tin
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

        // Cập nhật pricesMapThongTin
        onThongTinimfor(thongTinAdapter.getCurrentThongTin())
        thongTinAdapter.notifyDataSetChanged()
    }

    private fun setupFurnitureAndAmenitiesObservers() {
        // Quan sát nội thất đã chọn
        roomDetailsViewModel.furniture.observe(this) { selectedFurniture ->
            selectedFurniture?.let {
                noiThatAdapter.updateSelectedItems(it)
                selectedNoiThatList.clear()
                selectedNoiThatList.addAll(noiThatAdapter.getSelectedNoiThat())
            }
        }

        // Quan sát tiện nghi đã chọn
        roomDetailsViewModel.amenities.observe(this) { selectedAmenities ->
            selectedAmenities?.let {
                TienNghiAdapter.updateSelectedItems(it)
                selectedTienNghiList.clear()
                selectedTienNghiList.addAll(TienNghiAdapter.getSelectedTienNghi())
            }
        }
    }

    private fun updateNhaTroDetails(nhaTro: NhaTroModel) {
        binding.apply {
            maNhaTro = nhaTro.maNhaTro
            diaChi = nhaTro.diaChi
            diaChiChiTiet = nhaTro.diaChiChiTiet
            Dc_quanhuyen = nhaTro.dcQuanHuyen
            Dc_tinhtp = nhaTro.dcTinhTP
            maLoaiPhong = nhaTro.maLoaiNhaTro
        }
        Log.d("NhaTro", "NhaTro: $nhaTro")
    }

    private fun updateNoiThatAdapter(noiThatList: List<NoiThat>) {
        noiThatAdapter = NoiThatAdapter(this, noiThatList, this)
        binding.recyclerView.adapter = noiThatAdapter
    }

    private fun updateTienNghiAdapter(tienNghiList: List<TienNghi>) {
        TienNghiAdapter = TienNghiAdapter(this, tienNghiList, this)
        binding.RcTienNghi.adapter = TienNghiAdapter
    }

    private fun updateGioiTinhAdapter(gioiTinhList: List<GioiTinh>) {
        gioitinhAdapter = GioiTinhAdapter(this, gioiTinhList, this)
        binding.listViewGioiTinh.adapter = gioitinhAdapter

        // Nếu đã có mã giới tính từ thông tin phòng
        if (Ma_gioiTinh.isNotEmpty()) {
            gioitinhAdapter.selectById(Ma_gioiTinh)
        }
    }
    private fun setupRoomNameFocusListener() {
        binding.roomName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Hiển thị Snackbar khi trường nhập liệu được focus
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Hãy nhập tên phòng, ví dụ: Phòng 101",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        // Thêm CurrencyFormatTextWatcher cho trường giá phòng
        CurrencyFormatTextWatcher.addTo(binding.roomPrice)
    }
}


//
//        // Logic khi nhấn nút "Đăng"
//        binding.addRoomButton2.setOnClickListener {
//            TrangThaiPhong = false
//            saveRoomToFirestore(isSaved = false, trangThaiPhong = TrangThaiPhong)
//            loadingUtil.show()
//        }