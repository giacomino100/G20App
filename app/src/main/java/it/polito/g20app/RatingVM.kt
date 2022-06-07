package it.polito.g20app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
    var comment: String = " "
)

class RatingVM: ViewModel() {
    private val _ratings = MutableLiveData<Rating>()
    val ratings : LiveData<Rating> = _ratings

    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("ratings")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _ratings.value = v!!.filter { p -> p.id == auth.uid }[0].toRating()
                } else _ratings.value = emptyList<Rating>()[0]
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
            Rating(id, idVendor, idBuyer, idTimeSlot, rate, comment)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}