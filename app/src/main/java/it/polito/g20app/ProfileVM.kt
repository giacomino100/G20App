package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

data class Profile(
    var id: String = " ",
    var fullname: String = " ",
    var nickname: String = " ",
    var location: String = " ",
    var email: String = " ",
    var credit: String = " "
)


class ProfileVM: ViewModel() {
    private val _profile = MutableLiveData<List<Profile>>()
    val profile : LiveData<List<Profile>> = _profile


    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("profiles")
            .addSnapshotListener { v, e ->
                Log.d("profili", v.toString())
                if (e==null) {
                    _profile.value = v!!.mapNotNull { p -> p.toProfile() }
                } else _profile.value = emptyList()
            }
    }

    private fun DocumentSnapshot.toProfile(): Profile? {
        return try {
            val id = id
            val fullname = get("fullname") as String
            val nickname = get("nickname") as String
            val email = get("email") as String
            val location = get("location") as String
            val credit = get("credit") as String
            Profile(id, fullname, nickname, location, email, credit)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


