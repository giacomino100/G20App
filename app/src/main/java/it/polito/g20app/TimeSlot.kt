 package it.polito.g20app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalTime.*


 class TimeSlotAdapter(val data: MutableList<TimeSlot>, isSkillDetails: Boolean, isTimeSlotSaved: Boolean, isTimeSlotAssigned: Boolean): RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    private var displayData = data.toMutableList()
    private var flag = isSkillDetails
    private var _isTimeSlotSaved = isTimeSlotSaved
    private var _isTimeSlotAssigned = isTimeSlotAssigned

    class TimeSlotViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.slot_title)
        private val edit: ImageView = v.findViewById(R.id.editTimeSlot)
        private val card: CardView = v.findViewById(R.id.card)


        fun convertMonthFromNameToNumber(month: String): Any {
            val result = when(month){
                "Jan" -> 1
                "Feb" -> 2
                "Mar" -> 3
                "Apr" -> 4
                "May" -> 5
                "Jun" -> 6
                "Jul" -> 7
                "Aug" -> 8
                "Set" -> 9
                "Oct" -> 10
                "Nov" -> 11
                "Dec" -> 12
                else -> { }
            }
            return result
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(timeslot: TimeSlot, flag: Boolean, action: (v: View)->Unit) {
            title.text = timeslot.title
            card.setOnClickListener(action)

            if (flag) edit.visibility = View.GONE

            if (timeslot.taken){
                //THIS IF IS NEEDED TO MANAGE THE RATING OF THE USER
                //Se sono passate le ore del time slot possiamo votarlo
                val dayTS = timeslot.date.split(" ")[2]
                val monthTS = timeslot.date.split(" ")[1]
                val yearTS = timeslot.date.split(" ")[5]
                val hourTS = timeslot.date.split(" ")[3].split(":")[0]

                val dayNow = LocalDate.now().toString().split("-")[2]
                val monthNow = LocalDate.now().toString().split("-")[1]
                val yearNow = LocalDate.now().toString().split("-")[0]
                val hourNow = now().toString().split(":")[0]

                Log.d("review", LocalDateTime.now().toString())

                if((yearNow >= yearTS && monthNow >= monthTS && dayNow > dayTS) ||  (yearNow >= yearTS && monthNow >= monthTS && dayNow >= dayTS && hourNow >= (timeslot.duration + hourTS))){
                    //true se il giorno è maggiore, oppure se è uguale ma l'ora è maggiore
                    edit.visibility = View.VISIBLE
                    edit.setImageResource(R.drawable.ic_baseline_rate_review_24)
                    edit.setOnClickListener {
                        it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_rating_fragment)
                    }
                }
            }else{
                edit.setOnClickListener {
                    //Clicking the edit button
                    val bundle = Bundle()
                    bundle.putString("id", timeslot.id)
                    bundle.putString("idUser", timeslot.idUser)
                    bundle.putString("title", timeslot.title)
                    bundle.putString("description", timeslot.description)
                    bundle.putString("dateAndTime", timeslot.date)
                    bundle.putString("duration", timeslot.duration)
                    bundle.putString("location", timeslot.location)
                    bundle.putBoolean("taken", timeslot.taken)
                    bundle.putString("idSkill", timeslot.idSkill)
                    it.findNavController().navigate(R.id.action_nav_adv_list_to_timeSlotEditFragment, bundle)
                }

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.time_slot, parent, false)
        return TimeSlotViewHolder(vg)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            bundle.putBoolean("taken", item.taken)
            bundle.putString("idSkill", item.idSkill)
            if (flag && !_isTimeSlotSaved && !_isTimeSlotAssigned) it.findNavController().navigate(R.id.action_nav_skill_details_to_nav_slot_details3, bundle)
            else it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_slot_details, bundle)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
