package com.ph32395.staynow.ChucNangTimKiem

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.databinding.ActivitySearchBinding
import com.ph32395.staynow.databinding.BottomSheetCitySearchBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchActivity : AppCompatActivity(), BottomSheetFragment.PriceRangeListener {

    private lateinit var binding: ActivitySearchBinding
    private var TAG: String = "zzzzzzzzzz"
    private val database = FirebaseDatabase.getInstance()
    private val searchHistoryRef = database.getReference("LichSuTimKiem")
    var min: Int = 0
    var max: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        readKeyWordSearch(userId)

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

    private fun readKeyWordSearch(userId: String?) {
        val listKeySearch: MutableList<SearchData> = mutableListOf()
        var adapter:AdapterHistoryKeyWord
        searchHistoryRef.child(userId!!).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded: snapshot: $snapshot")
                Log.d(TAG, "onChildAdded: snapshot_value: ${snapshot.value}")
                Log.d(TAG, "onChildAdded: previousChildName: $previousChildName")

                val newData = snapshot.getValue(SearchData::class.java)
                Log.d(TAG, "onChildAdded: newData $newData")
                newData.let {
                    listKeySearch.add(it!!)
                    Log.d(TAG, "onChildAdded: it let ${it.Tu_khoa}")
                }
                adapter = AdapterHistoryKeyWord(this@SearchActivity, listKeySearch, userId)
                binding.lvHistory.adapter = adapter
                Log.d(TAG, "onChildAdded: List Search $listKeySearch")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: snapshot: $snapshot")
                Log.d(TAG, "onChildChanged: previousChildName: $previousChildName")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved: snapshot: $snapshot")
                Log.d(TAG, "onChildRemoved: $listKeySearch")
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
        val searchData = mapOf(
            "Ma_timkiem" to searchId,
            "Tu_khoa" to searchQuery,
            "Thoi_giantimkiem" to formattedTime
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