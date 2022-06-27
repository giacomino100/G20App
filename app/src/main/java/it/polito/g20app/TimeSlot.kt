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
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList


 class TimeSlotAdapter(val data: MutableList<TimeSlot>, isSkillDetails: Boolean, isTimeSlotSaved: Boolean, isTimeSlotAssigned: Boolean): RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    private var displayData = data.toMutableList()
    private var flag = isSkillDetails
    private var _isTimeSlotSaved = isTimeSlotSaved
    private var _isTimeSlotAssigned = isTimeSlotAssigned

    class TimeSlotViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.slot_title)
        private val edit: ImageView = v.findViewById(R.id.editTimeSlot)
        private val card: CardView = v.findViewById(R.id.card)
        private var auth: FirebaseAuth = Firebase.auth
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        private fun convertMonthFromNameToNumber(month: String): String {
            val result = when(month){
                "Jan" -> "01"
                "Feb" -> "02"
                "Mar" -> "03"
                "Apr" -> "04"
                "May" -> "05"
                "Jun" -> "06"
                "Jul" -> "07"
                "Aug" -> "08"
                "Set" -> "09"
                "Oct" -> "10"
                "Nov" -> "11"
                "Dec" -> "12"
                else -> " "
            }
            return result
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(timeslot: TimeSlot, flag: Boolean, action: (v: View)->Unit) {


            title.text = timeslot.title
            card.setOnClickListener(action)

            if (flag) edit.visibility = View.GONE

            if (timeslot.taken){
                //Needed for user rating: once the timeslot is expired (according to the timeslot duration),
                //the buyer can rate the vendor
                val dayTS = timeslot.date.split(" ")[2]
                val monthTS = timeslot.date.split(" ")[1]
                val yearTS = timeslot.date.split(" ")[5]
                val hourTS = timeslot.date.split(" ")[3].split(":")[0]
                val minTS = timeslot.date.split(" ")[3].split(":")[1]
                val secondsTS = timeslot.date.split(" ")[3].split(":")[2]

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val dataFormatted = yearTS + "-" + convertMonthFromNameToNumber(monthTS) + "-" + dayTS + " ${hourTS}:${minTS}:${secondsTS}.000"


                if(LocalDateTime.parse(dataFormatted, formatter).plusHours(2).isBefore(LocalDateTime.now())){
                    //true -> timeslot expired
                    if (timeslot.reviewedByVendor && timeslot.reviewedByBuyer) edit.visibility = View.GONE
                    else {
                        if (timeslot.idUser != auth.uid) {
                            if (!timeslot.reviewedByBuyer) edit.visibility = View.VISIBLE
                            else edit.visibility = View.GONE
                        } else {
                            if (!timeslot.reviewedByVendor) edit.visibility = View.VISIBLE
                            else edit.visibility = View.GONE
                        }
                        edit.setImageResource(R.drawable.ic_baseline_rate_review_24)
                        edit.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("idTimeSlot", timeslot.id)
                            bundle.putString("idVendor", timeslot.idUser)
                            bundle.putString("idBuyer", timeslot.buyer)

                            val updatedTimeSlot = hashMapOf(
                                "idUser" to timeslot.idUser,
                                "idSkill" to timeslot.idSkill,
                                "title" to timeslot.title,
                                "description" to timeslot.description,
                                "location" to timeslot.location,
                                "duration" to timeslot.duration,
                                "date" to timeslot.date,
                                "taken" to timeslot.taken,
                                "userInterested" to timeslot.userInterested,
                                "buyer" to timeslot.buyer,
                                "credits" to timeslot.credits,
                                "reviewedByVendor" to timeslot.reviewedByVendor,
                                "reviewedByBuyer" to timeslot.reviewedByBuyer
                            )

                            if (timeslot.idUser != auth.uid) {
                                //caso del buyer che fa la recensione al vendor
                                bundle.putString("idWriter", timeslot.buyer)
                                updatedTimeSlot["reviewedByBuyer"] = true
                            } else if (!timeslot.reviewedByVendor) {
                                //caso del vendor che fa la recensione al buyer
                                bundle.putString("idWriter", timeslot.idUser)
                                updatedTimeSlot["reviewedByVendor"] = true
                            }

                            db.collection("timeslots").document(timeslot.id).set(updatedTimeSlot).addOnSuccessListener {
                                Log.d("database", "Timeslots successfully updated")
                            }.addOnFailureListener {
                                Log.d("database", "Error updating the selected timeslot in the timeslots collection")
                            }
                            it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_rating_fragment, bundle)
                        }
                    }
                }
            } else{
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
                    bundle.putString("credits", timeslot.credits)
                    bundle.putBoolean("reviewedByVendor", timeslot.reviewedByVendor)
                    bundle.putBoolean("reviewedByBuyer", timeslot.reviewedByBuyer)
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
            bundle.putString("credits", item.credits)
            bundle.putBoolean("reviewedByVendor", item.reviewedByVendor)
            bundle.putBoolean("reviewedByBuyer", item.reviewedByBuyer)
            if (flag && !_isTimeSlotSaved && !_isTimeSlotAssigned) it.findNavController().navigate(R.id.action_nav_skill_details_to_nav_slot_details3, bundle)
            else it.findNavController().navigate(R.id.action_nav_adv_list_to_nav_slot_details, bundle)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
