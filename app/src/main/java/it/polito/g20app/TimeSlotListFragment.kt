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

    private val viewModelT by viewModels<TimeSlotVM>()
    private var auth: FirebaseAuth = Firebase.auth
    private var isTimeSlotSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                if (it.get("flag") as String == "true"){
                    Log.d("saved", it.get("flag") as String)
                    isTimeSlotSaved = true
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_time_slot_list, container, false)
        activity?.findViewById<NavigationView?>(R.id.nav_view)?.setCheckedItem(R.id.nav_adv_list)
        (activity as FirebaseActivity).supportActionBar?.setHomeButtonEnabled(true)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //setting name of toolbar
        if(isTimeSlotSaved)(activity as FirebaseActivity).supportActionBar?.title = "My favorites"

        val rv = root.findViewById<RecyclerView>(R.id.rv)
        val fab = root.findViewById<FloatingActionButton>(R.id.fab)

        //se stiamo visualizzando i time slot preferiti non possiamo aggiungerne altri
        if(isTimeSlotSaved)
            fab.visibility = View.GONE

        rv.layoutManager = LinearLayoutManager(root.context)

        //defining ViewModel
        viewModelT.timeSlots.observe(viewLifecycleOwner) {
            if (isTimeSlotSaved){
                //qui dentro se vogliamo visualizzare i time slot aggiunti ai favoriti da un utente
                root.findViewById<TextView>(R.id.alert).isVisible = it.isEmpty()
                val adapter = TimeSlotAdapter(it.filter { ts -> ts.userInterested.contains(auth.uid) } as MutableList<TimeSlot>, true, isTimeSlotSaved)
                rv.adapter = adapter
            } else {
                //this row is needed to show the message in case the list is empty
                root.findViewById<TextView>(R.id.alert).isVisible = it.isEmpty()
                val adapter = TimeSlotAdapter(it.filter { ts -> ts.idUser == auth.uid } as MutableList<TimeSlot>, false, isTimeSlotSaved)
                rv.adapter = adapter
            }
        }

        fab.setOnClickListener {
            //adding a new timeSlot
            val bundle = Bundle()
            it.findNavController().navigate(R.id.action_nav_adv_list_to_timeSlotEditFragment, bundle)
        }

        return root
    }


}