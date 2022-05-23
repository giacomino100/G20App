package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TimeSlotListFragment : Fragment(R.layout.fragment_time_slot_list) {

    val vm by viewModels<TimeSlotVM>()
    private var auth: FirebaseAuth = Firebase.auth

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

        //defining ViewModel
        vm.timeSlots.observe(viewLifecycleOwner) {
            //this row is needed to show the message in case the list is empty
            root.findViewById<TextView>(R.id.alert).isVisible = it.isEmpty()
            val mySlots = mutableListOf<TimeSlot>()
            it.map {
                if(it.idUser == auth.uid){
                 mySlots.add(it)
                }
            }

            mySlots.let {
                val adapter = TimeSlotAdapter(it as MutableList<TimeSlot>, false)
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