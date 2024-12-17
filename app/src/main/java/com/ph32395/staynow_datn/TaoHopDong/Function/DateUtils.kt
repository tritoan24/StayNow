// DateUtils.kt
package com.ph32395.staynow_datn.utils

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun updateEndDateBasedOnMonths(startDate: Calendar, monthsText: String, tvEndDate: TextView) {
        val months = monthsText.replace(" Tháng", "").toIntOrNull() ?: return

        val endDate = Calendar.getInstance().apply {
            time = startDate.time
            add(Calendar.MONTH, months)
        }
        tvEndDate.text = dateFormat.format(endDate.time)
    }

    fun updateMonthsBasedOnDates(startDate: Calendar, endDate: Calendar, tvMonth: TextView) {
        val diffInMillis = endDate.timeInMillis - startDate.timeInMillis
        if (diffInMillis > 0) {
            val monthsDiff = diffInMillis / (1000L * 60 * 60 * 24 * 30) // Số tháng ước tính
            val daysDiff = (diffInMillis / (1000L * 60 * 60 * 24)) % 30 // Số ngày dư

            if (monthsDiff == 0L) {
                tvMonth.text = "${daysDiff} Ngày"
            } else {
                tvMonth.text = if (daysDiff > 0) {
                    "${monthsDiff} Tháng ${daysDiff} Ngày"
                } else {
                    "${monthsDiff} Tháng"
                }
            }
        } else {
            tvMonth.text = "0 Tháng" // Trường hợp ngày kết thúc trước ngày bắt đầu
        }
    }

    fun showMonthPicker(context: AppCompatActivity, tvMonth: TextView, startDate: Calendar, tvEndDate: TextView) {
        val months = (1..12).map { "$it Tháng" }

        MaterialDialog(context).show {
            title(text = "Chọn tháng")
            listItems(items = months) { _, index, _ ->
                val selectedMonths = index + 1 // Chỉ số bắt đầu từ 0, nên cần cộng thêm 1
                tvMonth.text = "$selectedMonths Tháng"
                // Cập nhật ngày kết thúc dựa trên số tháng
                updateEndDateBasedOnMonths(startDate, "$selectedMonths Tháng", tvEndDate)
            }
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}
