package com.ph32395.staynow_datn.ChucNangTimKiem

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ItemHistorySearchBinding

class AdapterHistoryKeyWord(
    context: Context,
    private var item: MutableList<SearchDataModel>,
    val useID: String
) :
    ArrayAdapter<SearchDataModel>(
        context, R.layout.item_history_search, item
    ) {
    private lateinit var binding: ItemHistorySearchBinding

    //    private val database = FirebaseDatabase.getInstance()
//    private val searchHistoryRef = database.getReference("LichSuTimKiem")
    private val firestore = FirebaseFirestore.getInstance()
    private val searchHistoryRef = firestore.collection("LichSuTimKiem")

    private val TAG: String = "zzzz"
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_history_search, parent, false)
        binding = ItemHistorySearchBinding.bind(view)

        val searchData = item[position]

        binding.tvKeyWordSearch.text = item[position].tuKhoa
        binding.ivDelete.setOnClickListener {
            removeSearchData(searchData)
        }

        return view
    }

    fun updateList(newItems: List<SearchDataModel>) {
        item = newItems.toMutableList()
        notifyDataSetChanged()
    }

    private fun removeSearchData(searchData: SearchDataModel) {
        // Tìm vị trí của item cần xóa trong listKeySearch
        val position = item.indexOf(searchData)
        if (position != -1) {
            // Xóa item khỏi danh sách
            item.removeAt(position)
            // Cập nhật adapter
            notifyDataSetChanged()

            // Xóa dữ liệu khỏi Firebase
            deleteSearchFromFirebase(searchData)
        }


    }

    private fun deleteSearchFromFirebase(searchData: SearchDataModel) {
        // Xóa mục tìm kiếm trong Firebase
//        searchHistoryRef.child(useID).child(searchData.ma_timkiem!!).removeValue()
//            .addOnSuccessListener {
//                Log.d("SearchHistory", "Mục tìm kiếm đã được xóa thành công.")
//            }
//            .addOnFailureListener { exception ->
//                Log.e("SearchHistory", "Lỗi khi xóa mục tìm kiếm: ${exception.message}")
//            }
        searchHistoryRef.document(useID).collection("HistoryKeyWord").document(searchData.maTimKiem!!)
            .delete().addOnSuccessListener {
                Log.d(TAG, "deleteSearchFromFirebase: Xoa thanh cong")
            }.addOnFailureListener {
                Log.d(TAG, "deleteSearchFromFirebase: Xoa that bai")
            }
    }
}