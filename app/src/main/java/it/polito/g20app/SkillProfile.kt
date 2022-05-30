package it.polito.g20app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SkillProfileAdapter(val data: MutableList<Skill>, isEditProfile: Boolean): RecyclerView.Adapter<SkillProfileAdapter.SkillProfileViewHolder>() {
    private var displayData = data.toMutableList()
    private val flag = isEditProfile

    class SkillProfileViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var id: String = " "
        private val title: TextView = v.findViewById(R.id.skill_title_profile)
        private val desc: TextView = v.findViewById(R.id.skill_desc_profile)
        private val delete: ImageButton = v.findViewById(R.id.deleteButton)
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        private var auth: FirebaseAuth = Firebase.auth


        fun bind(skill: Skill, flag: Boolean) {
            id = skill.id
            title.text = skill.name
            desc.text = skill.description
            if (flag) delete.visibility = View.VISIBLE

            //Delete the relation Skill-Profile
            delete.setOnClickListener {
                db.collection("skillsProfile").get()
                    .addOnCompleteListener { task ->
                        Log.d("tobedeleted", task.result.filter { it.data["idSkill"] == skill.id && it.data["idUser"] == auth.uid}.map { it.id }.toString())
                        task.result.filter { it.data["idSkill"] == skill.id && it.data["idUser"] == auth.uid} [0].reference.delete()
                            .addOnSuccessListener {
                                Log.d("deleteSkill", "skill ${skill.id} deleted")
                            }
                    }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillProfileViewHolder {

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.skill_profile, parent, false)

        return SkillProfileViewHolder(vg)
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SkillProfileViewHolder, position: Int) {
        val item = displayData[position]
        val flag2 = flag
        holder.bind(item, flag = flag2)
    }


}