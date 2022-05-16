package it.polito.g20app

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimeSlotDAO {

    @Query("SELECT count() from items")
    fun count(): LiveData<Int>

    @Insert
    fun insertTimeSlot(timeSlot: TimeSlot)

    @Update
    fun updateTimeSlot(timeSlot: TimeSlot)

    @Query("SELECT * from items")
    fun findAll(): LiveData<List<TimeSlot>>

    @Query("SELECT * from items WHERE id = :id")
    fun getTimeSlot(id: Int): LiveData<TimeSlot>

}