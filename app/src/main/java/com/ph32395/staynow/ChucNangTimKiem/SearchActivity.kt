package com.ph32395.staynow.ChucNangTimKiem

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ph32395.staynow.Maps.MapsActivity
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.databinding.ActivitySearchBinding
import com.ph32395.staynow.databinding.BottomSheetCitySearchBinding
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PhongTroAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchActivity : AppCompatActivity(), BottomSheetFragment.PriceRangeListener,
    BottomSheetFilter.FilterCriteriaListener {

    private lateinit var binding: ActivitySearchBinding
    private var TAG: String = "zzzzzzzzzz"
    private val firestore = FirebaseFirestore.getInstance()
    private val searchHistoryRef = firestore.collection("LichSuTimKiem")
    private val dataRoom = firestore.collection("PhongTro")
    private val homeViewModel: HomeViewModel = HomeViewModel()
    var listFullRoom = mutableListOf<Pair<String, PhongTroModel>>()
    val listKeySearch: MutableList<SearchDataModel> = mutableListOf()
    var selectedTypesViewModel: MutableList<String> = mutableListOf()
    var selectedTienNghiViewModel: MutableList<String> = mutableListOf()
    var selectedNoiThatViewModel: MutableList<String> = mutableListOf()
    private lateinit var adapter: PhongTroAdapter
    var min: Int = 0
    var max: Int = 0
    var isAscending = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        readKeyWordSearch(userId)

        updateUI(listFullRoom, homeViewModel)
        readListRoom()

        binding.edtSearch.setOnFocusChangeListener { v, hasFocus ->
            Log.d(TAG, "onCreate: hasFocus-- $hasFocus")

            if (hasFocus) {
                binding.ivClearText.visibility = View.VISIBLE
                binding.ivClearText.setOnClickListener {
                    binding.edtSearch.text.clear()
                }
            } else {
                binding.ivClearText.visibility = View.INVISIBLE
            }

        }

        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.d(TAG, "onCreate: Content Text ${binding.edtSearch.text}")
                hideKeyClearFocus()
                true
            } else {
                false
            }
        }

        binding.btnSearch.setOnClickListener {
            Log.d(TAG, "onCreate: btnSearch ${binding.edtSearch.text}")
            saveKeyWordSearch(binding.edtSearch.text)
            val query: String = binding.edtSearch.text.toString()
            if (query.isNotEmpty()) {
                searchRoomByNameOrDescription(query, adapter)
            } else {
                // Tải lại toàn bộ danh sách nếu không có từ khóa
                readListRoom()
            }
            hideKeyClearFocus()
        }

        binding.btnKhoangGia.setOnClickListener {
            val priceRangeBottomSheet = BottomSheetFragment()
            priceRangeBottomSheet.show(supportFragmentManager, priceRangeBottomSheet.tag)
            priceRangeBottomSheet.updatePriceRange(min, max)
        }
        Log.d(TAG, "onCreate: full room $listFullRoom")
        binding.btnCity.setOnClickListener {
            if (isAscending) {
                Log.d(TAG, "onCreate: tang")
                listFullRoom = listFullRoom.sortedBy { it.second.Gia_phong }.toMutableList()
                Log.d(TAG, "onCreate:  listSortBy $listFullRoom.")
                updateUI(listFullRoom, homeViewModel)
            } else {
                Log.d(TAG, "onCreate: giam")
                listFullRoom =
                    listFullRoom.sortedByDescending { it.second.Gia_phong }.toMutableList()
                updateUI(listFullRoom, homeViewModel)

                Log.d(TAG, "onCreate:  listSortBy $listFullRoom")
            }
            isAscending = !isAscending
        }
        binding.ivFilter.setOnClickListener {
            val bottomSheetFragment = BottomSheetFilter()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            Log.d(TAG, "onCreate: selectedTypesViewModel$selectedTypesViewModel")
            Log.d(TAG, "onCreate: selectedTienNghiViewModel$selectedTienNghiViewModel")
            Log.d(TAG, "onCreate: selectedNoiThatViewModel$selectedNoiThatViewModel")
            bottomSheetFragment.updateFilter(
                selectedTypesViewModel,
                selectedNoiThatViewModel,
                selectedTienNghiViewModel
            )


        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
        binding.lvHistory.setOnItemClickListener { parent, view, position, id ->
            Log.d(TAG, "onCreate: LvHistory parent ${parent.adapter.getItem(position).toString()}")
            Log.d(TAG, "onCreate: LvHistory view $view")
            Log.d(TAG, "onCreate: LvHistory position $position")
            Log.d(TAG, "onCreate: LvHistory id $id")
            val item = parent.adapter.getItem(position)
            if (item is SearchDataModel) {
                Log.d(TAG, "Item text: ${item.tu_khoa}")
                binding.edtSearch.setText(item.tu_khoa.toString())
            } else {
                Log.d(TAG, "Item at position $position is not a String")
            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchRoomByNameOrDescription(query: String, adapter: PhongTroAdapter) {
        val queryWords = query.split(" ").filter { it.isNotEmpty() }
        val listSearch = mutableListOf<Pair<String, PhongTroModel>>()
        Log.d(TAG, "onDataChange: queryWords $queryWords")
        Log.d(TAG, "onDataChange: query $query")
        binding.layoutLoading.visibility = View.VISIBLE
        binding.rvListRoom.visibility = View.GONE
        // Truy vấn tất cả các phòng trọ một lần
        listFullRoom.clear()

        dataRoom.get().addOnSuccessListener { snapshot ->
            val tasks = mutableListOf<Task<QuerySnapshot>>()

            for (document in snapshot.documents) {
                val id = document.id.toString()
                val roomData = document.toObject(PhongTroModel::class.java)
                Log.d(TAG, "searchRoomByNameOrDescription: room data $roomData")

                // Truy vấn chi tiết thông tin diện tích
                val task = firestore.collection("ChiTietThongTin")
                    .whereEqualTo("ma_phongtro", id) // Truy vấn theo mã phòng trọ
                    .whereEqualTo("ten_thongtin", "Diện tích") // Lọc theo thông tin "Diện tích"
                    .get()
                    .addOnSuccessListener { chiTietSnapshot ->
                        val chiTiet = chiTietSnapshot.documents.firstOrNull()
                        val dienTich = chiTiet?.getDouble("so_luong_donvi") // Lấy giá trị diện tích
                        Log.d(TAG, "searchRoomByNameOrDescription: chi tiet $chiTiet")
                        Log.d(TAG, "searchRoomByNameOrDescription: dien tich $dienTich")
                        roomData?.Dien_tich = dienTich?.toLong()

                        val roomName = roomData?.Ten_phongtro ?: ""
                        val roomDescription = roomData?.Mota_chitiet ?: ""

                        // Kiểm tra nếu toàn bộ chuỗi `query` xuất hiện trong tên hoặc mô tả
                        val queryInDescriptionOrName =
                            roomName.contains(query, ignoreCase = true) ||
                                    roomDescription.contains(query, ignoreCase = true)

                        // Kiểm tra nếu tất cả các từ trong `queryWords` xuất hiện trong tên hoặc mô tả
                        val allWordsMatch = queryWords.all { word ->
                            roomName.contains(word, ignoreCase = true) || roomDescription.contains(
                                word,
                                ignoreCase = true
                            )
                        }

                        // Thêm phòng trọ vào danh sách nếu một trong hai điều kiện đúng
                        if (queryInDescriptionOrName || allWordsMatch) {
                            val trangThaiDuyet = document.getString("Trang_thaiduyet")
                            val trangThaiLuu = document.getBoolean("Trang_thailuu")
                            val trangThaiPhong = document.getBoolean("Trang_thaiphong")
                            Log.d(
                                TAG,
                                "TrangThaiDuyet: $trangThaiDuyet, TrangThaiLuu: $trangThaiLuu, TrangThaiPhong: $trangThaiPhong"
                            )
                            if (trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                                listSearch.add(Pair(id, roomData!!))
                                Log.d(
                                    TAG,
                                    "onDataChange: Room $roomData (tìm kiếm chi tiết hoặc tương đối)"
                                )
                            }

                        }
                        Log.d(TAG, "searchRoomByNameOrDescription: listSearch $listSearch")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error fetching room details: ${exception.message}")
                    }

                tasks.add(task)
            }

            // Đảm bảo tất cả các truy vấn đã hoàn thành
            Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnCompleteListener {
                // Sau khi tất cả các truy vấn hoàn thành, cập nhật giao diện
                Log.d(TAG, "searchRoomByNameOrDescription: listSearch Tasks $listSearch")
                if (listSearch.isNotEmpty()) {
                    listFullRoom.addAll(listSearch)
                    binding.layoutNullMsg.visibility = View.GONE
                    binding.rvListRoom.visibility = View.VISIBLE
                } else {
                    binding.layoutNullMsg.visibility = View.VISIBLE
                    binding.rvListRoom.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting documents: ", exception)
        }.addOnCompleteListener {
            binding.layoutLoading.visibility = View.GONE
            binding.rvListRoom.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun readListRoom() {
        //firestore
        binding.layoutLoading.visibility = View.VISIBLE
        binding.rvListRoom.visibility = View.GONE
        dataRoom.get().addOnSuccessListener { it ->
            listFullRoom.clear()
            Log.d(TAG, "readListRoom: it read room ${it.toObjects(PhongTroModel::class.java)}")
            for (document in it) {
                val id = document.id.toString()
                Log.d(TAG, "readListRoom: document.id.toString() $id")
                val roomList = document.toObject(PhongTroModel::class.java)
                firestore.collection("ChiTietThongTin")
                    .whereEqualTo("ma_phongtro", id) // Truy vấn theo mã phòng trọ
                    .whereEqualTo("ten_thongtin", "Diện tích") // Lọc theo thông tin "Diện tích"
                    .get()
                    .addOnSuccessListener { chiTietSnapshot ->
                        val chiTiet = chiTietSnapshot.documents.firstOrNull()
                        val dienTich = chiTiet?.getDouble("so_luong_donvi") // Lấy giá trị diện tích

                        // Cập nhật diện tích vào đối tượng phòng
                        roomList.Dien_tich = dienTich?.toLong()

                        val trangThaiDuyet = document.getString("Trang_thaiduyet")
                        val trangThaiLuu = document.getBoolean("Trang_thailuu")
                        val trangThaiPhong = document.getBoolean("Trang_thaiphong")
                        Log.d(
                            TAG,
                            "readListRoom: trangThaiDuyet $trangThaiDuyet trangThaiLuu $trangThaiLuu trangThaiPhong $trangThaiPhong"
                        )
                        if (trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                            Log.d(TAG, "readListRoom: if ${roomList.Ten_phongtro}")
                            // Thêm vào danh sách hiển thị
                            listFullRoom.add(Pair(id, roomList))
                            Log.d(TAG, "Room added: $roomList")

                        }
                        // Cập nhật giao diện sau khi hoàn tất
                        if (listFullRoom.size > 0) {
                            adapter.notifyDataSetChanged()
                        }
                        Log.d(TAG, "readListRoom:listFullRoom $listFullRoom")
                        Log.d(TAG, "readListRoom:listFullRoom ${listFullRoom.size}")

                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error fetching room details: ${exception.message}")
                    }
            }
        }.addOnFailureListener {
            Log.e(TAG, "readListRoom: ${it.message.toString()}")
        }.addOnCompleteListener {
            binding.layoutLoading.visibility = View.GONE
            binding.rvListRoom.visibility = View.VISIBLE
            Log.d(TAG, "readListRoom:  complete")
        }


    }

    private fun readKeyWordSearch(userId: String?) {
        var adapter: AdapterHistoryKeyWord = AdapterHistoryKeyWord(this, mutableListOf(), userId!!)

        searchHistoryRef.document(userId).collection("HistoryKeyWord").orderBy("timestamps")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.d(TAG, "Error listening to snapshots: $error")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    for (change in snapshots.documentChanges) {
                        when (change.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "onChildAdded: snapshot: ${change.document}")
                                Log.d(TAG, "onChildAdded: snapshot_value: ${change.document.data}")

                                val newData = change.document.toObject(SearchDataModel::class.java)
                                Log.d(TAG, "onChildAdded: newData $newData")

                                newData.let {
                                    listKeySearch.add(it)
                                    Log.d(TAG, "onChildAdded: it let ${it.tu_khoa}")
                                }

                                if (listKeySearch.size >= 3) {
                                    Log.d(
                                        TAG,
                                        "onChildAdded: listKeySearch.size >= 3 Three size list search"
                                    )
                                    binding.lvHistory.layoutParams.height = 300
                                }

                                // Đảo ngược danh sách để hiển thị từ mới nhất đến cũ nhất
                                listKeySearch.reverse()
                                adapter = AdapterHistoryKeyWord(
                                    this@SearchActivity,
                                    listKeySearch,
                                    userId
                                )
                                binding.lvHistory.adapter = adapter
                                adapter.notifyDataSetChanged()
                                Log.d(TAG, "onChildAdded: List Search $listKeySearch")
                            }

                            DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "onChildChanged: snapshot: ${change.document}")
                                Log.d(TAG, "onChildChanged: listKeySearch $listKeySearch")
                                // Cập nhật danh sách nếu cần
                            }

                            DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "onChildRemoved: snapshot: ${change.document}")
                                listKeySearch.removeIf {
                                    it.timestamps == change.document.getString(
                                        "timestamps"
                                    )
                                }
                                if (listKeySearch.size < 3) {
                                    binding.lvHistory.layoutParams.height =
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

    }

    private fun saveKeyWordSearch(text: Editable) {
        Log.d(TAG, "saveKeyWordSearch: text $text")

// Lấy reference đến Firestore
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "saveKeyWordSearch: userID $userId")
        Log.d(TAG, "saveKeyWordSearch: firestore $firestore")

        val timeStamp = System.currentTimeMillis() // Lấy thời gian hiện tại
        val formattedTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
            Date(timeStamp)
        )
        val searchQuery = text.toString() // Từ khóa tìm kiếm
// Tạo một ID tìm kiếm ngẫu nhiên
        val searchId =
            searchHistoryRef.document(userId!!).collection("HistoryKeyWord").document().id
        val searchData = SearchDataModel(
            ma_timkiem = searchId,
            tu_khoa = searchQuery,
            thoi_giantimkiem = formattedTime,
            timestamps = timeStamp.toString()
        )
        Log.d(TAG, "saveKeyWordSearch: timeStamp $timeStamp")
        Log.d(TAG, "saveKeyWordSearch: formattedTime $formattedTime")
        Log.d(TAG, "saveKeyWordSearch: searchId $searchId")
        Log.d(TAG, "saveKeyWordSearch: searchData $searchData")

// Lưu vào Firestore
        searchHistoryRef.document(userId).collection("HistoryKeyWord").document(searchId)
            .set(searchData)
            .addOnSuccessListener {
                Log.d("SearchHistory", "Tìm kiếm đã được lưu vào Firestore.")
            }
            .addOnFailureListener { exception ->
                Log.e("SearchHistory", "Lỗi khi lưu tìm kiếm: ${exception.message}")
            }


    }

    private fun showBottomSheetCity() {
        val bottomSheetCity = BottomSheetDialog(this)

        val bindingBottomSheet = BottomSheetCitySearchBinding.inflate(layoutInflater)
        bottomSheetCity.setContentView(bindingBottomSheet.root)

        val listCity = listOf(
            "Hà Nội",
            "Hồ Chí Minh",
            "Hải Phòng",
            "Đà Nẵng",
            "Cần Thơ",
            "Biên Hòa",
            "Hải Dương"
        )
        Log.d(TAG, "showBottomSheetCity: List city $listCity")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listCity)
        bindingBottomSheet.lvCity.adapter = adapter
        bindingBottomSheet.lvCity.setOnItemClickListener { parent, view, position, id ->
            Log.d(TAG, "showBottomSheetCity: parent: $parent")
            Log.d(TAG, "showBottomSheetCity: view: $view")
            Log.d(TAG, "showBottomSheetCity: position: ${listCity[position]}")
            Log.d(TAG, "showBottomSheetCity: id: $id")
        }

        bottomSheetCity.show()

    }

    private fun hideKeyClearFocus() { // dung de clear focus va an ban phim khi nhan search
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        binding.edtSearch.clearFocus() // clear focus khi nhan search
    }

    private fun updateUI(
        listRoom: MutableList<Pair<String, PhongTroModel>>,
        homeViewModel: HomeViewModel
    ) {
        adapter = PhongTroAdapter(listRoom, homeViewModel)
        binding.rvListRoom.layoutManager = GridLayoutManager(this@SearchActivity, 2)
        binding.rvListRoom.adapter = adapter
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onPriceRangeSelected(minPrice: Int, maxPrice: Int) {
        Log.d(TAG, "onPriceRangeSelected: Min: $minPrice max: $maxPrice search")
        Log.d(TAG, "onPriceRangeSelected: list full room $listFullRoom")
//        filterRoomsByPrice(minPrice.toFloat(), maxPrice.toFloat())
//        listFullRoom = listFullRoom.filter {
//            it.second.Gia_phong in minPrice.toDouble()..maxPrice.toDouble()
//        }.toMutableList()
//        Log.d(TAG, "onPriceRangeSelected: $listFullRoom")
//        if (listFullRoom.isEmpty()) {
//            binding.rvListRoom.visibility = View.GONE
//            binding.layoutNullMsg.visibility = View.VISIBLE
//            readListRoom()
//        } else {
//            binding.rvListRoom.visibility = View.VISIBLE
//            binding.layoutNullMsg.visibility = View.GONE
//            updateUI(listFullRoom, homeViewModel)
//            Log.d(TAG, "onPriceRangeSelected: list full $listFullRoom")
//            adapter.notifyDataSetChanged()
//        }

        val listFilterPrice = mutableListOf<Pair<String, PhongTroModel>>()

//        dataRoom.whereGreaterThanOrEqualTo("Gia_phong", minPrice.toDouble())
//            .whereLessThanOrEqualTo("Gia_phong", maxPrice.toDouble())
//            .get()
//            .addOnSuccessListener {
//
//                for (document in it) {
//                    val id = document.id
//                    val roomData = document.toObject(PhongTroModel::class.java)
//
//                    firestore.collection("ChiTietThongTin")
//                        .whereEqualTo("ma_phongtro", id)
//                        .whereEqualTo("ten_thongtin", "Diện tích").get().addOnSuccessListener {
//                            val chiTiet = it.documents.firstOrNull()
//                            val dienTich = chiTiet?.getDouble("so_luong_donvi")
//
//                            roomData.Dien_tich = dienTich?.toLong()
//
//                            Log.d(TAG, "onPriceRangeSelected: cos dien tich $roomData")
//                            listFilterPrice.add(Pair(id, roomData))
//                            if (listFullRoom.isEmpty()) {
//                                binding.rvListRoom.visibility = View.GONE
//                                binding.layoutNullMsg.visibility = View.VISIBLE
//                            } else {
//                                binding.rvListRoom.visibility = View.VISIBLE
//                                binding.layoutNullMsg.visibility = View.GONE
//                                listFullRoom = listFilterPrice
//                                updateUI(listFullRoom, homeViewModel)
//                                Log.d(TAG, "onPriceRangeSelected: list full $listFullRoom")
//                                adapter.notifyDataSetChanged()
//                            }
//
//
//                        }.addOnFailureListener {
//                            Log.d(TAG, "onPriceRangeSelected: it.msg ${it.message.toString()}")
//                        }
//
//                }
//
//            }
//            .addOnFailureListener {
//                Log.d(TAG, "onPriceRangeSelected: it.msg ${it.message.toString()}")
//            }
        binding.layoutLoading.visibility = View.VISIBLE
        binding.rvListRoom.visibility = View.GONE
        binding.layoutNullMsg.visibility = View.GONE
        dataRoom.whereGreaterThanOrEqualTo("Gia_phong", minPrice.toDouble())
            .whereLessThanOrEqualTo("Gia_phong", maxPrice.toDouble())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tasks =
                    mutableListOf<Task<QuerySnapshot>>() // Danh sách các tác vụ truy vấn phụ

                for (document in querySnapshot) {
                    val id = document.id
                    val roomData = document.toObject(PhongTroModel::class.java)

                    // Thêm truy vấn phụ vào danh sách
                    val task = firestore.collection("ChiTietThongTin")
                        .whereEqualTo("ma_phongtro", id)
                        .whereEqualTo("ten_thongtin", "Diện tích")
                        .get()
                        .addOnSuccessListener { detailSnapshot ->
                            val chiTiet = detailSnapshot.documents.firstOrNull()
                            val dienTich = chiTiet?.getDouble("so_luong_donvi")
                            roomData.Dien_tich = dienTich?.toLong()
                            val trangThaiDuyet = document.getString("Trang_thaiduyet")
                            val trangThaiLuu = document.getBoolean("Trang_thailuu")
                            val trangThaiPhong = document.getBoolean("Trang_thaiphong")
                            Log.d(
                                TAG,
                                "TrangThaiDuyet: $trangThaiDuyet, TrangThaiLuu: $trangThaiLuu, TrangThaiPhong: $trangThaiPhong"
                            )

                            if (trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                                // Thêm dữ liệu vào danh sách
                                listFilterPrice.add(Pair(id, roomData))
                            }

                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching ChiTietThongTin for room $id: ${e.message}")
                        }
                    tasks.add(task)
                }

                // Chờ tất cả truy vấn hoàn thành
                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    if (listFilterPrice.isEmpty()) {
                        binding.rvListRoom.visibility = View.GONE
                        binding.layoutNullMsg.visibility = View.VISIBLE
                        binding.layoutLoading.visibility = View.GONE
                    } else {
                        binding.rvListRoom.visibility = View.VISIBLE
                        binding.layoutNullMsg.visibility = View.GONE
                        listFullRoom = listFilterPrice
                        updateUI(listFullRoom, homeViewModel)
                        adapter.notifyDataSetChanged()
                    }

                    Log.d(TAG, "onPriceRangeSelected: Filtered list: $listFilterPrice")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching rooms: ${e.message}")
                binding.rvListRoom.visibility = View.GONE
                binding.layoutNullMsg.visibility = View.VISIBLE
            }.addOnCompleteListener {
                binding.layoutLoading.visibility = View.GONE
            }


        max = maxPrice
        min = minPrice

    }


    override fun onFilterSelected(
        selectedTypes: MutableList<String>,
        selectedTienNghi: MutableList<String>,
        selectedNoiThat: MutableList<String>
    ) {
        selectedTypesViewModel = selectedTypes
        selectedNoiThatViewModel = selectedNoiThat
        selectedTienNghiViewModel = selectedTienNghi
        Log.d(TAG, "onFilterSelected:selectedTypes $selectedTypes")
        Log.d(TAG, "onFilterSelected:selectedTienNghi $selectedTienNghi")
        Log.d(TAG, "onFilterSelected:selectedNoiThat $selectedNoiThat")

//        val firestore = FirebaseFirestore.getInstance()

        var loaiPhongTask: Task<QuerySnapshot>? = null
        loaiPhongTask = if (selectedTypes.isNotEmpty()) {
            firestore.collection("LoaiPhong")
                .whereIn("Ten_loaiphong", selectedTypes)
                .get()
        } else {
            firestore.collection("LoaiPhong").get() // Lấy tất cả nếu không có chọn
        }

        // Lọc TienNghi nếu có tiêu chí selectedTienNghi
        var tienNghiTask: Task<QuerySnapshot>? = null
        tienNghiTask = if (selectedTienNghi.isNotEmpty()) {
            firestore.collection("TienNghi")
                .whereIn("Ten_tiennghi", selectedTienNghi)
                .get()
        } else {
            firestore.collection("TienNghi").get() // Lấy tất cả nếu không có chọn
        }

        // Lọc NoiThat nếu có tiêu chí selectedNoiThat
        var noiThatTask: Task<QuerySnapshot>? = null
        noiThatTask = if (selectedNoiThat.isNotEmpty()) {
            firestore.collection("NoiThat")
                .whereIn("Ten_noithat", selectedNoiThat)
                .get()
        } else {
            firestore.collection("NoiThat").get() // Lấy tất cả nếu không có chọn
        }


        // Chờ tất cả các truy vấn hoàn thành
        Tasks.whenAllComplete(loaiPhongTask, tienNghiTask, noiThatTask)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Xử lý kết quả của LoaiPhong
                    val maLoaiPhongList =
                        loaiPhongTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã loại phòng: $maLoaiPhongList")

                    // Xử lý kết quả của TienNghi
                    val maTienNghiList =
                        tienNghiTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã tiện nghi: $maTienNghiList")

                    // Xử lý kết quả của NoiThat
                    val maNoiThatList = noiThatTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã nội thất: $maNoiThatList")

                    binding.layoutLoading.visibility = View.VISIBLE
                    binding.rvListRoom.visibility = View.GONE
                    binding.layoutNullMsg.visibility = View.GONE
                    listFullRoom.clear()
                    // Truy vấn bảng PhongTro và lọc theo mã loại phòng
                    firestore.collection("PhongTro")
                        .whereIn("Ma_loaiphong", maLoaiPhongList)
                        .get()
                        .addOnSuccessListener { phongTroSnapshot ->
                            val phongTroIds = phongTroSnapshot.documents.map { it.id }
                            Log.d(TAG, "Danh sách phòng trọ với loại phòng: $phongTroIds")

                            // Lọc các phòng trọ có nội thất
                            firestore.collection("PhongTroNoiThat")
                                .whereIn("ma_noithat", maNoiThatList)
                                .get()
                                .addOnSuccessListener { phongTroNoiThatSnapshot ->
                                    val phongTroWithNoiThatIds =
                                        phongTroNoiThatSnapshot.documents.map { it.getString("ma_phongtro") }
                                    Log.d(
                                        TAG,
                                        "Danh sách phòng trọ có nội thất: $phongTroWithNoiThatIds"
                                    )

                                    // Lọc các phòng trọ có tiện nghi
                                    firestore.collection("PhongTroTienNghi")
                                        .whereIn("ma_tiennghi", maTienNghiList)
                                        .get()
                                        .addOnSuccessListener { phongTroTienNghiSnapshot ->
                                            val phongTroWithTienNghiIds =
                                                phongTroTienNghiSnapshot.documents.map {
                                                    it.getString("ma_phongtro")
                                                }
                                            Log.d(
                                                TAG,
                                                "Danh sách phòng trọ có tiện nghi: $phongTroWithTienNghiIds"
                                            )
                                            // Tìm giao của ba danh sách phòng trọ
                                            val finalRooms = phongTroIds
                                                .intersect(phongTroWithNoiThatIds.toSet())
                                                .intersect(phongTroWithTienNghiIds.toSet())
                                            Log.d(TAG, "Danh sách phòng trọ cuối cùng: $finalRooms")
                                            if (finalRooms.isEmpty()) {
                                                binding.layoutNullMsg.visibility = View.VISIBLE
                                                binding.rvListRoom.visibility = View.GONE
                                                binding.layoutLoading.visibility = View.GONE
                                            } else {
                                                binding.layoutNullMsg.visibility = View.GONE
                                                binding.rvListRoom.visibility = View.VISIBLE
                                                firestore.collection("PhongTro")
                                                    .whereIn(
                                                        FieldPath.documentId(),
                                                        finalRooms.toList()
                                                    ) // Lọc theo danh sách ID
                                                    .get()
                                                    .addOnSuccessListener { querySnapshot ->
                                                        // Chuyển dữ liệu từ querySnapshot thành list các đối tượng phòng trọ
                                                        for (document in querySnapshot) {
                                                            val id = document.id
                                                            val roomData =
                                                                document.toObject(PhongTroModel::class.java)
                                                            Log.d(
                                                                TAG,
                                                                "onFilterSelected: roomData $roomData"
                                                            )
                                                            firestore.collection("ChiTietThongTin")
                                                                .whereEqualTo(
                                                                    "ma_phongtro",
                                                                    id
                                                                ) // Truy vấn theo mã phòng trọ
                                                                .whereEqualTo(
                                                                    "ten_thongtin",
                                                                    "Diện tích"
                                                                ) // Lọc theo thông tin "Diện tích"
                                                                .get()
                                                                .addOnSuccessListener { chiTietSnapshot ->
                                                                    val chiTiet =
                                                                        chiTietSnapshot.documents.firstOrNull()
                                                                    val dienTich =
                                                                        chiTiet?.getDouble("so_luong_donvi") // Lấy giá trị diện tích

                                                                    // Cập nhật diện tích vào đối tượng phòng
                                                                    roomData.Dien_tich =
                                                                        dienTich?.toLong()
                                                                    val trangThaiDuyet =
                                                                        document.getString("Trang_thaiduyet")
                                                                    val trangThaiLuu =
                                                                        document.getBoolean("Trang_thailuu")
                                                                    val trangThaiPhong =
                                                                        document.getBoolean("Trang_thaiphong")
                                                                    Log.d(
                                                                        TAG,
                                                                        "TrangThaiDuyet: $trangThaiDuyet, TrangThaiLuu: $trangThaiLuu, TrangThaiPhong: $trangThaiPhong"
                                                                    )

                                                                    if (trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                                                                        // Thêm vào danh sách hiển thị
                                                                        listFullRoom.add(
                                                                            Pair(
                                                                                id,
                                                                                roomData
                                                                            )
                                                                        )
                                                                    }

                                                                    // Cập nhật giao diện sau khi hoàn tất
                                                                    if (listFullRoom.size > 0) {
                                                                        binding.layoutLoading.visibility =
                                                                            View.GONE
                                                                        binding.layoutNullMsg.visibility = View.GONE
                                                                        binding.rvListRoom.visibility =
                                                                            View.VISIBLE
                                                                        updateUI(
                                                                            listFullRoom,
                                                                            homeViewModel
                                                                        )
                                                                    }else{
                                                                        Log.d(
                                                                            TAG,
                                                                            "onFilterSelected: else $listFullRoom"
                                                                        )
                                                                        binding.rvListRoom.visibility =
                                                                            View.GONE
                                                                        binding.layoutLoading.visibility = View.GONE
                                                                        binding.layoutNullMsg.visibility = View.VISIBLE
                                                                    }
                                                                }
                                                                .addOnFailureListener { exception ->
                                                                    Log.e(
                                                                        TAG,
                                                                        "Error fetching room details: ${exception.message}"
                                                                    )
                                                                }
                                                        }

//                                                    Cập nhật UI với dữ liệu mới
//                                                    updateUI(listFullRoom, homeViewModel)
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.d(
                                                            TAG,
                                                            "Lỗi khi lấy dữ liệu phòng trọ: ${exception.message}"
                                                        )
                                                    }
                                            }


                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error fetching PhongTroTienNghi", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error fetching PhongTroNoiThat", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching PhongTro", e)
                        }
                } else {
                    Log.e(TAG, "Error fetching filter data", task.exception)
                }
            }
    }


}