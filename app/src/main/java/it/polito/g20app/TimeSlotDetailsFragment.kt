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

class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private var idSelected: String = " "
    private var idTimeslotSkill: String = " "
    private var receiver: String = " " //receiver of chat
    private val viewModelT by viewModels<TimeSlotVM>()
    private val viewModelS by viewModels<SkillVM>()

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
             receiver = ts.idUser
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
            Log.d("DebugChat", viewModelT.timeSlots.value.toString())
            receiver = viewModelT.timeSlots.value!!.filter { t -> t.id == idSelected }[0].idUser
            bundle.putString("receiver", receiver)

            //questo bundle è necessario per recuperare dal db i messaggi relativi ad un time slot e ad uno user
            findNavController().navigate(R.id.action_nav_slot_details_to_chat_fragment, bundle)

            //TODO: Aggiungere condizione per mostrare chat dal punto di vista del proprietario
            //findNavController().navigate(R.id.action_nav_slot_details_to_recycler view, bundle)


        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}