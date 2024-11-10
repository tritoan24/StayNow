package com.ph32395.staynow.ChucNangTimKiem

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.Model.PhongTro
import com.ph32395.staynow.databinding.ActivitySearchBinding
import com.ph32395.staynow.databinding.BottomSheetCitySearchBinding
import com.ph32395.staynow.fragment.home.PhongTroAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchActivity : AppCompatActivity(), BottomSheetFragment.PriceRangeListener {

    private lateinit var binding: ActivitySearchBinding
    private var TAG: String = "zzzzzzzzzz"
    private val database = FirebaseDatabase.getInstance()
    private val searchHistoryRef = database.getReference("LichSuTimKiem")
    private val dataRoom = database.getReference("PhongTro")
    val listFullRoom: MutableList<PhongTro> = mutableListOf()
    val listKeySearch: MutableList<SearchDataModel> = mutableListOf()
    var min: Int = 0
    var max: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        readKeyWordSearch(userId)

        val adapter = PhongTroAdapter(listFullRoom)
        binding.rvListRoom.layoutManager = GridLayoutManager(this@SearchActivity, 2)
        binding.rvListRoom.adapter = adapter
        readListRoom(adapter)

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
                readListRoom(adapter)
            }
            hideKeyClearFocus()
        }

        binding.btnKhoangGia.setOnClickListener {
            val priceRangeBottomSheet = BottomSheetFragment()
            priceRangeBottomSheet.show(supportFragmentManager, priceRangeBottomSheet.tag)
            priceRangeBottomSheet.updatePriceRange(min, max)
        }

        binding.btnCity.setOnClickListener {
            showBottomSheetCity()
        }
        binding.ivFilter.setOnClickListener {
            val bottomSheetFragment = BottomSheetFilter()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }


    // Hàm tìm kiếm tương đối trong name hoặc address
    fun searchRoomByNameOrDescription(query: String, adapter: PhongTroAdapter) {
        dataRoom.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<PhongTro>()
                val queryWords = query.split(" ").filter { it.isNotEmpty() }

                Log.d(TAG, "onDataChange: queryWords $queryWords")
                Log.d(TAG, "onDataChange: query $query")

                for (roomSnapshot in snapshot.children) {
                    val roomData = roomSnapshot.getValue(PhongTro::class.java)
                    val roomName = roomData?.tenPhongTro ?: ""
                    val roomDescription = roomData?.motaChiTiet ?: ""

                    // Kiểm tra nếu toàn bộ chuỗi `query` xuất hiện trong tên hoặc mô tả
                    val queryInDescriptionOrName = roomName.contains(query, ignoreCase = true) ||
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
                        filteredList.add(roomData!!)
                        binding.rvListRoom.visibility = View.VISIBLE
                        binding.layoutNullMsg.visibility = View.GONE
                        Log.d(TAG, "onDataChange: Rom $roomData (tìm kiếm chi tiết hoặc tương đối)")
                    }
                }

                adapter.updateList(filteredList)

                if (filteredList.isEmpty()) {
                    Log.d(TAG, "Không tìm thấy phòng trọ nào với từ khóa: $query")
                    binding.rvListRoom.visibility = View.GONE
                    binding.layoutNullMsg.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu: ${error.message}")
            }
        })
    }

    //Cung có the dung ham nay neu thay han tren khong on
