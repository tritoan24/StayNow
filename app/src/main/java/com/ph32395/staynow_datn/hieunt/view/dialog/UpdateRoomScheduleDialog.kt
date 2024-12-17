package com.ph32395.staynow_datn.hieunt.view.dialog

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.DialogUpdateRoomScheduleBinding
import com.ph32395.staynow_datn.hieunt.base.BaseFragmentDialog
import com.ph32395.staynow_datn.hieunt.custom_view.WheelView
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.hieunt.widget.toast
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.model.CalendarEvent
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class UpdateRoomScheduleDialog(
    private val scheduleRoomModel: ScheduleRoomModel,
    private val onClickConfirm : (String, String) -> Unit
): BaseFragmentDialog<DialogUpdateRoomScheduleBinding>(true) {
    private var horizontalCalendar: HorizontalCalendar? = null
    private val simpleDateFormat by lazy { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    private lateinit var dateSelected: String
    private var hours: String = "00"
    private var minutes: String = "00"

    override fun initView() {
        initWheelView()
        initHorizontalCalendarPicker()
    }

    override fun initClickListener() {
        binding.tvConfirm.tap {
            if (scheduleRoomModel.date == dateSelected && scheduleRoomModel.time == "${hours}:${minutes}"){
                toast("Ngày và giờ không có sự thay đổi")
            } else {
                onClickConfirm.invoke("${hours}:${minutes}", dateSelected)
                dismiss()
            }
        }
    }

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogUpdateRoomScheduleBinding {
        return DialogUpdateRoomScheduleBinding.inflate(inflater,container,false)
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