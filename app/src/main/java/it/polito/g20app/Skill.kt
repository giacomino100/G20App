package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class SkillAdapter(val data: MutableList<Skill>): RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {
    var displayData = data.toMutableList()

    class SkillViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var id: String = " "
        private val title: TextView = v.findViewById(R.id.skill_title)
        private val card: CardView = v.findViewById(R.id.card_skill)


        fun bind(skill: Skill, action: (v: View)->Unit) {
            id = skill.id
            title.text = skill.name
            card.setOnClickListener(action)
        }

        fun unbind() {
            card.setOnClickListener(null)
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
            //cliccando sull'edit si apre il TimeSlotDetailsFragment
            //passaggio di informazioni tra fragment with a Bundle
            val bundle = Bundle()
            bundle.putString("id",item.id)
            bundle.putString("name", item.name)
            bundle.putString("description", item.description)

            //TODO: passare dati ad un fragment che mostri i dettagli di una skill
            it.findNavController().navigate(R.id.action_nav_skills_list_to_nav_adv_list, bundle)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun submitList(it: List<TimeSlot>?) {
        if (it != null) {
            for (i in 0..it.size-1){ Log.d("submitList", it[i].toString()) }
        }
    }


}