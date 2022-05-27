package it.polito.g20app

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

data class TimeSlot(
    var id: String = " ",
    var idUser: String = " ",
    var idSkill: String = " ",
    var title: String = " ",
    var description: String = " ",
    var location: String = " ",
    var duration: String = " ",
    var date: String = " "
)

class TimeSlotVM: ViewModel(){

    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots : LiveData<List<TimeSlot>> = _timeSlots

    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    init {
        l1 = db.collection("timeslots")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _timeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
                } else _timeSlots.value = emptyList()
            }
    }


    fun addTimeSlot(timeSlot: TimeSlot){
        val newTimeSlot = hashMapOf(
            "idUser" to timeSlot.idUser,
            "idSkill" to timeSlot.idSkill,
            "title" to timeSlot.title,
            "description" to timeSlot.description,
            "location" to timeSlot.location,
            "duration" to timeSlot.duration,
            "date" to timeSlot.date
        )
        db.collection("timeslots").document().set(newTimeSlot)
    }

    fun updateTimeSlot(timeSlot: TimeSlot){
        val updatedTimeSlot = hashMapOf(
            "idUser" to timeSlot.idUser,
            "idSkill" to timeSlot.idSkill,
            "title" to timeSlot.title,
            "description" to timeSlot.description,
            "location" to timeSlot.location,
            "duration" to timeSlot.duration,
            "date" to timeSlot.date
        )
        db.collection("timeslots").document(timeSlot.id).set(updatedTimeSlot)
    }

    private fun DocumentSnapshot.toTimeSlot(): TimeSlot? {
        return try {
            val id = id
            val idUser = get("idUser") as String
            val idSkill = get("idSkill") as String
            val title = get("title") as String
            val description = get("description") as String
            val date = get("date") as String
            val location = get("location") as String
            val duration = get("duration") as String
            TimeSlot(id, idUser, idSkill, title, description, location, duration, date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
