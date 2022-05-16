package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


class TimeSlotListFragment : Fragment(R.layout.fragment_time_slot_list) {

    val vm by viewModels<TimeSlotVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_time_slot_list, container, false)
        activity?.findViewById<NavigationView?>(R.id.nav_view)?.setCheckedItem(R.id.nav_adv_list)

        (activity as FirebaseActivity).supportActionBar?.setHomeButtonEnabled(true)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv = root.findViewById<RecyclerView>(R.id.rv)
        val fab = root.findViewById<FloatingActionButton>(R.id.fab)
        rv.layoutManager = LinearLayoutManager(root.context)

        val l = mutableListOf<TimeSlot>()

        val prova = TimeSlot()
        val prova1 = TimeSlot()
        val prova2 = TimeSlot()


        prova.id = 1
        prova.title = "titolo"
        prova.description = "descrizione"
        prova.dateAndTime = "Data personale"
        prova.location = "Torino"
        prova.duration = "2h"

        prova1.id = 2
        prova1.title = "titolo1"
        prova1.description = "descrizione1"
        prova1.dateAndTime = "Data personale1"
        prova1.location = "Torino1"
        prova1.duration = "2h1"

        prova2.id = 3
        prova2.title = "titolo2"
        prova2.description = "descrizione2"
        prova2.dateAndTime = "Data personale2"
        prova2.location = "Torino2"
        prova2.duration = "2h2"

        //l.add(prova)
        //l.add(prova1)

        //defining ViewModel
        vm.value.observe(viewLifecycleOwner) {
            it.let {
                //TODO if the list is empty we have to show a message
                val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>)
                rv.adapter = adapter
            }
        }

        fab.setOnClickListener {
            //aggiungere un nuovo elemento alla lista
            //il nuovo elemento deve essere creato dall'utente utilizzando il fragment TimeSlotEditFragment
            val bundle = Bundle()
            it.findNavController()
                .navigate(R.id.action_nav_adv_list_to_timeSlotEditFragment, bundle)

        }

        return root
    }


}