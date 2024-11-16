package com.ph32395.staynow.hieunt.view.feature.schedule_room

import android.graphics.Color
import android.util.Log
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ActivityScheduleRoomBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.ROOM_DETAIL
import com.ph32395.staynow.hieunt.view_model.CommonVM
import com.ph32395.staynow.hieunt.widget.currentBundle
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.model.CalendarEvent
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate
import java.util.Calendar
import java.util.Random

class ScheduleRoomActivity: BaseActivity<ActivityScheduleRoomBinding, CommonVM>() {
    private var horizontalCalendar: HorizontalCalendar? = null


    override fun setViewBinding(): ActivityScheduleRoomBinding {
        return ActivityScheduleRoomBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java

    override fun initView() {
        val roomModel = currentBundle()?.getSerializable(ROOM_DETAIL) as? PhongTroModel ?: PhongTroModel()
        initHorizontalCalendarPicker()
    }

    override fun dataObserver() {

    }

    private fun initHorizontalCalendarPicker() {
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
                //.defaultSelectedDate(calendar)
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
                })
                .build()
            horizontalCalendar?.selectDate(Calendar.getInstance(), true)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

}