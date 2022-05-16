package it.polito.g20app

import android.app.Application
import androidx.lifecycle.LiveData

class TimeSlotRepo(application: Application) {
    private val TimeSlotDAO = TimeSlotDB.getDatabase(application).TimeSlotDAO()

    fun count(): LiveData<Int> = TimeSlotDAO.count()

    fun add(timeSlot: TimeSlot) {
        val i = TimeSlot().also {
            it.title = timeSlot.title
            it.description = timeSlot.description
            it.dateAndTime = timeSlot.dateAndTime
            it.duration = timeSlot.duration
            it.location = timeSlot.location
        }
        TimeSlotDAO.insertTimeSlot(i)
    }

    fun update(timeSlot: TimeSlot) {
        val i = TimeSlot().also {
            it.id = timeSlot.id
            it.title = timeSlot.title
            it.description = timeSlot.description
            it.dateAndTime = timeSlot.dateAndTime
            it.duration = timeSlot.duration
            it.location = timeSlot.location
        }
        TimeSlotDAO.updateTimeSlot(i)
    }

    fun timeSlots(): LiveData<List<TimeSlot>> = TimeSlotDAO.findAll()
    fun getTimeSlot(id: Int): LiveData<TimeSlot> = TimeSlotDAO.getTimeSlot(id)

}