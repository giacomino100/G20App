package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private var idSelected: String = " "
    private var idTimeslotSkill: String = " "

    val viewModelT by viewModels<TimeSlotVM>()
    val viewModelS by viewModels<SkillVM>()

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

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}