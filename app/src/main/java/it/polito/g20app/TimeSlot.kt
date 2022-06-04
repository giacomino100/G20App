package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView


class TimeSlotAdapter(val data: MutableList<TimeSlot>, isSkillDetails: Boolean, isTimeSlotSaved: Boolean): RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    private var displayData = data.toMutableList()
    private var flag = isSkillDetails
    private var _isTimeSlotSaved = isTimeSlotSaved

    class TimeSlotViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.slot_title)
        private val edit: ImageView = v.findViewById(R.id.editTimeSlot)
        private val card: CardView = v.findViewById(R.id.card)

        fun bind(timeslot: TimeSlot, flag: Boolean, action: (v: View)->Unit) {
            title.text = timeslot.title
            card.setOnClickListener(action)

            if (flag) edit.visibility = View.GONE

            edit.setOnClickListener {
                //Cliccando il tasto edit nella Lista
                val bundle = Bundle()
                bundle.putString("id", timeslot.id)
                bundle.putString("idUser", timeslot.idUser)
                bundle.putString("title", timeslot.title)
                bundle.putString("description", timeslot.description)
                bundle.putString("dateAndTime", timeslot.date)
                bundle.putString("duration", timeslot.duration)
                bundle.putString("location", timeslot.location)
                bundle.putString("idSkill", timeslot.idSkill)
                it.findNavController().navigate(R.id.action_nav_adv_list_to_timeSlotEditFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.time_slot, parent, false)
        return TimeSlotViewHolder(vg)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val item = displayData[position]
        val flag2 = flag

        holder.bind(timeslot =  item, flag = flag2) {
            val bundle = Bundle()
            if (flag) bundle.putInt("fromSkillDet", 1)
            else bundle.putInt("fromSkillDet", 0)
            bundle.putString("id", item.id)
            bundle.putString("idUser", item.idUser)
            bundle.putString("title", item.title)
            bundle.putString("description", item.description)
            bundle.putString("dateAndTime", item.date)
            bundle.putString("duration", item.duration)
            bundle.putString("location", item.location)
            bundle.putString("idSkill", item.idSkill)
            if (flag && !_isTimeSlotSaved) it.findNavController().navigate(R.id.action_nav_skill_details_to_nav_slot_details3, bundle)
            else it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_slot_details, bundle)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
