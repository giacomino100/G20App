package it.polito.g20app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SkillDetailsFragment : Fragment() {

    private val vm by viewModels<TimeSlotVM>()
    private var auth: FirebaseAuth = Firebase.auth

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_skill_details, container, false)
        (activity as FirebaseActivity).supportActionBar?.setHomeButtonEnabled(true)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv = root.findViewById<RecyclerView>(R.id.rv_skill_details)
        rv.layoutManager = LinearLayoutManager(root.context)

        val switchSort = root.findViewById<Switch>(R.id.switchSort)
        val switchFilter = root.findViewById<Switch>(R.id.switchFilter)

        var idSkill: String
        arguments.let {
            idSkill = it?.getString("id").toString()
        }

        vm.timeSlots.observe(viewLifecycleOwner){
            val flag: Boolean

            if (it.none { t -> t.idSkill == idSkill && t.idUser != auth.uid}){
                //if the timeslots list is empty, hide the switches
                switchSort.visibility = View.GONE
                switchFilter.visibility = View.GONE
                Snackbar.make(root, "There are no TimeSlots for the selected skill", Snackbar.LENGTH_LONG).show()
            }
            else {
                switchSort.visibility = View.VISIBLE
                switchFilter.visibility = View.VISIBLE
                flag = true
                Log.d("timeslots",
                    it.filter { t -> t.idSkill == idSkill && t.idUser != auth.uid && !t.taken}.toString()
                )
                // !t.taken -> if the timeslot has not already been taken, it will be shown
                val adapter = TimeSlotAdapter(it.filter { t -> t.idSkill == idSkill && t.idUser != auth.uid && !t.taken} as MutableList<TimeSlot>, flag, false, false)
                rv.adapter = adapter
            }
        }


        switchSort?.setOnCheckedChangeListener { _, isChecked ->
            vm.timeSlots.observe(viewLifecycleOwner) { it ->
                val sortedSlots = if (isChecked) it.filter { it.idSkill == idSkill && it.idUser != auth.uid }.sortedBy { it.title }
                               else it.filter { it.idSkill == idSkill && it.idUser != auth.uid }

                sortedSlots.let {
                    val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>, true, false, false)
                    rv.adapter = adapter
                }
            }
        }

        switchFilter?.setOnCheckedChangeListener { _, isChecked ->
            vm.timeSlots.observe(viewLifecycleOwner) { it ->
                    val filteredSlots = if (isChecked) it.filter { it.idSkill == idSkill && it.idUser != auth.uid }.filter {
                        val time = it.date.split(" ")
                        val params = time[3].split(":")
                        if (params[0].toInt() == 12) {
                            if (params[1].toInt() == 0) {
                                params[2].toInt() == 0
                            } else params[1].toInt() < 60
                        } else params[0].toInt() < 12
                    } else it.filter { it.idSkill == idSkill && it.idUser != auth.uid}
                        filteredSlots.let {
                            val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>, true, false, false)
                            rv.adapter = adapter

                    }

                }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setting name and description of skill in this fragment
        arguments?.let{
            view.findViewById<TextView>(R.id.skill_name).text = it.get("name").toString()
            view.findViewById<TextView>(R.id.skill_description).text = it.get("description").toString()
        }

    }
}