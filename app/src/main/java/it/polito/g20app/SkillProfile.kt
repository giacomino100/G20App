package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class SkillProfileAdapter(val data: MutableList<Skill>): RecyclerView.Adapter<SkillProfileAdapter.SkillProfileViewHolder>() {
    var displayData = data.toMutableList()

    class SkillProfileViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var id: String = " "
        private val title: TextView = v.findViewById(R.id.skill_title_profile)
        private val desc: TextView = v.findViewById(R.id.skill_desc_profile)
        private val card1: CardView = v.findViewById(R.id.card1)
        private val card2: CardView = v.findViewById(R.id.card2)

        fun bind(skill: Skill, action: (v: View)->Unit) {
            id = skill.id
            title.text = skill.name
            desc.text = skill.description
            card1.setOnClickListener{
                Log.d("card","cliccato1")
            }
            card2.setOnClickListener{
                Log.d("card","cliccato2")
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
        holder.bind(item) {
            //cliccando sull'edit si apre il TimeSlotDetailsFragment
            //passaggio di informazioni tra fragment with a Bundle
            val bundle = Bundle()
            bundle.putString("id", item.id) //idSkill
            bundle.putString("name", item.name)
            bundle.putString("description", item.description)
            it.findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, bundle)
        }    }


}