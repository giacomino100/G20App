package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class SkillAdapter(val data: MutableList<Skill>): RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {
    private var displayData = data.toMutableList()

    class SkillViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var id: String = " "
        private val title: TextView = v.findViewById(R.id.skill_title)
        private val card: CardView = v.findViewById(R.id.card_skill)


        fun bind(skill: Skill, action: (v: View)->Unit) {
            id = skill.id
            title.text = skill.name
            card.setOnClickListener(action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.skill, parent, false)

        return SkillViewHolder(vg)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val item = displayData[position]
        holder.bind(item) {
            val bundle = Bundle()
            bundle.putString("id", item.id) //idSkill
            bundle.putString("name", item.name)
            bundle.putString("description", item.description)
            it.findNavController().navigate(R.id.action_nav_skills_list_to_nav_skill_details, bundle)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}