package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class TimeSlot(
    var id: String = " ",
    var idUser: String = " ",
    var idSkill: String = " ",
    var title: String = " ",
    var description: String = " ",
    var location: String = " ",
    var duration: String = " ",
    var date: String = " ",
    var taken: Boolean = false,
    var userInterested: List<String> = listOf(),
    var buyer: String = " ",
    var credits: String = " ",
    var reviewedByVendor: Boolean = false,
    var reviewedByBuyer: Boolean = false
)

@Suppress("UNCHECKED_CAST")
class TimeSlotVM: ViewModel(){

    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots : LiveData<List<TimeSlot>> = _timeSlots

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
            "date" to timeSlot.date,
            "taken" to timeSlot.taken,
            "userInterested" to timeSlot.userInterested,
            "buyer" to timeSlot.buyer,
            "credits" to timeSlot.credits,
            "reviewedByVendor" to timeSlot.reviewedByVendor,
            "reviewedByBuyer" to timeSlot.reviewedByBuyer
        )
        db.collection("timeslots").document().set(newTimeSlot).addOnSuccessListener {
            Log.d("database", "New entry successfully added in timeslots collection")
        }.addOnFailureListener {
            Log.d("database", "Error saving a new entry in timeslots collection")
        }
    }

    fun updateTimeSlot(timeSlot: TimeSlot){
        val updatedTimeSlot = hashMapOf(
            "idUser" to timeSlot.idUser,
            "idSkill" to timeSlot.idSkill,
            "title" to timeSlot.title,
            "description" to timeSlot.description,
            "location" to timeSlot.location,
            "duration" to timeSlot.duration,
            "date" to timeSlot.date,
            "taken" to timeSlot.taken,
            "userInterested" to timeSlot.userInterested,
            "buyer" to timeSlot.buyer,
            "credits" to timeSlot.credits,
            "reviewedByVendor" to timeSlot.reviewedByVendor,
            "reviewedByBuyer" to timeSlot.reviewedByBuyer
        )
        Log.d("updatingTimeslot", "aggiornamento utenti interessati: ${timeSlot.userInterested}")
        db.collection("timeslots").document(timeSlot.id).set(updatedTimeSlot).addOnSuccessListener {
            Log.d("database", "Timeslots successfully updated")
        }.addOnFailureListener {
            Log.d("database", "Error updating the selected timeslot in the timeslots collection")
        }
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
            val taken = get("taken") as Boolean
            val userInterested = get("userInterested") as List<String>
            val buyer = get("buyer") as String
            val credits = get("credits") as String
            val reviewedByVendor = get("reviewedByVendor") as Boolean
            val reviewedByBuyer = get("reviewedByBuyer") as Boolean
            TimeSlot(id, idUser, idSkill, title, description, location, duration, date, taken, userInterested, buyer, credits, reviewedByVendor, reviewedByBuyer)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
