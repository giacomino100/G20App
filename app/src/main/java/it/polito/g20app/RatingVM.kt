package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

data class Rating (
    var id: String = " ",
    var idVendor: String = " ",
    var idBuyer: String = " ",
    var idTimeSlot: String = " ",
    var rate: String = " ",
    var comment: String = " ",
    var idWriter: String = " "
)

class RatingVM: ViewModel() {
    private val _ratings = MutableLiveData<List<Rating>>()
    val ratings : LiveData<List<Rating>> = _ratings

    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("ratings")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _ratings.value = v!!.mapNotNull { r -> r.toRating() }
                } else _ratings.value = emptyList()
            }
    }

    fun addRating(newRating: Rating) : Task<DocumentReference> {
        val newRating = hashMapOf(
            "idVendor" to newRating.idVendor,
            "idBuyer" to newRating.idBuyer,
            "idTimeSlot" to newRating.idTimeSlot,
            "rate" to newRating.rate,
            "comment" to newRating.comment,
            "idWriter" to newRating.idWriter
            )
        return db.collection("ratings").add(newRating).addOnSuccessListener {
            Log.d("database", "inserito")
        }.addOnFailureListener {
            Log.d("database", "non inserito correttamente")
        }
    }

    private fun DocumentSnapshot.toRating(): Rating? {
        return try {
            val id: String = " "
            val idVendor = get("idVendor") as String
            val idBuyer = get("idBuyer") as String
            val idTimeSlot = get("idTimeSlot") as String
            val rate = get("rate") as String
            val comment = get("comment") as String
            val idWriter = get("idWriter") as String
            Rating(id, idVendor, idBuyer, idTimeSlot, rate, comment, idWriter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}