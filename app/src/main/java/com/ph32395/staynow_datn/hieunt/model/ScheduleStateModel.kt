package com.ph32395.staynow_datn.hieunt.model

data class ScheduleStateModel (
    var name : String = "",
    var status : Int = 0,
    var count: Int = 0,
    var isSelected : Boolean = false
)