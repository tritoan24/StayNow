package com.ph32395.staynow.TaoPhongTro

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow.CapNhatViTriPhong.CapNhatViTri
import com.ph32395.staynow.DiaChiGHN.GHNViewModel
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.Ward
import com.ph32395.staynow.DichVu.DichVu
import com.ph32395.staynow.DichVu.DichVuAdapter
import com.ph32395.staynow.DichVu.DichVuViewModel
import com.ph32395.staynow.GioiTinh.GioiTinhViewModel
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.LoaiPhong.LoaiPhongAdapter
import com.ph32395.staynow.LoaiPhong.LoaiPhongViewModel
import com.ph32395.staynow.NoiThat.GioiTinhAdapter
import com.ph32395.staynow.NoiThat.NoiThat
import com.ph32395.staynow.NoiThat.NoiThatAdapter
import com.ph32395.staynow.NoiThat.NoiThatViewModel
import com.ph32395.staynow.R
import com.ph32395.staynow.ThongTin.ThongTin
import com.ph32395.staynow.ThongTin.ThongTinAdapter
import com.ph32395.staynow.ThongTin.ThongTinViewModel
import com.ph32395.staynow.TienNghi.TienNghi
import com.ph32395.staynow.TienNghi.TienNghiAdapter
import com.ph32395.staynow.TienNghi.TienNghiViewModel
import com.ph32395.staynow.databinding.ActivityTaoPhongTroBinding
import com.ph32395.staynow.fragment.home.HomeFragment
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import selectLocationDialog


class TaoPhongTro : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {

    private lateinit var firestore: FirebaseFirestore

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

    // Khai báo pricesMap như một mutableMap để lưu thông tin mã dịch vụ và giá
    private val pricesMap = mutableMapOf<String, Pair<DichVu, Int>>()
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
    var addressDetail = ""
    var fullAddress = ""
    var fullAddressDeltail = ""
    var Ma_loaiphong = ""
    var Ma_gioiTinh = ""


    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var completionAnimation: LottieAnimationView


    private lateinit var ghnViewModel: GHNViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivityTaoPhongTroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()


        loadingAnimation = findViewById(R.id.loadingAnimation)
        completionAnimation = findViewById(R.id.completionAnimation)


        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userId = currentUser?.uid ?: ""
        Log.d("UID", "UID: $userId")


        // Khởi tạo RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.RcTienNghi.layoutManager = GridLayoutManager(this, 2)
        binding.listViewDichVu.layoutManager = GridLayoutManager(this, 4)
        binding.listViewThongTin.layoutManager = GridLayoutManager(this, 4)
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

        // Logic khi nhấn nút "Lưu"
        binding.addRoomButton.setOnClickListener {
            saveRoomToFirestore(isSaved = true)
        }

        // Logic khi nhấn nút "Đăng"
        binding.addRoomButton2.setOnClickListener {
            saveRoomToFirestore(isSaved = false)
        }


        // Khởi tạo ViewModel
        // Khởi tạo ViewModel
        ghnViewModel = ViewModelProvider(this).get(GHNViewModel::class.java)

        binding.roomAddress.setOnClickListener {
            // Call the selectLocationDialog to let the user select a location (Province, District, Ward)
            selectLocationDialog(
                context = this,
                title = "Chọn Tỉnh",
                itemType = "Province",
                ghnViewModel = ghnViewModel,
                onItemSelected = { selectedItem ->
                    when (selectedItem) {
                        is Ward -> {
                            ward = selectedItem.WardName
                        }

                        is District -> {
                            district = selectedItem.DistrictName
                        }

                        is Province -> {
                            city = selectedItem.ProvinceName
                        }

                        is String -> {
                            addressDetail = selectedItem
                        }
                    }

                    // Cập nhật chuỗi fullAddress và fullAddressDetail theo yêu cầu
                    fullAddress = "$ward, $district, $city"
                    fullAddressDeltail = "$addressDetail, $ward, $district, $city"

                    // Hiển thị địa chỉ đầy đủ
                    binding.roomAddress.text = fullAddressDeltail
                }
            )
        }

