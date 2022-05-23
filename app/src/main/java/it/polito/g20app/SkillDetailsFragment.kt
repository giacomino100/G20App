package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


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

        //defining ViewModel
        //TODO caricare dal db tutti i time slot di un idUser con un idSkill (Recuperabili dal bundle), guardare cosa arriva da SkillVM
        var idSkill = " "
        arguments.let {
            idSkill = it?.getString("id").toString()
        }

        vm.timeSlots.observe(viewLifecycleOwner){
            val mySlots = mutableListOf<TimeSlot>()
            it.map {
                if(it.idSkill == idSkill){
                    mySlots.add(it)
                }
            }

            mySlots.let {
                val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>)
                rv.adapter = adapter
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