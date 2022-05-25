package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.sql.Time
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


class SkillDetailsFragment : Fragment() {

    val vm by viewModels<TimeSlotVM>()

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

        var idSkill = " "
        arguments.let {
            idSkill = it?.getString("id").toString()
        }

        vm.timeSlots.observe(viewLifecycleOwner){
            var flag: Boolean

            if (it.filter { t -> t.idSkill == idSkill }.isNullOrEmpty()){
                //se la lista di timeslots è vuota, non visualizzo gli switch
                switchSort.visibility = View.GONE
                switchFilter.visibility = View.GONE
                Snackbar.make(root, "There are no TimeSlots for the selected skill", Snackbar.LENGTH_LONG).show()
            }
            else {
                switchSort.visibility = View.VISIBLE
                switchFilter.visibility = View.VISIBLE
                flag = true
                val adapter = TimeSlotAdapter(it.filter { t -> t.idSkill == idSkill } as MutableList<TimeSlot>, flag)
                rv.adapter = adapter
            }
        }


        switchSort?.setOnCheckedChangeListener { _, isChecked ->
            vm.timeSlots.observe(viewLifecycleOwner) { it ->
                val sortedSlots = if (isChecked) it.filter { it.idSkill == idSkill }.sortedBy { it.title }
                               else it.filter { it.idSkill == idSkill }

                sortedSlots.let {
                    val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>, true)
                    rv.adapter = adapter
                }
            }
        }

        switchFilter?.setOnCheckedChangeListener { _, isChecked ->
            vm.timeSlots.observe(viewLifecycleOwner) { it ->
                val filteredSlots = if (isChecked) it.filter { it.idSkill == idSkill }.filter { it ->
                    var time = it.date.split(" ")
                    var params = time[3].split(":")
                    if (params[0].toInt() == 12) {
                        if (params[1].toInt() == 0) {
                            params[2].toInt() == 0
                        } else params[1].toInt() < 60
                    } else params[0].toInt() < 12
                } else it.filter { it.idSkill == idSkill }

                filteredSlots.let {
                    val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>, true)
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