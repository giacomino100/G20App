package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class Profile(
    var id: String = " ",
    var fullname: String = " ",
    var nickname: String = " ",
    var location: String = " ",
    var email: String = " "
)


class ProfileVM: ViewModel() {
    private val _profiles = MutableLiveData<List<Profile>>()
    val profiles : LiveData<List<Profile>> = _profiles

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("profiles")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _profiles.value = v!!.mapNotNull { d -> d.toProfile() }
                    Log.d("initProfiles", _profiles.value.toString())
                } else _profiles.value = emptyList()
            }
    }

    fun DocumentSnapshot.toProfile(): Profile? {
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