package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class TimeSlotAdapter(val data: MutableList<TimeSlot>): RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    var displayData = data.toMutableList()

    class TimeSlotViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.slot_title)
        private val edit: ImageView = v.findViewById(R.id.edit)
        private val card: CardView = v.findViewById(R.id.card)


        fun bind(timeslot: TimeSlot, action: (v: View)->Unit) {
            title.text = timeslot.title
            card.setOnClickListener(action)

            edit.setOnClickListener {
                //Cliccando il tasto edit nella Lista
                val bundle = Bundle()
                bundle.putString("id", timeslot.id.toString())
                bundle.putString("title", timeslot.title)
                bundle.putString("description", timeslot.description)
                bundle.putString("dateAndTime", timeslot.date)
                bundle.putString("duration", timeslot.duration)
                bundle.putString("location", timeslot.location)
                it.findNavController().navigate(R.id.action_nav_adv_list_to_timeSlotEditFragment, bundle)
            }
        }

        fun unbind() {
            card.setOnClickListener(null)
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

        holder.bind(item) {
            //cliccando sull'edit si apre il TimeSlotDetailsFragment
            //passaggio di informazioni tra fragment with a Bundle
            val bundle = Bundle()
            bundle.putString("id", item.id)
            it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_slot_details, bundle)
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