        // Handling Image Picker (Existing functionality for image selection)
        binding.addImage.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList ->
                    displaySelectedImages(uriList)
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
                // Cập nhật RecyclerView khi có dữ liệu
                DichVuAdapter = DichVuAdapter(this, dichVuList, this)
                binding.listViewDichVu.adapter = DichVuAdapter
            } else {
                // Hiển thị thông báo nếu không có dữ liệu
                Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })

        thongTinViewModel.getListThongTin().observe(this, Observer { thongTinList ->
            if (thongTinList != null && thongTinList.isNotEmpty()) {
                // Cập nhật RecyclerView khi có dữ liệu
                thongTinAdapter = ThongTinAdapter(this, thongTinList, this)
                binding.listViewThongTin.adapter = thongTinAdapter
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

    override fun onAllPricesEntered(prices: List<Pair<DichVu, Int>>) {
        prices.forEach { (dichVu, price) ->
            // Lưu thông tin dịch vụ và giá vào pricesMap
            pricesMap[dichVu.Ma_dichvu.toString()] = Pair(dichVu, price)
        }
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
        loaiPhong: com.ph32395.staynow.LoaiPhong.LoaiPhong,
        isSelected: Boolean
    ) {
        Ma_loaiphong = loaiPhong.Ma_loaiphong.toString()
    }

    override fun onGioiTinhSelected(
        gioiTinh: com.ph32395.staynow.GioiTinh.GioiTinh,
        isSelected: Boolean
    ) {
        Ma_gioiTinh = gioiTinh.Ma_gioitinh.toString()
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

    private fun saveRoomToFirestore(isSaved: Boolean) {
        val roomName = binding.roomName.text.toString()
        val roomPrice = binding.roomPrice.text.toString().toIntOrNull() ?: 0
        val description = binding.description.text.toString()

        //validate
        if (roomName.isEmpty()) {
            binding.roomName.error = "Tên phòng trọ không được để trống"
            return
        }
        if (roomPrice == 0) {
            binding.roomPrice.error = "Giá phòng không được để trống"
            return
        }
        if (description.isEmpty()) {
            binding.description.error = "Mô tả không được để trống"
            return
        }
        if (fullAddress.isEmpty()) {
            binding.roomAddress.error = "Địa chỉ không được để trống"
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
        if (pricesMap.size < 4) {
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


        // Hiển thị hoạt ảnh loading khi bắt đầu tải dữ liệu
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.setAnimation("loading.json")
        loadingAnimation.playAnimation()


        val ThoiGian_taophong = System.currentTimeMillis()
        val Ngay_capnhat = System.currentTimeMillis()
        val So_luotxemphong = 0


        val Trang_thailuu = if (isSaved) true else false
        val Trang_thaiduyet = if (isSaved) "Da duyet" else "Cho duyet"


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
                                Trang_thailuu,
                                Trang_thaiduyet,
                                ThoiGian_taophong,
                                Ngay_capnhat,
                                So_luotxemphong,
                                isSaved
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
            Trang_thailuu: Boolean,
            Trang_thaiduyet: String,
            ThoiGian_taophong: Long,
            Ngay_capnhat: Long,
            So_luotxemphong: Int,
            isSaved: Boolean
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    // Chuẩn bị dữ liệu để lưu vào Firestore
                    val roomData = hashMapOf(
                        "Ten_phongtro" to roomName,
                        "Gia_phong" to roomPrice,
                        "Mota_chitiet" to description,
                        "Ma_nguoidung" to userId,
                        "Dia_chi" to fullAddress,
                        "Dia_chichitiet" to fullAddressDeltail,
                        "Trang_thaidc" to false,
                        "Ma_loaiphong" to Ma_loaiphong,
                        "Ma_gioiTinh" to Ma_gioiTinh,
                        "Trang_thailuu" to Trang_thailuu,
                        "Trang_thaiduyet" to Trang_thaiduyet,
                        "ThoiGian_taophong" to ThoiGian_taophong,
                        "Ngay_capnhat" to Ngay_capnhat,
                        "So_luotxemphong" to So_luotxemphong,
                        "imageUrls" to imageUrls
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
                    completionAnimation.postDelayed({
                        completionAnimation.visibility = View.GONE
                    }, 2000)



                    Toast.makeText(
                        this@TaoPhongTro,
                        "Tất cả dữ liệu đã được lưu thành công!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Điều hướng sau khi lưu thành công
                    if (isSaved) {
                        // Chuyển sang màn hình Home khi nhấn nút "Lưu phòng"
                        val intent = Intent(this@TaoPhongTro, HomeFragment::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Chuyển sang màn hình chọn địa chỉ khi nhấn nút "Đăng"
                        val intent = Intent(this@TaoPhongTro, CapNhatViTri::class.java)
                        intent.putExtra("PHONG_TRO_ID", roomTask.id) // Truyền ID phòng trọ
                        startActivity(intent)
                    }


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingAnimation.visibility = View.GONE
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
        val totalTasks = pricesMap.size

        pricesMap.forEach { (maDichVu, pair) ->
            val dichVu = pair.first
            val price = pair.second

            val phiDichVu = PhiDichVu(
                So_tien = price,
                Ma_phongtro = maPhongTro,
                Ten_dichvu = dichVu.Ten_dichvu,
                Icon_dichvu = dichVu.Icon_dichvu,
                Don_vi = dichVu.Don_vi
            )

            firestore.collection("PhiDichVu")
                .add(phiDichVu)
                .addOnSuccessListener {

                }
                .addOnFailureListener { exception ->
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


}




