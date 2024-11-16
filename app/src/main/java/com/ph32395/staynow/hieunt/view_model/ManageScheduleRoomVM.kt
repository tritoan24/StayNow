package com.ph32395.staynow.hieunt.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.ph32395.staynow.hieunt.helper.Default.Collection.DAT_PHONG
import com.ph32395.staynow.hieunt.helper.Default.Collection.MA_NGUOI_DUNG
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageScheduleRoomVM: ViewModel() {
    private val _scheduleRoomState = MutableStateFlow<List<ScheduleRoomModel>>(emptyList())
    val scheduleRoomState : StateFlow<List<ScheduleRoomModel>> = _scheduleRoomState

    private val firestore = FirebaseFirestore.getInstance()

    fun filterRoomByState(state: Int) {
        _scheduleRoomState.value = scheduleRoomState.value.filter { it.status == state}.toMutableList()
    }

    fun fetchAllScheduleByUser(userId: String, onCompletion : (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = firestore.collection(DAT_PHONG)
                    .whereEqualTo(MA_NGUOI_DUNG, userId)
                    .get()
                    .await()
                val scheduleRooms = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<ScheduleRoomModel>()
                }
                _scheduleRoomState.value = scheduleRooms
                onCompletion.invoke(true)
            } catch (e: Exception) {
                Log.d("ManageScheduleRoomVM", "Error: ${e.message}")
                _scheduleRoomState.value = emptyList()
                onCompletion.invoke(false)
            }
        }
    }
}