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

data class Skill(
    var id: String = " ",
    var idUser: String = " ",
    var name: String = " ",
    var description: String = " "
)

class SkillVM: ViewModel(){
    private val _skills = MutableLiveData<List<Skill>>()
    val skills : LiveData<List<Skill>> = _skills

    private var _userSkills = MutableLiveData<List<Skill>>()
    val userSkills : LiveData<List<Skill>> = _userSkills

    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration
    private var l2: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("skills")
            .whereNotEqualTo("idUser", auth.uid.toString())
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _skills.value = v!!.mapNotNull { d -> d.toSkill() }
                } else _skills.value = emptyList()
            }
        l2 = db.collection("skills")
            .whereEqualTo("idUser", auth.uid.toString())
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _userSkills.value = v!!.mapNotNull { d -> d.toSkill() }
                }
            }
    }

    fun DocumentSnapshot.toSkill(): Skill? {
        return try {
            val id = id
            val idUser = get("idUser") as String
            val name = get("name") as String
            val description = get("description") as String
            Skill(id, idUser, name, description)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}