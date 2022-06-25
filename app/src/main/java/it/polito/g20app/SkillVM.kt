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
    var name: String = " ",
    var description: String = " "
)

data class SkillProfile(
    var id: String = " ",
    var idSkill: String = " ",
    var idUser: String = " "
)

class SkillVM: ViewModel(){
    private val _skills = MutableLiveData<List<Skill>>()
    private val _skillsProfile = MutableLiveData<List<SkillProfile>>()
    val skills : LiveData<List<Skill>> = _skills
    val skillsProfile: LiveData<List<SkillProfile>> = _skillsProfile
    private var auth: FirebaseAuth = Firebase.auth

    private var l1: ListenerRegistration
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        l1 = db.collection("skills")
            .addSnapshotListener { v, e ->
                Log.d("profili skill", v.toString())
                if (e==null) {
                    _skills.value = v!!.mapNotNull { d -> Log.d("profili", d.toString()); d.toSkill() }
                } else _skills.value = emptyList()
            }.also { db.collection("skillsProfile")
                .addSnapshotListener { v, e ->
                    if (e==null) {
                        _skillsProfile.value = v!!.filter { d -> d.data["idUser"] == auth.uid }.mapNotNull { d -> d.toSkillProfile() }
                    } else _skills.value = emptyList()
                }
            }
    }

    private fun DocumentSnapshot.toSkill(): Skill? {
        return try {
            val id = id
            val name = get("name") as String
            val description = get("description") as String
            Skill(id, name, description)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun DocumentSnapshot.toSkillProfile(): SkillProfile? {
        return try {
            val id = id
            val idSkill = get("idSkill") as String
            val idUser = get("idUser") as String
            SkillProfile(id, idSkill, idUser)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addSkillProfile(skillProfile: SkillProfile){
        val newSkillProfile = hashMapOf(
            "idUser" to skillProfile.idUser,
            "idSkill" to skillProfile.idSkill
        )
        db.collection("skillsProfile").document().set(newSkillProfile).addOnSuccessListener {
            Log.d("database", "New entry successfully added in skillProfiles collection")
        }.addOnFailureListener {
            Log.d("database", "Error saving a new entry in skillProfiles collection")
        }
    }
}