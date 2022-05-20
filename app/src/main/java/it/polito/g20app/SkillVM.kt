package it.polito.g20app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SkillVM: ViewModel(){
    private val _courses = MutableLiveData<List<Skill>>()
    val courses : LiveData<List<Skill>> = _courses

    private var l: ListenerRegistration
    private val db: FirebaseFirestore

    init {
        db = FirebaseFirestore.getInstance()
        l = db.collection("skills")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _courses.value = v!!.mapNotNull { d -> d.toSkill() }
                } else _courses.value = emptyList()
            }
    }

    data class Skill(
        var id: String = " ",
        var name: String = " ",
        var description: String = " "
    )

    fun DocumentSnapshot.toSkill(): Skill? {
        return try {
            val id = ""
            val name = get("Title") as String
            val description = get("Description") as String
            Skill(id, name, description)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}