//    fun searchRoomByNameOrDescription2(query: String, adapter: PhongTroAdapter) {
//        dataRoom.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val filteredList = mutableListOf<PhongTro>()
//                val cleanedQuery = query.trim().replace(Regex("\\s+"), " ") // Xóa khoảng trắng thừa
//                val queryWords = cleanedQuery.split(" ").filter { it.isNotEmpty() }
//
//                Log.d(TAG, "onDataChange: cleanedQuery $cleanedQuery")
//                Log.d(TAG, "onDataChange: queryWords $queryWords")
//
//                // Tìm kiếm theo cụm từ đầy đủ
//                for (roomSnapshot in snapshot.children) {
//                    val roomData = roomSnapshot.getValue(PhongTro::class.java)
//                    val roomName = roomData?.tenPhongTro ?: ""
//                    val roomDescription = roomData?.motaChiTiet ?: ""
//
//                    if (roomName.contains(cleanedQuery, ignoreCase = true) ||
//                        roomDescription.contains(cleanedQuery, ignoreCase = true)) {
//                        filteredList.add(roomData!!)
//                        Log.d(TAG, "onDataChange: Rom $roomData (tìm kiếm theo cụm từ đầy đủ)")
//                    }
//                }
//
//                // Nếu không có kết quả theo cụm từ đầy đủ, thực hiện tìm kiếm từng từ khóa
//                if (filteredList.isEmpty()) {
//                    for (roomSnapshot in snapshot.children) {
//                        val roomData = roomSnapshot.getValue(PhongTro::class.java)
//                        val roomName = roomData?.tenPhongTro ?: ""
//                        val roomDescription = roomData?.motaChiTiet ?: ""
//
//                        val allWordsMatch = queryWords.all { word ->
//                            roomName.contains(word, ignoreCase = true) ||
//                                    roomDescription.contains(word, ignoreCase = true)
//                        }
//
//                        if (allWordsMatch) {
//                            filteredList.add(roomData!!)
//                            Log.d(TAG, "onDataChange: Rom $roomData (tìm kiếm từng từ khóa)")
//                        }
//                    }
//                }
//
//                adapter.updateList(filteredList)
//
//                if (filteredList.isEmpty()) {
//                    Log.d(TAG, "Không tìm thấy phòng trọ nào với từ khóa: $query")
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e(TAG, "Lỗi khi đọc dữ liệu: ${error.message}")
//            }
//        })
//    }


    private fun readListRoom(adapter: PhongTroAdapter) {

        dataRoom.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: snapshotRoom $snapshot")

                listFullRoom.clear()

                // Duyệt qua các node con trong "rooms"
                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(PhongTro::class.java)
                    room?.let {
                        listFullRoom.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
                error.toException().printStackTrace()
            }
        })

    }

    private fun readKeyWordSearch(userId: String?) {
        var adapter: AdapterHistoryKeyWord = AdapterHistoryKeyWord(this, mutableListOf(), userId!!)
        searchHistoryRef.child(userId).orderByChild("timestamps")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildAdded: snapshot: $snapshot")
                    Log.d(TAG, "onChildAdded: snapshot_value: ${snapshot.value}")
                    Log.d(TAG, "onChildAdded: previousChildName: $previousChildName")

                    val newData = snapshot.getValue(SearchDataModel::class.java)
                    Log.d(TAG, "onChildAdded: newData $newData")
                    newData.let {
                        listKeySearch.add(it!!)
                        Log.d(TAG, "onChildAdded: it let ${it.tu_khoa}")
                    }
                    if (listKeySearch.size >= 3) {
                        Log.d(TAG, "onChildAdded: listKeySearch.size >= 3 Three size list search")
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

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildChanged: snapshot: $snapshot")
                    Log.d(TAG, "onChildChanged: previousChildName: $previousChildName")
                    Log.d(TAG, "onChildChanged: listKeySearch $listKeySearch")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d(TAG, "onChildRemoved: snapshot: $snapshot")
                    Log.d(TAG, "onChildRemoved: $listKeySearch")
                    if (listKeySearch.size < 3) {
                        Log.d(TAG, "onChildAdded: listKeySearch.size >= 3 Three size list search")
                        binding.lvHistory.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildMoved: snapshot: $snapshot")
                    Log.d(TAG, "onChildMoved: previousChildName: $previousChildName")

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: error: $error")
                }

            })

    }

    private fun saveKeyWordSearch(text: Editable) {
        Log.d(TAG, "saveKeyWordSearch: text $text")

        // Lấy reference đến Realtime Database

        //  userId
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "saveKeyWordSearch: userID $userId")
        Log.d(TAG, "saveKeyWordSearch: database $database")
        Log.d(TAG, "saveKeyWordSearch: searchHistoryRef $searchHistoryRef")

        val timeStamp = System.currentTimeMillis() // Lấy thời gian hiện tại
        val formattedTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
            Date(timeStamp)
        )
        val searchQuery = text.toString() // Từ khóa tìm kiếm
        // Tạo một ID tìm kiếm ngẫu nhiên
        val searchId = searchHistoryRef.child(userId!!).push().key ?: return

        // Tạo đối tượng lưu vào Firebase
//        val searchData = mapOf(
//            "Ma_timkiem" to searchId,
//            "Tu_khoa" to searchQuery,
//            "Thoi_giantimkiem" to formattedTime,
//            "timestamps" to timeStamp
//        )
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
        // Lưu vào Firebase
        searchHistoryRef.child(userId).child(searchId).setValue(searchData)
            .addOnSuccessListener {
                Log.d("SearchHistory", "Tìm kiếm đã được lưu vào Firebase.")
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

    @SuppressLint("SetTextI18n")
    override fun onPriceRangeSelected(minPrice: Int, maxPrice: Int) {
        Log.d(TAG, "onPriceRangeSelected: Min: $minPrice max: $maxPrice search")
        max = maxPrice
        min = minPrice

    }


}