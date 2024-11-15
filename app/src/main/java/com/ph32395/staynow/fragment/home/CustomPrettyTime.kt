package com.ph32395.staynow.fragment.home

import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.units.*
import org.ocpsoft.prettytime.format.SimpleTimeFormat
import java.util.*

object PrettyTimeHelper {
    fun createCustomPrettyTime(): PrettyTime {
        val prettyTime = PrettyTime(Locale("vi"))

        prettyTime.clearUnits() // Xóa các định dạng mặc định để thay đổi cách hiển thị

        prettyTime.apply {
            registerUnit(Second(), SimpleTimeFormat().apply {
                setPattern("%n giây trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Minute(), SimpleTimeFormat().apply {
                setPattern("%n phút trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Hour(), SimpleTimeFormat().apply {
                setPattern("%n giờ trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Day(), SimpleTimeFormat().apply {
                setPattern("%n ngày trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Week(), SimpleTimeFormat().apply {
                setPattern("%n tuần trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Month(), SimpleTimeFormat().apply {
                setPattern("%n tháng trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })

            registerUnit(Year(), SimpleTimeFormat().apply {
                setPattern("%n năm trước")
                setPastSuffix("")
                setFutureSuffix("nữa")
            })
        }

        return prettyTime
    }
}
