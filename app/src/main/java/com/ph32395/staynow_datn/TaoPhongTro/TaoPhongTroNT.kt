package com.ph32395.staynow_datn.TaoPhongTro


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
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.DichVu.DichVuAddServiceUtil
import com.ph32395.staynow_datn.DichVu.DichVuViewModel
import com.ph32395.staynow_datn.GioiTinh.GioiTinhViewModel
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhongViewModel
import com.ph32395.staynow_datn.NoiThat.GioiTinhAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.NoiThat.NoiThatAdapter
import com.ph32395.staynow_datn.NoiThat.NoiThatViewModel
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroAdapter
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.ThongTin.ThongTinAdapter
import com.ph32395.staynow_datn.ThongTin.ThongTinViewModel
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.TienNghi.TienNghiAdapter
import com.ph32395.staynow_datn.TienNghi.TienNghiViewModel
import com.ph32395.staynow_datn.databinding.ActivityTaoPhongTroBinding
import com.ph32395.staynow_datn.databinding.ActivityTaoPhongTroNtBinding
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TaoPhongTroNT : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityTaoPhongTroNtBinding
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var noiThatViewModel: NoiThatViewModel
    private lateinit var TienNghiAdapter: TienNghiAdapter
    private lateinit var tienNghiViewModel: TienNghiViewModel
    private lateinit var DichVuAdapter: DichVuAdapter
    private lateinit var dichVuViewModel: DichVuViewModel
    private lateinit var thongTinAdapter: ThongTinAdapter
    private lateinit var thongTinViewModel: ThongTinViewModel
    private lateinit var gioitinhViewModel: GioiTinhViewModel
    private lateinit var gioitinhAdapter: GioiTinhAdapter
    private lateinit var nhatroViewModel: NhaTroViewModel
    private lateinit var nhatroAdapter: SimpleHomeAdapter


    private var listPhiDichVu = mutableListOf<PhiDichVu>()



    // Khai bao bien luu tru du lieu thong tin

    private val pricesMapThongTin = mutableMapOf<String, Pair<ThongTin, Int>>()


    private lateinit var mAuth: FirebaseAuth
    private lateinit var userId: String


    // Khai báo list để lưu thông tin dịch vụ và tiện nghi đã chọn
    private val selectedTienNghiList = mutableListOf<TienNghi>()  // Lưu tiện nghi
    private val selectedNoiThatList = mutableListOf<NoiThat>()  // Lưu nội thất

    private lateinit var imageAdapter: ChoiceImageAdapter
    private var mutableUriList: MutableList<Uri> = mutableListOf()



    var Ma_gioiTinh = ""
    var TrangThaiPhong = false

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
        binding = ActivityTaoPhongTroNtBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
        binding.listViewGioiTinh.layoutManager = GridLayoutManager(this, 3)
        binding.listViewNhaTro.layoutManager = GridLayoutManager(this, 3)

        // Khởi tạo ViewModel
        noiThatViewModel = ViewModelProvider(this).get(NoiThatViewModel::class.java)
        tienNghiViewModel = ViewModelProvider(this).get(TienNghiViewModel::class.java)
        dichVuViewModel = ViewModelProvider(this).get(DichVuViewModel::class.java)
        thongTinViewModel = ViewModelProvider(this).get(ThongTinViewModel::class.java)
        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)
        nhatroViewModel = ViewModelProvider(this).get(NhaTroViewModel::class.java)

        nhatroAdapter = SimpleHomeAdapter(this, emptyList(), this)

        nhatroViewModel.getAllNhaTroByUserId(currentUser?.uid ?: "")

        // Observe the nhatro list
        nhatroViewModel.listNhaTro.observe(this) { nhatroList ->
            nhatroAdapter.updateList(nhatroList)
        }
        nhatroViewModel.selectedNhaTroDetails.observe(this) { nhaTro ->
            nhaTro?.let {
                binding.apply {
                    maNhaTro = nhaTro.maNhaTro
                    diaChi = nhaTro.diaChi
                    diaChiChiTiet = nhaTro.diaChiChiTiet
                    Dc_quanhuyen = nhaTro.dcQuanHuyen
                    Dc_tinhtp = nhaTro.dcTinhTP
                    maLoaiPhong = nhaTro.maLoaiNhaTro


            }

                Log.d("NhaTro", "NhaTro: $nhaTro")
        } }


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
            DichVuAddServiceUtil.showAddServiceDialog(this@TaoPhongTroNT) { newDichVu ->
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


        nhatroViewModel.listNhaTro.observe(this) { nhaTroList ->

                // Tạo adapter mới với danh sách mới
                nhatroAdapter = SimpleHomeAdapter(this, nhaTroList, this)
                binding.listViewNhaTro.adapter = nhatroAdapter



        }


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

    private fun saveRoomToFirestore(isSaved: Boolean, trangThaiPhong: Boolean) {
        val roomName = binding.roomName.text.toString()
        val roomPrice = CurrencyFormatTextWatcher.getUnformattedValue(binding.roomPrice).toInt()
        val description = binding.description.text.toString()
        val dcQuanhuyen = Dc_quanhuyen
        val dcTinhtp = Dc_tinhtp


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
        if (maNhaTro.isEmpty()) {
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
        // Kiểm tra trùng tên phòng
        firestore.collection("PhongTro")
            .whereEqualTo("maNhaTro", maNhaTro)
            .whereEqualTo("tenPhongTro", roomName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Tên phòng đã tồn tại
                    loadingUtil.hide()
                    binding.roomName.error = "Tên phòng đã tồn tại trong nhà trọ này"
                    Toast.makeText(this, "Tên phòng đã tồn tại trong nhà trọ này", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
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
                    Toast.makeText(this, "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi kiểm tra tên phòng: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    "diaChi" to diaChi,
                    "diaChiChiTiet" to diaChiChiTiet,
                    "trangThaiDC" to false,
                    "dcQuanHuyen" to dc_quanhuyen,
                    "dcTinhTP" to dc_tinhtp,
                    "maLoaiNhaTro" to maLoaiPhong,
                    "maNhaTro" to maNhaTro,
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
                nhatroViewModel.checkDuplicateRoom(maNhaTro, roomName)

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
                            this@TaoPhongTroNT,
                            "Tất cả dữ liệu đã được lưu thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Điều hướng sau khi lưu thành công
                        val intent = Intent(this@TaoPhongTroNT, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish()

                    }, 2000) // Thời gian delay 2 giây
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingUtil.hide()
                    Toast.makeText(
                        this@TaoPhongTroNT,
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
}




