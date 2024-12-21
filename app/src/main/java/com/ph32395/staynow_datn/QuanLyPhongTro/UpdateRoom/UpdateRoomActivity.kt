package com.ph32395.staynow_datn.QuanLyPhongTro.UpdateRoom

import DichVuAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.DichVu.DichVuAddServiceUtil
import com.ph32395.staynow_datn.DichVu.DichVuViewModel
import com.ph32395.staynow_datn.GioiTinh.GioiTinhViewModel
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongViewModel
import com.ph32395.staynow_datn.Model.ChiTietThongTinModel
import com.ph32395.staynow_datn.NoiThat.GioiTinhAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.NoiThat.NoiThatAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThatViewModel
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.ChoiceImageAdapter
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.TaoPhongTro.PhiThongTin
import com.ph32395.staynow_datn.TaoPhongTro.PhongTroNoiThat
import com.ph32395.staynow_datn.TaoPhongTro.PhongTroTienNghi
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.ThongTin.ThongTinAdapter
import com.ph32395.staynow_datn.ThongTin.ThongTinViewModel
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.TienNghi.TienNghiAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghiViewModel
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel
import com.ph32395.staynow_datn.databinding.ActivityUpdateRoomBinding
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateRoomActivity : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {
    private lateinit var firestore: FirebaseFirestore

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding: ActivityUpdateRoomBinding
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
    private lateinit var viewModel: RoomDetailViewModel


    private var listPhiDichVu = mutableListOf<PhiDichVu>()
    private var listTT= mutableListOf<ThongTin>()
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

    var city = ""
    var district = ""
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


    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var completionAnimation: LottieAnimationView
    private lateinit var loadingUtil: LoadingUtil

    //a2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivityUpdateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // Lấy dữ liệu từ Intent
//        val maPhongTro = intent.getStringExtra("id_PhongTro")
//        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
//        //đây là các model lấy dữ liệu của tôi
//        if (maPhongTro != null) {
//            viewModel.fetchRoomDetail(maPhongTro)
//            observeViewModel()
//        }

//        thongTinAdapter = ThongTinAdapter(this, listTT, this)
        //lấy dữ liệu từ viewModel
        val updateRoomModel  = intent.getParcelableExtra<UpdateRoomModel>("updateRoomModel")
        Log.d("UpdateRoomModel", "UpdateRoomModel: $updateRoomModel")

        // Populate basic room details

        //a
        firestore = FirebaseFirestore.getInstance()


        loadingAnimation = findViewById(R.id.loadingAnimation)
        completionAnimation = findViewById(R.id.completionAnimation)


        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userId = currentUser?.uid ?: ""
        Log.d("UID", "UID: $userId")

        //Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)
        loadingUtil.show()
        //khởi tạo adapter
        gioitinhAdapter = GioiTinhAdapter(this, listOf(), this)



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

        updateRoomModel?.let { model ->

            Log.d("UpdateRoomModel", "UpdateRoomModel bảng chi tiéte thông tin: ${model.Chi_tietthongtin}")

            // Set room name
            binding.roomName.setText(model.Ten_phongtro)

            // Set room price
            binding.roomPrice.setText(model.Gia_phong.toString())

            // Set description
            binding.description.setText(model.Chi_tietthem)

            // Set address
            binding.roomAddress.setText(model.Dia_chi)

            // Populate images
            if (model.Url_image.isNotEmpty()) {
                val uriList = model.Url_image.map { Uri.parse(it) }
                displaySelectedImages(uriList)
            }

            val listThongtin = thongTinViewModel.getListThongTin().value ?: mutableListOf() // Xử lý null
            val existingChiTietList = model.Chi_tietthongtin.map { detail ->
                ChiTietThongTinModel(ten_thongtin = detail.ten_thongtin, so_luong_donvi = detail.so_luong_donvi)
            }


//            // Select room type
//            loaiPhongAdapter.selectByName(model.Loai_phong)
//
//            // Select gender type
//            gioitinhAdapter.selectByName(model.Gioi_tinh)
//
//            // Populate Additional Information (Chi_tietthongtin)
//            model.Chi_tietthongtin.forEach { detail ->
////                Log.d("UpdateRoomModel", "Detail: ten_thongtin = ${detail.ten_thongtin}, so_luong_donvi = ${detail.so_luong_donvi}")
////                thongTinAdapter.selectItemByName(detail.ten_thongtin, detail.so_luong_donvi)
//
//
//
//            }

//
//            // Populate Services (Dich_vu)
//            model.Dich_vu.forEach { service ->
//                DichVuAdapter.selectItemByName(service.ten_dichvu, service.so_tien)
//            }
//
//            // Populate Furniture (Noi_that)
//            model.Noi_that.forEach { furniture ->
//                noiThatAdapter.selectItemByName(furniture.Ten_noithat)
//            }
//
//            // Populate Amenities (Tien_nghi)
//            model.Tien_nghi.forEach { amenity ->
//                TienNghiAdapter.selectItemByName(amenity.Ten_tiennghi)
//            }
        }


        //lấy mã người dùng từ mAuth


        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->  // `uriList` là danh sách Uri của ảnh đã chọn
                    displaySelectedImages(uriList)

                }
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

