package it.polito.g20app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
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

@Suppress("BooleanLiteralArgument")
class TimeSlotListFragment : Fragment(R.layout.fragment_time_slot_list) {

    private val viewModelT by viewModels<TimeSlotVM>()
    private var auth: FirebaseAuth = Firebase.auth
    private var isTimeSlotSaved = false
    private var isTimeSlotAssigned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                if (it.get("flag") as String == "true") isTimeSlotSaved = true
                if (it.get("flag") as String == "trueAssigned") isTimeSlotAssigned = true
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
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
        val switchAssigned = root.findViewById<Switch>(R.id.switch_assignedTS)

        //favourite timeslots case
        if(isTimeSlotSaved) {
            (activity as FirebaseActivity).supportActionBar?.title = "My favorites"
            fab.visibility = View.GONE
        }

        //assigned timeslots case
        if(isTimeSlotAssigned) {
            fab.visibility = View.GONE
            switchAssigned.visibility = View.VISIBLE
            (activity as FirebaseActivity).supportActionBar?.title = "My timeslots purchased"
            viewModelT.timeSlots.observe(viewLifecycleOwner) {
                Log.d("timeslotsPurchased", it.filter { t-> t.buyer == auth.uid && t.taken}.toString())
                it.filter { t-> t.buyer == auth.uid && t.taken}.let { list ->
                    val adapter = TimeSlotAdapter(list as MutableList<TimeSlot>, true, false, true)
                    rv.adapter = adapter
                }
            }
        }

        rv.layoutManager = LinearLayoutManager(root.context)


        switchAssigned?.setOnCheckedChangeListener { _, isChecked ->
            viewModelT.timeSlots.observe(viewLifecycleOwner){
                Log.d("timeslotsPurchased", it.filter { t-> t.buyer == auth.uid && t.taken}.toString())
                if(isChecked) {
                    (activity as FirebaseActivity).supportActionBar?.title = "My timeslots sold"
                    switchAssigned.text = "Sold"
                }
                else {
                    (activity as FirebaseActivity).supportActionBar?.title = "My timeslots purchased"
                    switchAssigned.text = "Purchased"
                }

                val sortedSlots = if (isChecked) it.filter { t-> t.idUser == auth.uid && t.taken }
                else it.filter { t-> t.buyer == auth.uid && t.taken}

                sortedSlots.let { list ->
                    val adapter = TimeSlotAdapter(list as MutableList<TimeSlot>, true, false, true)
                    rv.adapter = adapter
                }
            }
        }

        //Loading timeslots
    viewModelT.timeSlots.observe(viewLifecycleOwner) {
            if (isTimeSlotSaved){
                //user favourite timeslots case
                root.findViewById<TextView>(R.id.alert).isVisible = it.isEmpty()
                val adapter = TimeSlotAdapter(it.filter { ts -> ts.userInterested.contains(auth.uid) } as MutableList<TimeSlot>, true, isTimeSlotSaved, false)
                rv.adapter = adapter
            } else if(isTimeSlotAssigned) {
                //user purchased timeslots
                val adapter = TimeSlotAdapter(it.filter { ts -> ts.idUser != auth.uid && ts.buyer == auth.uid } as MutableList<TimeSlot>, true, false, true)
                rv.adapter = adapter
            } else {
                //this row is needed to show the message in case the list is empty
                root.findViewById<TextView>(R.id.alert).isVisible = it.isEmpty()
                Log.d("credits", it.toString())
                val adapter = TimeSlotAdapter(it.filter { ts -> ts.idUser == auth.uid } as MutableList<TimeSlot>, false, isTimeSlotSaved, false)
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