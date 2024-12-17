package com.ph32395.staynow.hieunt.view_model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.ph32395.staynow.hieunt.helper.Default.Collection.CHANGED_SCHEDULE_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.Collection.DATE
import com.ph32395.staynow.hieunt.helper.Default.Collection.DAT_PHONG
import com.ph32395.staynow.hieunt.helper.Default.Collection.MAP_LINK
import com.ph32395.staynow.hieunt.helper.Default.Collection.MESSAGE
import com.ph32395.staynow.hieunt.helper.Default.Collection.RENTER_ID
import com.ph32395.staynow.hieunt.helper.Default.Collection.ROOM_SCHEDULE_ID
import com.ph32395.staynow.hieunt.helper.Default.Collection.STATUS
import com.ph32395.staynow.hieunt.helper.Default.Collection.TENANT_ID
import com.ph32395.staynow.hieunt.helper.Default.Collection.THONG_BAO
import com.ph32395.staynow.hieunt.helper.Default.Collection.TIME
import com.ph32395.staynow.hieunt.helper.Default.Collection.TIME_STAMP
import com.ph32395.staynow.hieunt.helper.Default.Collection.TITLE
import com.ph32395.staynow.hieunt.helper.Default.Collection.TYPE_NOTIFICATION
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_TENANT
import com.ph32395.staynow.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_RENTER
import com.ph32395.staynow.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_TENANT
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageScheduleRoomVM : ViewModel() {
    private val _allScheduleRoomState = MutableStateFlow<List<ScheduleRoomModel>>(emptyList())
    val allScheduleRoomState: StateFlow<List<ScheduleRoomModel>> = _allScheduleRoomState
    private val _scheduleRoomState = MutableStateFlow<List<ScheduleRoomModel>>(emptyList())
    val scheduleRoomState: StateFlow<List<ScheduleRoomModel>> = _scheduleRoomState

    private val firestore = FirebaseFirestore.getInstance()

    fun filerScheduleRoomState(status: Int, onCompletion: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _scheduleRoomState.value = _allScheduleRoomState.value.filter { it.status == status }
            withContext(Dispatchers.Main) {
                onCompletion.invoke()
            }
        }
    }

    fun fetchAllScheduleByTenant(tenantId: String, onCompletion: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection(DAT_PHONG)
                    .whereEqualTo(TENANT_ID, tenantId)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            Log.d("ManageScheduleRoomVM", "Error: ${exception.message}")
                            _allScheduleRoomState.value = emptyList()  // Khi có lỗi xảy ra
                            onCompletion.invoke(false)
                        } else {
                            val scheduleRooms = querySnapshot?.documents?.mapNotNull { document ->
                                document.toObject<ScheduleRoomModel>()
                            } ?: emptyList()
                            _allScheduleRoomState.value = scheduleRooms
                            Log.d("ManageScheduleRoomVM", "listScheduleRooms: $scheduleRooms")
                            onCompletion.invoke(true)
                        }
                    }
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error: ${e.message}")
                _allScheduleRoomState.value = emptyList()
                onCompletion.invoke(false)
            }
        }
    }

    fun fetchAllScheduleByRenter(renterId: String, onCompletion: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection(DAT_PHONG)
                    .whereEqualTo(RENTER_ID, renterId)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            Log.d("ManageScheduleRoomVM", "Error: ${exception.message}")
                            _allScheduleRoomState.value = emptyList()  // Khi có lỗi xảy ra
                            onCompletion.invoke(false)
                        } else {
                            val scheduleRooms = querySnapshot?.documents?.mapNotNull { document ->
                                document.toObject<ScheduleRoomModel>()
                            } ?: emptyList()
                            _allScheduleRoomState.value = scheduleRooms
                            Log.d("ManageScheduleRoomVM", "listScheduleRooms: $scheduleRooms")
                            onCompletion.invoke(true)
                        }
                    }
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error: ${e.message}")
                _allScheduleRoomState.value = emptyList()
                onCompletion.invoke(false)
            }
        }
    }

    fun updateScheduleRoomStatus(
        roomScheduleId: String,
        status: Int,
        onCompletion: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentRef =
                    firestore.collection(DAT_PHONG).whereEqualTo(ROOM_SCHEDULE_ID, roomScheduleId)
                val querySnapshot = documentRef.get().await()
                if (querySnapshot.isEmpty) {
                    onCompletion.invoke(false)
                    return@launch
                }
                val documentId = querySnapshot.documents.first().id
                firestore.collection(DAT_PHONG).document(documentId)
                    .update(STATUS, status)
                    .addOnSuccessListener {
                        _allScheduleRoomState.value = _allScheduleRoomState.value.map {
                            if (it.roomScheduleId == roomScheduleId) {
                                it.copy(status = status)
                            } else {
                                it
                            }
                        }
                        onCompletion.invoke(true)
                    }
                    .addOnFailureListener {
                        onCompletion.invoke(false)
                    }
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error update status: ${e.message}")
                onCompletion.invoke(false)
            }
        }
    }

    fun updateScheduleRoom(
        roomScheduleId: String,
        newTime: String,
        newDate: String,
        isChangedScheduleByRenter: Boolean = false,
        onCompletion: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentRef =
                    firestore.collection(DAT_PHONG).whereEqualTo(ROOM_SCHEDULE_ID, roomScheduleId)
                val querySnapshot = documentRef.get().await()
                if (querySnapshot.isEmpty) {
                    onCompletion.invoke(false)
                    return@launch
                }
                val documentId = querySnapshot.documents.first().id
                firestore.collection(DAT_PHONG).document(documentId)
                    .update(
                        TIME, newTime,
                        CHANGED_SCHEDULE_BY_RENTER, isChangedScheduleByRenter,
                        DATE, newDate,
                        STATUS, 0
                    )
                    .addOnSuccessListener {
                        _allScheduleRoomState.value = _allScheduleRoomState.value.map {
                            if (it.roomScheduleId == roomScheduleId) {
                                it.copy(
                                    time = newTime,
                                    date = newDate,
                                    status = 0,
                                    changedScheduleByRenter = isChangedScheduleByRenter
                                )
                            } else {
                                it
                            }
                        }
                        onCompletion.invoke(true)
                    }
                    .addOnFailureListener {
                        onCompletion.invoke(false)
                    }
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error update status: ${e.message}")
                onCompletion.invoke(false)
            }
        }
    }

    fun pushNotification(
        titleNotification: String,
        data: ScheduleRoomModel,
        isRenterPushNotification: Boolean = true,
        onCompletion: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val mapLink =
                if (titleNotification == TITLE_CANCELED_BY_RENTER || titleNotification == TITLE_CANCELED_BY_TENANT)
                    null
                else "geo:0,0?q=${Uri.encode(data.roomAddress)}"
            val notificationData = hashMapOf(
                TITLE to titleNotification,
                MESSAGE to "Phòng: ${data.roomName}, Địa chỉ: ${data.roomAddress}",
                DATE to data.date,
                TIME to data.time,
                MAP_LINK to mapLink,
                TIME_STAMP to System.currentTimeMillis(),
                TYPE_NOTIFICATION to if (isRenterPushNotification) TYPE_SCHEDULE_ROOM_TENANT else TYPE_SCHEDULE_ROOM_RENTER
                //thay doi TYPE_NOTIFICATION de them cac pendingIntent trong service neu can
            )
            val database = FirebaseDatabase.getInstance()
            val thongBaoRef = database.getReference(THONG_BAO)
            // neu ChuTro push noti thi luu id NguoiThue va nguoc lai
            val userId = if (isRenterPushNotification) data.tenantId else data.renterId
            val userThongBaoRef = thongBaoRef.child(userId)

            val newThongBaoId = userThongBaoRef.push().key
            if (newThongBaoId != null) {
                userThongBaoRef.child(newThongBaoId).setValue(notificationData)
                    .addOnSuccessListener {
                        onCompletion.invoke(true)
                    }
                    .addOnFailureListener {
                        onCompletion.invoke(false)
                    }
            }
        }
    }

}