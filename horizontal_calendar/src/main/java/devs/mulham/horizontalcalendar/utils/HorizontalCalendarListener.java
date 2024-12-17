package devs.mulham.horizontalcalendar.utils;

import java.util.Calendar;

import devs.mulham.horizontalcalendar.HorizontalCalendarView;


public abstract class HorizontalCalendarListener {

    public abstract void onDateSelected(Calendar date, int position);

    public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {
    }

    public boolean onDateLongClicked(Calendar date, int position) {
        return false;
    }

}