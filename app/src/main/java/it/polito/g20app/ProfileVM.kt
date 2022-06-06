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

data class Profile(
    var id: String = " ",
    var fullname: String = " ",
    var nickname: String = " ",
    var location: String = " ",
    var email: String = " "
)


class ProfileVM: ViewModel() {
    private val _profile = MutableLiveData<Profile>()
    val profile : LiveData<Profile> = _profile

    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("profiles")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _profile.value = v!!.filter { p -> p.id == auth.uid }[0].toProfile()
                } else _profile.value = emptyList<Profile>()[0]
            }
    }

    private fun DocumentSnapshot.toProfile(): Profile? {
        return try {
            val id = id
            val fullname = get("fullname") as String
            val nickname = get("nickname") as String
            val email = get("email") as String
            val location = get("location") as String
            Profile(id, fullname, nickname, location, email)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}