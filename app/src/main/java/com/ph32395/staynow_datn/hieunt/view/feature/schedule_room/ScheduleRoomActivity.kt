package com.ph32395.staynow_datn.hieunt.view.feature.schedule_room

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ActivityScheduleRoomBinding
import com.ph32395.staynow_datn.hieunt.base.BaseActivity
import com.ph32395.staynow_datn.hieunt.custom_view.WheelView
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.DATE
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.DAT_PHONG
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.HO_TEN
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.MAP_LINK
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.MESSAGE
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.NGUOI_DUNG
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.SO_DIEN_THOAI
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.THONG_BAO
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.TIME
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.TIME_STAMP
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.TITLE
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.TYPE_NOTIFICATION
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_DETAIL
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_ID
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_SCHEDULE
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_SCHEDULE_ROOM_SUCCESSFULLY
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_RENTER
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow_datn.hieunt.view.feature.ScheduleRoomSuccessActivity
import com.ph32395.staynow_datn.hieunt.view_model.CommonVM
import com.ph32395.staynow_datn.hieunt.widget.currentBundle
import com.ph32395.staynow_datn.hieunt.widget.getTextEx
import com.ph32395.staynow_datn.hieunt.widget.launchActivity
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.hieunt.widget.toast
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.model.CalendarEvent
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class ScheduleRoomActivity : BaseActivity<ActivityScheduleRoomBinding, CommonVM>() {
    private var horizontalCalendar: HorizontalCalendar? = null
    private val simpleDateFormat by lazy { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    private lateinit var dateSelected: String
    private lateinit var roomModel: PhongTroModel
    private var hours: String = "00"
    private var minutes: String = "00"
    private var roomIdInDetail : String = ""
    private var renterNameByGetInfo = ""
    private var renterPhoneNumberByGetInfo = ""
    private var tenantNameByGetInfo = ""
    private var tenantPhoneNumberByGetInfo = ""

    override fun setViewBinding(): ActivityScheduleRoomBinding {
        return ActivityScheduleRoomBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java    

    override fun initView() {
        roomModel = currentBundle()?.getSerializable(ROOM_DETAIL) as? PhongTroModel ?: PhongTroModel()
        roomIdInDetail = currentBundle()?.getString(ROOM_ID).toString()

        if (roomIdInDetail.isEmpty()){
            toast("Có lỗi khi lấy thông tin!")
            finish()
        }

        initHorizontalCalendarPicker()
        initWheelView()
        showLoading()

        getAllInfo { isSuccess ->
            dismissLoading()
            if (isSuccess) {
                binding.tvConfirm.apply {
                    isEnabled = true
                    alpha = 1f
                }
            } else {
                toast("Có lỗi khi lấy thông tin!")
            }
        }
    }

    override fun initClickListener() {
        binding.apply {
            ivBack.tap {
                onBackPressedSystem()
            }
            tvConfirm.tap {
                showLoading()
                val scheduleRoomModel = ScheduleRoomModel().apply {
                    maPhongTro = roomIdInDetail
                    tenPhong = roomModel.Ten_phongtro
                    maChuTro = roomModel.maNguoiDung
                    diaChiPhong = roomModel.Dia_chichitiet
                    tenChuTro = renterNameByGetInfo
                    sdtChuTro = renterPhoneNumberByGetInfo
                    maNguoiThue = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    tenNguoiThue = tenantNameByGetInfo
                    sdtNguoiThue = tenantPhoneNumberByGetInfo
                    ngayDatPhong = dateSelected
                    thoiGianDatPhong = "${hours}:${minutes}"
                    ghiChu = edtNote.getTextEx()
                    trangThaiDatPhong = 0
                }
                addScheduleRoomToFireStore(scheduleRoomModel) { isCompletion ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        dismissLoading()
                        if (isCompletion) {
                            pushNotification(scheduleRoomModel){
                                toastNotification(it)
                            }
                            launchActivity(Bundle().apply { putSerializable(ROOM_SCHEDULE, scheduleRoomModel) }, ScheduleRoomSuccessActivity::class.java)
                        } else {
                            toast("Error")
                        }
                    }
                }
            }
        }
    }

    override fun dataObserver() {

    }

    private fun toastNotification(isCompletion: Boolean) {
        lifecycleScope.launch {
            if (isCompletion)
                toast("Thông báo đã được gửi đến chủ trọ")
            else
                toast("Có lỗi xảy ra!")
        }
    }

    private fun addScheduleRoomToFireStore(
        schedule: ScheduleRoomModel,
        onCompletion: (Boolean) -> Unit = {}
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val documentRef = FirebaseFirestore.getInstance().collection(DAT_PHONG).document()
                val scheduleWithId = schedule.copy(maDatPhong = documentRef.id)
                documentRef.set(scheduleWithId).addOnSuccessListener {
                    onCompletion.invoke(true)
                }.addOnFailureListener { e ->
                    Log.d("addScheduleRoomToFireStore", "Error adding document: ${e.message}")
                    onCompletion.invoke(false)
                }
            } catch (e: Exception) {
                Log.d("addScheduleRoomToFireStore", "Error: ${e.message}")
                onCompletion.invoke(false)
            }
        }
    }

    private fun pushNotification(data: ScheduleRoomModel, onCompletion: (Boolean) -> Unit){
        lifecycleScope.launch(Dispatchers.IO) {
            val mapLink = null
            val notificationData = hashMapOf(
                TITLE to TITLE_SCHEDULE_ROOM_SUCCESSFULLY,
                MESSAGE to "Phòng: ${data.tenPhong}, Địa chỉ: ${data.diaChiPhong}",
                DATE to data.ngayDatPhong,
                TIME to data.thoiGianDatPhong,
                MAP_LINK to mapLink,
                TIME_STAMP to System.currentTimeMillis(),
                TYPE_NOTIFICATION to TYPE_SCHEDULE_ROOM_RENTER
                //thay doi TYPE_NOTIFICATION de them cac pendingIntent trong service neu can
            )
            val database = FirebaseDatabase.getInstance()
            val thongBaoRef = database.getReference(THONG_BAO)
            // neu ChuTro push noti thi luu id NguoiThue va nguoc lai
            val userId = data.maChuTro
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

    private fun getAllInfo(onCompletion: (Boolean) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            var isSuccess = true
            val jobCallInfoRenter = async {
                try {
                    FirebaseDatabase.getInstance().reference.child(NGUOI_DUNG)
                        .child(roomModel.maNguoiDung).get()
                        .addOnSuccessListener { data ->
                            data?.let {
                                renterNameByGetInfo = it.child(HO_TEN).value as? String ?: ""
                                renterPhoneNumberByGetInfo = it.child(SO_DIEN_THOAI).value as? String ?: ""
                            }
                        }
                        .addOnFailureListener {
                            isSuccess = false
                        }
                } catch (e: Exception) {
                    isSuccess = false
                }
            }

            val jobCallInfoTenant = async {
                try {
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        FirebaseDatabase.getInstance().reference.child(NGUOI_DUNG).child(it).get()
                            .addOnSuccessListener { data ->
                                data?.let {
                                    tenantNameByGetInfo = data.child(HO_TEN).value as? String ?: ""
                                    tenantPhoneNumberByGetInfo = data.child(SO_DIEN_THOAI).value as? String ?: ""
                                }
                            }.addOnFailureListener {
                                isSuccess = false
                            }
                    }
                } catch (e: Exception) {
                    isSuccess = false
                }
            }

            awaitAll(jobCallInfoRenter, jobCallInfoTenant)
            withContext(Dispatchers.Main) {
                onCompletion.invoke(isSuccess)
            }
        }
    }

    private fun initHorizontalCalendarPicker() {
        dateSelected = simpleDateFormat.format(Calendar.getInstance().time)

        val startDate = Calendar.getInstance()
        startDate.add(Calendar.YEAR, -5)
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 5)

        try {
            horizontalCalendar = HorizontalCalendar.Builder(binding.root, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .configure()
                .formatTopText("MMM")
                .formatMiddleText("dd")
                .formatBottomText("EEE")
                .showTopText(true)
                .showBottomText(true)
                .textColor(Color.LTGRAY, Color.WHITE)
                .colorTextMiddle(Color.LTGRAY, Color.parseColor("#ffd54f"))
                .end()
                .addEvents(object : CalendarEventsPredicate {
                    var rnd = Random()
                    override fun events(date: Calendar): List<CalendarEvent> {
                        val events: MutableList<CalendarEvent> = ArrayList()
                        val count = rnd.nextInt(6)
                        for (i in 0..count) {
                            events.add(
                                CalendarEvent(
                                    Color.rgb(
                                        rnd.nextInt(256),
                                        rnd.nextInt(256),
                                        rnd.nextInt(256)
                                    ), "event"
                                )
                            )
                        }
                        return events
                    }
                }).build().apply {
                    selectDate(Calendar.getInstance(), true)
                    calendarListener = object : HorizontalCalendarListener() {
                        override fun onDateSelected(date: Calendar, position: Int) {
                            dateSelected = simpleDateFormat.format(date.time)
                            Log.d("setDataTime", "date: $dateSelected")
                        }
                    }
                }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun initWheelView() {
        binding.hourWheelView.setDataItems((0 until 24).map { "%02d".format(it) }.toMutableList())
        binding.minuteWheelView.setDataItems((0 until 60).map { "%02d".format(it) }.toMutableList())
        binding.hourWheelView.setOnItemSelectedListener(object : WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                hours = (data as String)
                Log.d("setDataTime", "hours: $hours")
            }
        })
        binding.minuteWheelView.setOnItemSelectedListener(object :
            WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                minutes = (data as String)
                Log.d("setDataTime", "minutes: $minutes")
            }
        })
    }

}