//        // Logic khi nhấn nút "Lưu"
//        binding.addRoomButton.setOnClickListener {
//            TrangThaiPhong = false
//            // Gọi hàm lưu phòng với trạng thái là "Lưu"
//            saveRoomToFirestore(isSaved = true, trangThaiPhong = TrangThaiPhong)
//        }

        // Handling Image Picker (Existing functionality for image selection)
        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->
                    displaySelectedImages(uriList)
                }
        }
        // Inside your fragment or activity
        binding.fabAddDichVu.setOnClickListener {
            DichVuAddServiceUtil.showAddServiceDialog(this@UpdateRoomActivity) { newDichVu ->
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
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })

        tienNghiViewModel.getListTienNghi().observe(this, Observer { TienNghiList ->
            if (TienNghiList != null && TienNghiList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                TienNghiAdapter = TienNghiAdapter(this, TienNghiList, this)
                binding.RcTienNghi.adapter = TienNghiAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })

        thongTinViewModel.getListThongTin().observe(this, Observer { thongTinList ->
            if (thongTinList != null && thongTinList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                thongTinAdapter = ThongTinAdapter(this, thongTinList, this)
                listTT = thongTinList.toMutableList()
                thongTinAdapter.updateData(listTT)
                binding.listViewThongTin.adapter = thongTinAdapter
                loadingUtil.hide()
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })

        loaiPhongViewModel.getListLoaiPhong().observe(this, Observer { listLoaiPhong ->
            if (listLoaiPhong != null && listLoaiPhong.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                loaiPhongAdapter = LoaiPhongAdapter(this, listLoaiPhong, this)
                binding.recyclerViewLoaiPhong.adapter = loaiPhongAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
        gioitinhViewModel.getListGioiTinh().observe(this, Observer { gioiTinhList ->
            if (gioiTinhList != null && gioiTinhList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                gioitinhAdapter = GioiTinhAdapter(this, gioiTinhList, this)
                binding.listViewGioiTinh.adapter = gioitinhAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
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
            pricesMapThongTin[thongtin.Ma_thongtin.toString()] = Pair(thongtin, price)
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
        Ma_loaiphong = loaiPhong.Ma_loaiphong.toString()
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

    private fun observeViewModel() {
//        Quan sat chi tiet phong tro chinh
        viewModel.room.observe(this) { room ->
            binding.roomName.setText(room.Ten_phongtro)
            binding.roomPrice.setText(room.Gia_phong.toString())
            binding.description.setText(room.Mota_chitiet)
            binding.roomAddress.setText(room.Dia_chi)
            gioitinhAdapter.selectById(room.Ma_gioiTinh)
        }
    }

    private fun saveRoomDataToFirestore(
        roomName: String,
        roomPrice: Int,
        description: String,
        imageUrls: List<String>,
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
                    "Ten_phongtro" to roomName,
                    "Gia_phong" to roomPrice,
                    "Mota_chitiet" to description,
                    "Ma_nguoidung" to userId,
                    "Dia_chi" to fullAddressct,
                    "Dia_chichitiet" to fullAddressDeltail,
                    "Trang_thaidc" to false,
                    "Ma_loaiphong" to Ma_loaiphong,
                    "Ma_gioiTinh" to Ma_gioiTinh,
                    "Trang_thailuu" to Trang_thailuu,
                    "Trang_thaiduyet" to Trang_thaiduyet,
                    "ThoiGian_taophong" to ThoiGian_taophong,
                    "Ngay_capnhat" to Ngay_capnhat,
                    "So_luotxemphong" to So_luotxemphong,
                    "imageUrls" to imageUrls,
                    "Trang_thaiphong" to trangThaiPhong,
                    "Trang_thaidc" to true
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
                    loadingAnimation.visibility = View.GONE
                    completionAnimation.setAnimation("done.json")
                    completionAnimation.visibility = View.VISIBLE
                    completionAnimation.playAnimation()

                    // Sau khi animation hoàn thành trong 2 giây
                    completionAnimation.postDelayed({
                        completionAnimation.visibility = View.GONE

                        // Toast thông báo thành công
                        Toast.makeText(
                            this@UpdateRoomActivity,
                            "Tất cả dữ liệu đã được lưu thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Điều hướng sau khi lưu thành công
                        val intent =
                            Intent(this@UpdateRoomActivity, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish()

                    }, 2000) // Thời gian delay 2 giây
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingAnimation.visibility = View.GONE
                    Toast.makeText(
                        this@UpdateRoomActivity,
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
                Ma_phongtro = maPhongTro,
                Ma_tiennghi = tienNghi.Ma_tiennghi.toString()
            )

            firestore.collection("PhongTroTienNghi").add(phongTroTienNghi)
                .addOnSuccessListener {
                    Log.d("Firestore", "Lưu tiện nghi ${tienNghi.Ten_tiennghi} thành công!")
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
                Ma_phongtro = maPhongTro,
                Ma_noithat = noithat.Ma_noithat.toString()
            )

            firestore.collection("PhongTroNoiThat").add(phongTroNoiThat)
                .addOnSuccessListener {
                    Log.d("Firestore", "Lưu tiện nghi ${noithat.Ten_noithat} thành công!")
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
            val newPhiDichVu = phiDichVu.copy(Ma_phongtro = maPhongTro)

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

            val phiThongTin = PhiThongTin(
                So_luong_donvi = price,
                Ma_phongtro = maPhongTro,
                Ten_thongtin = thongtin.Ten_thongtin,
                Icon_thongtin = thongtin.Icon_thongtin,
                Don_vi = thongtin.Don_vi
            )

            firestore.collection("ChiTietThongTin")
                .add(phiThongTin)
                .addOnSuccessListener {

                }
                .addOnFailureListener { exception ->
                }
        }
        Log.d("PhiThongTin", "Tổng số tasks: $totalTasks")
    }

    //đẻ code nãy cất ở dưới này

}
