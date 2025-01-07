package com.ph32395.staynow_datn.hieunt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel

@Dao
interface ScheduleRoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchedule(model: ScheduleRoomModel)

    @Update
    fun updateSchedule(model: ScheduleRoomModel)

    @Query("SELECT EXISTS(SELECT 1 FROM ScheduleRoomModel WHERE maPhongTro = :maPhongTro LIMIT 1)")
    fun checkRoomExist(maPhongTro: String): Boolean

    @Query("SELECT * FROM ScheduleRoomModel WHERE maPhongTro = :maPhongTro LIMIT 1")
    fun getRoomScheduleRoomId(maPhongTro: String): ScheduleRoomModel?
}