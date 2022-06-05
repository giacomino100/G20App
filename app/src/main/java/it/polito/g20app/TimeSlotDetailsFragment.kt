package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private var idSelected: String = " "
    private var idTimeslotSkill: String = " "
    private var idVendor: String = " " //receiver of chat
    private val viewModelT by viewModels<TimeSlotVM>()
    private val viewModelS by viewModels<SkillVM>()
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_time_slot_details, container, false)

        //recupero id del time slot dal Bundle
        arguments.let { idSelected = it!!.getString("id").toString() }

        //Loading time slot dal db
         viewModelT.timeSlots.observe(viewLifecycleOwner) {
             val ts = it.filter { t -> t.id == idSelected }[0]
             idVendor = ts.idUser
             root.findViewById<TextView>(R.id.slot_title).text = ts.title
             root.findViewById<TextView>(R.id.slot_description).text = ts.description
             root.findViewById<TextView>(R.id.slot_date_and_time).text = ts.date
             root.findViewById<TextView>(R.id.slot_duration).text = ts.duration
             root.findViewById<TextView>(R.id.slot_location).text = ts.location
             idTimeslotSkill = ts.idSkill
         }

        //Recupero skill dal db
        viewModelS.skills.observe(viewLifecycleOwner) {
            root.findViewById<TextView>(R.id.slot_skill).text = it.filter { s -> s.id == idTimeslotSkill }[0].name
        }

        //setting chat button for client
        root.findViewById<Button>(R.id.chat_button)?.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("idTimeSlot", idSelected)
            idVendor = viewModelT.timeSlots.value!!.filter { t -> t.id == idSelected }[0].idUser
            bundle.putString("idVendor", idVendor)
            if (arguments?.get("fromSkillDet") == 1) {
                Log.d("fromSkillDet?", "yes")
                //if the navigation path is: nav_skills_list -> nav_skill_details, the app goes to the nav_chat_fragment
                findNavController().navigate(R.id.action_nav_slot_details_to_chat_fragment, bundle)
            } else {
                Log.d("fromSkillDet?", "no")
                //the navigation path is: menù -> nav_adv_list, the app goes to nav_timeslot_chats_fragment
                findNavController().navigate(R.id.action_nav_slot_details_to_nav_timeslot_chats_fragment)
            }
        }

        //setting like button
        root.findViewById<Button>(R.id.like_button).let {
            if (it != null) {
                 viewModelT.timeSlots.observe(viewLifecycleOwner){ it1 ->
                     if(it1.filter{it.id == idSelected}.map{it.idUser}.get(0) == auth.uid){
                         it.isEnabled = false
                     }
                         val checkUserInterested =
                             it1.filter { it.id == idSelected }.map { it.userInterested }
                         if (checkUserInterested.size > 0) {
                             val userInterested = checkUserInterested.get(0)
                             if (userInterested.contains(auth.uid)) {
                                 it.text = "Remove from favorites"
                             } else {
                                 it.text = "Add to your favorites"
                             }
                         }

                }
            }
        }

        root.findViewById<Button>(R.id.like_button).setOnClickListener {
            val timeSlotToUpdate = viewModelT.timeSlots.value?.filter { it.id == idSelected }?.get(0)
            var liked = false
            val userInterested = viewModelT.timeSlots.value?.filter{it.id == idSelected}?.map { it.userInterested }?.get(0)

            if (userInterested != null) {
                liked = userInterested.contains(auth.uid)
            }

            var newUserInterested = mutableListOf<String>()

            if(liked){
                if (userInterested != null) {
                    newUserInterested = userInterested.filter { it != auth.uid } as MutableList<String>
                }
            } else {
                if (userInterested != null) {
                    newUserInterested.addAll(userInterested)
                }
                newUserInterested.add(auth.uid.toString())
            }


            val updatedTimeSlot = timeSlotToUpdate?.let { it1 ->
                TimeSlot(
                    it1.id,
                    timeSlotToUpdate.idUser,
                    timeSlotToUpdate.idSkill,
                    timeSlotToUpdate.title,
                    timeSlotToUpdate.description,
                    timeSlotToUpdate.location,
                    timeSlotToUpdate.duration,
                    timeSlotToUpdate.date,
                    timeSlotToUpdate.taken,
                    newUserInterested
                )
            }
            if (updatedTimeSlot != null) {
                viewModelT.updateTimeSlot(updatedTimeSlot)
            }
        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}