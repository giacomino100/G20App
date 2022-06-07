package it.polito.g20app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private var idSelected: String = " "
    private var idTimeslotSkill: String = " "
    private var idVendor: String = " "
    private var tsTitle: String = " "
    private val viewModelT by viewModels<TimeSlotVM>()
    private val viewModelS by viewModels<SkillVM>()
    private var auth: FirebaseAuth = Firebase.auth

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_time_slot_details, container, false)

        //getting the timeSlot id from bundle
        arguments.let {
            idSelected = it!!.getString("id").toString()
            val taken = it.getBoolean("taken")
            if (taken)
                root.findViewById<Button>(R.id.chat_button).visibility = View.INVISIBLE
        }

        //Loading timeSlot
         viewModelT.timeSlots.observe(viewLifecycleOwner) {
             val ts = it.filter { t -> t.id == idSelected }[0]

             idVendor = ts.idUser
             root.findViewById<TextView>(R.id.slot_title).text = ts.title
             tsTitle = ts.title
             root.findViewById<TextView>(R.id.slot_description).text = ts.description
             root.findViewById<TextView>(R.id.slot_date_and_time).text = ts.date
             root.findViewById<TextView>(R.id.slot_duration).text = ts.duration
             root.findViewById<TextView>(R.id.slot_location).text = ts.location
             idTimeslotSkill = ts.idSkill
         }

        //Loading skills
        viewModelS.skills.observe(viewLifecycleOwner) {
            root.findViewById<TextView>(R.id.slot_skill).text = it.filter { s -> s.id == idTimeslotSkill }[0].name
        }

        //setting chat button for client
        root.findViewById<Button>(R.id.chat_button)?.setOnClickListener{
            //Setting bundle
            val bundle = Bundle()
            bundle.putString("idTimeSlot", idSelected)
            idVendor = viewModelT.timeSlots.value!!.filter { t -> t.id == idSelected }[0].idUser
            bundle.putString("idVendor", idVendor)
            bundle.putString("tsTitle", tsTitle)

            if (arguments?.get("fromSkillDet") == 1) {
                //if the navigation path is: nav_skills_list -> nav_skill_details, the app goes to the nav_chat_fragment
                bundle.putInt("fromSkillDet", 1)
                viewModelT.timeSlots.observe(viewLifecycleOwner) {
                    //Creating a new chat, the buyer is added to the timeslot 'userinterested' array on the db
                    if (it.none { ts -> ts.id == idSelected && !ts.taken }) {
                        val timeSlotToUpdate = viewModelT.timeSlots.value?.filter { it.id == idSelected }!![0]
                        val usersInterested = timeSlotToUpdate.userInterested as MutableList<String>
                        usersInterested.add(auth.uid.toString())
                        val updatedTimeSlot = TimeSlot(
                            timeSlotToUpdate.id,
                            timeSlotToUpdate.idUser,
                            timeSlotToUpdate.idSkill,
                            timeSlotToUpdate.title,
                            timeSlotToUpdate.description,
                            timeSlotToUpdate.location,
                            timeSlotToUpdate.duration,
                            timeSlotToUpdate.date,
                            timeSlotToUpdate.taken,
                            usersInterested
                        )
                        viewModelT.updateTimeSlot(updatedTimeSlot)
                    }
                }
                findNavController().navigate(R.id.action_nav_slot_details_to_chat_fragment, bundle)
            } else {
                //the navigation path is: menu'-> nav_adv_list, the app goes to nav_timeslot_chats_fragment
                bundle.putInt("fromSkillDet", 0)
                findNavController().navigate(R.id.action_nav_slot_details_to_nav_timeslot_chats_fragment, bundle)
            }
        }

        //setting like button
        root.findViewById<Button>(R.id.like_button).let {
            if (it != null) {
                 viewModelT.timeSlots.observe(viewLifecycleOwner){ ts ->
                     //if the timeslot idUser match the logged one, he cannot add the timeslot to his favourites
                     if (!ts.none { t -> t.id == idSelected && t.idUser == auth.uid }) it.isEnabled = false

                     if (ts.filter { t -> t.id == idSelected }[0].userInterested.contains(auth.uid))
                         it.text = "Remove from favorites"
                     else
                         it.text = "Add to your favorites"
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
                if (userInterested != null) newUserInterested = userInterested.filter { it != auth.uid } as MutableList<String>
            } else {
                if (userInterested != null) newUserInterested.addAll(userInterested)
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
            if (updatedTimeSlot != null) viewModelT.updateTimeSlot(updatedTimeSlot)
        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}