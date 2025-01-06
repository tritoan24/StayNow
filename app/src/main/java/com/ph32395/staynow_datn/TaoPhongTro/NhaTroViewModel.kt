package com.ph32395.staynow_datn.TaoPhongTro

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhong
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel



class NhaTroViewModel : ViewModel() {
    private val TAG = "NhaTroViewModel"
    private val nhaTroRef = FirebaseFirestore.getInstance().collection("NhaTro")

    private val _listNhaTro = MutableLiveData<List<NhaTroModel>>(emptyList())
    val listNhaTro: LiveData<List<NhaTroModel>> = _listNhaTro

    private val _selectedNhaTro = MutableLiveData<NhaTroModel?>()
    val selectedNhaTro: LiveData<NhaTroModel?> = _selectedNhaTro

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private val _isDuplicateRoom = MutableLiveData<Boolean>()
    val isDuplicateRoom: LiveData<Boolean> = _isDuplicateRoom

    // Add a new LiveData for the selected nhatro's details
    private val _selectedNhaTroDetails = MutableLiveData<NhaTroModel?>()
    val selectedNhaTroDetails: LiveData<NhaTroModel?> = _selectedNhaTroDetails

    // Add a method to update selected nhatro
    fun updateSelectedNhaTro(nhaTro: NhaTroModel) {
        _selectedNhaTroDetails.value = nhaTro
    }

    // Lấy toàn bộ danh sách nhà trọ theo userId (one-time fetch)
    fun getAllNhaTroByUserId(idUser: String) {
        _isLoading.value = true
        _error.value = null

        nhaTroRef.document(idUser)
            .collection("DanhSachNhaTro")
            .get()
            .addOnSuccessListener { documents ->
                val nhaTroList = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(NhaTroModel::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document ${doc.id}", e)
                        null
                    }
                }
                _listNhaTro.value = nhaTroList
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting documents", e)
                _error.value = e.message
                _isLoading.value = false
            }
    }

    // Lắng nghe thay đổi danh sách nhà trọ theo userId (real-time updates)
    fun listenToNhaTroChanges(idUser: String) {
        _isLoading.value = true
        _error.value = null

        nhaTroRef.document(idUser)
            .collection("DanhSachNhaTro")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    _error.value = e.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val nhaTroList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(NhaTroModel::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                _listNhaTro.value = nhaTroList
                _isLoading.value = false
            }
    }

    // Lấy thông tin chi tiết một nhà trọ theo id (one-time fetch)
    fun getNhaTroById(idUser: String, nhaTroId: String) {
        _isLoading.value = true
        _error.value = null

        nhaTroRef.document(idUser)
            .collection("DanhSachNhaTro")
            .document(nhaTroId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        val nhaTro = document.toObject(NhaTroModel::class.java)
                        _selectedNhaTro.value = nhaTro
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document", e)
                        _error.value = "Lỗi khi xử lý dữ liệu"
                    }
                } else {
                    _error.value = "Không tìm thấy nhà trọ"
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting document", e)
                _error.value = e.message
                _isLoading.value = false
            }
    }

    // Lắng nghe thay đổi thông tin chi tiết một nhà trọ (real-time updates)
    fun listenToNhaTroDetails(idUser: String, nhaTroId: String) {
        _isLoading.value = true
        _error.value = null

        nhaTroRef.document(idUser)
            .collection("DanhSachNhaTro")
            .document(nhaTroId)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    _error.value = e.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        val nhaTro = documentSnapshot.toObject(NhaTroModel::class.java)
                        _selectedNhaTro.value = nhaTro
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document", e)
                        _error.value = "Lỗi khi xử lý dữ liệu"
                    }
                } else {
                    _selectedNhaTro.value = null
                    _error.value = "Không tìm thấy nhà trọ"
                }
                _isLoading.value = false
            }
    }

    fun checkDuplicateRoom(maNhaTro: String, tenPhong: String) {
        firestore.collection("PhongTro")
            .whereEqualTo("maNhaTro", maNhaTro)
            .get()
            .addOnSuccessListener { documents ->
                var isDuplicate = false
                for (document in documents) {
                    val existingRoomName = document.getString("tenPhongTro")
                    if (existingRoomName == tenPhong) {
                        isDuplicate = true
                        break
                    }
                }
                _isDuplicateRoom.value = isDuplicate
            }
            .addOnFailureListener { exception ->
                Log.e("PhongCheckViewModel", "Error checking duplicate room: ", exception)
                _isDuplicateRoom.value = false
            }
    }
}
