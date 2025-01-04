package com.ph32395.staynow_datn.QuanLyPhongTro.UpdateRoom

import DichVuAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Adapter.PhiDichVuAdapter
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.DichVu.DichVu
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
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin
import com.ph32395.staynow_datn.TaoPhongTro.ChoiceImageAdapter
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
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
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateRoomActivity : AppCompatActivity(), AdapterTaoPhongTroEnteredListenner {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityUpdateRoomBinding
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var noiThatViewModel: NoiThatViewModel
    private lateinit var TienNghiAdapter: TienNghiAdapter
    private lateinit var tienNghiViewModel: TienNghiViewModel
    private lateinit var thongTinAdapter: ThongTinAdapter
    private lateinit var thongTinViewModel: ThongTinViewModel
    private lateinit var loaiPhongAdapter: LoaiPhongAdapter
    private lateinit var loaiPhongViewModel: LoaiPhongViewModel
    private lateinit var gioitinhViewModel: GioiTinhViewModel
    private lateinit var gioitinhAdapter: GioiTinhAdapter
    private lateinit var viewModel: RoomDetailViewModel

    private lateinit var phiDichVuAdapter: PhiDichVuAdapter


    private var listPhiDichVu = mutableListOf<PhiDichVu>()
    private var listTT= mutableListOf<ThongTin>()

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
    var Ma_loaiphong = ""
    var Ma_gioiTinh = ""

    //Phí thông tin
    var tendichvu = ""


    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var completionAnimation: LottieAnimationView
    private lateinit var loadingUtil: LoadingUtil

    //a2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding
        binding = ActivityUpdateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val maPhongTro = "68dNr0UV4fMRtq9Fhgrt"

//
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
        thongTinViewModel = ViewModelProvider(this).get(ThongTinViewModel::class.java)
        loaiPhongViewModel = ViewModelProvider(this).get(LoaiPhongViewModel::class.java)
        gioitinhViewModel = ViewModelProvider(this).get(GioiTinhViewModel::class.java)

        viewModel = ViewModelProvider(this).get(RoomDetailViewModel::class.java)

//

        // Khởi tạo RecyclerView và adapter như bình thường
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        noiThatAdapter = NoiThatAdapter(this, emptyList(), this)
        binding.recyclerView.adapter = noiThatAdapter
        // Load tất cả dữ liệu
        loadRoomData(maPhongTro)
        loadSelectedNoiThat(maPhongTro)

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

    // Thay thế phương thức onAllPricesEntered
    override fun onAllPricesEntered(prices: List<PhiDichVu>) {
        // Trong trường hợp này không cần xử lý thêm vì đã xử lý trong observer
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

    override fun onNhaTroSelected(nhaTro: NhaTroModel, isSelected: Boolean) {
        TODO("Not yet implemented")
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



    private fun loadRoomData(maPhongTro: String) {
        // 1. Load thông tin cơ bản của phòng
        firestore.collection("PhongTro").document(maPhongTro)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Điền thông tin cơ bản
                    binding.roomName.setText(document.getString("tenPhongTro"))
                    // Format giá phòng với CurrencyFormatTextWatcher
                    val giaPhong = document.getLong("giaPhong")?.toString() ?: "0"
                    binding.roomPrice.setText(giaPhong)
                    binding.description.setText(document.getString("moTaChiTiet"))

                    // Lưu các giá trị khác
                    Ma_loaiphong = document.getString("maLoaiNhaTro") ?: ""
                    Ma_gioiTinh = document.getString("maGioiTinh") ?: ""

                    // Load ảnh cũ
                    val imageUrls = document.get("imageUrls") as? List<String> ?: listOf()
                    loadExistingImages(imageUrls)
                }
            }
    }

    // Hàm load ảnh cũ
    private fun loadExistingImages(imageUrls: List<String>) {
        // Tạo danh sách Uri từ URLs
        val uris = imageUrls.map { url ->
            Uri.parse(url)
        }
        // Hiển thị ảnh trong RecyclerView
        displaySelectedImages(uris)
    }
    private fun loadSelectedNoiThat(maPhongTro: String) {
        // 1. Trước tiên load tất cả nội thất
        firestore.collection("NoiThat").get()
            .addOnSuccessListener { noiThatDocs ->
                val allNoiThat = noiThatDocs.mapNotNull { doc ->
                    doc.toObject(NoiThat::class.java)
                }

                // 2. Sau đó load danh sách nội thất đã chọn của phòng này
                firestore.collection("PhongTroNoiThat")
                    .whereEqualTo("maPhongTro", maPhongTro)
                    .get()
                    .addOnSuccessListener { selectedDocs ->
                        // Lấy danh sách mã nội thất đã chọn
                        val selectedIds = selectedDocs.mapNotNull { it.getString("maNoiThat") }

                        // Tạo adapter mới với danh sách đầy đủ và các items đã chọn
                        noiThatAdapter = NoiThatAdapter(
                            context = this,
                            noiThatList = allNoiThat,
                            listener = this,
                            selectedItems = selectedIds.toMutableSet()
                        )
                        binding.recyclerView.adapter = noiThatAdapter
                    }
            }
    }
}
