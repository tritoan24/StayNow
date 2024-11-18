package com.ph32395.staynow.hieunt.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.ph32395.staynow.hieunt.helper.Default.Collection.DAT_PHONG
import com.ph32395.staynow.hieunt.helper.Default.Collection.RENTER_ID
import com.ph32395.staynow.hieunt.helper.Default.Collection.ROOM_SCHEDULE_ID
import com.ph32395.staynow.hieunt.helper.Default.Collection.STATUS
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageScheduleRoomVM : ViewModel() {
    private val _allScheduleRoomState = MutableStateFlow<List<ScheduleRoomModel>>(emptyList())
    private val _scheduleRoomState = MutableStateFlow<List<ScheduleRoomModel>>(emptyList())
    val scheduleRoomState: StateFlow<List<ScheduleRoomModel>> = _scheduleRoomState

    private val firestore = FirebaseFirestore.getInstance()

    fun filerScheduleRoomState(status: Int,onCompletion: () -> Unit = {}) {
        viewModelScope.launch (Dispatchers.IO) {
            _scheduleRoomState.value = _allScheduleRoomState.value.filter { it.status == status }
            withContext(Dispatchers.Main){
                onCompletion.invoke()
            }
        }
    }

    fun fetchAllScheduleByUser(userId: String, onCompletion: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = firestore.collection(DAT_PHONG)
                    .whereEqualTo(RENTER_ID, userId)
                    .get()
                    .await()
                val scheduleRooms = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<ScheduleRoomModel>()
                }
                _allScheduleRoomState.value = scheduleRooms
                onCompletion.invoke(true)
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error: ${e.message}")
                _allScheduleRoomState.value = emptyList()
                onCompletion.invoke(false)
            }
        }
    }

    fun updateScheduleRoomStatus(roomScheduleId: String, status: Int, onCompletion: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentRef = firestore.collection(DAT_PHONG).whereEqualTo(ROOM_SCHEDULE_ID, roomScheduleId)
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

}