package it.polito.g20app

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass.
 * Use the [TimeSlotDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private var id1: Int = 0

    val vm by viewModels<TimeSlotVM>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_time_slot_details, container, false)
        //recupero dati dal Bundle
        arguments.let {
            if (it != null) {
                id1 = it.getString("id")!!.toInt()
                var timeSlot: TimeSlot
                vm.get(id1).observe(viewLifecycleOwner){
                    timeSlot = it
                    root.findViewById<TextView>(R.id.slot_title).text = timeSlot.title
                    root.findViewById<TextView>(R.id.slot_description).text = timeSlot.description
                    root.findViewById<TextView>(R.id.slot_date_and_time).text = timeSlot.dateAndTime
                    root.findViewById<TextView>(R.id.slot_duration).text = timeSlot.duration
                    root.findViewById<TextView>(R.id.slot_location).text = timeSlot.location
                }

            }

        }

        return root

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(time_slot_menu: Menu, inflater: MenuInflater) {
        //menu item that allows editing the advertisement
        inflater.inflate(R.menu.time_slot_menu, time_slot_menu)
        super.onCreateOptionsMenu(time_slot_menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.icona -> {
                //aprire TimeSlotEditFragment.kt
                val title = view?.findViewById<TextView>(R.id.slot_title)?.text.toString()
                val description = view?.findViewById<TextView>(R.id.slot_description)?.text.toString()
                val dateAndTime = view?.findViewById<TextView>(R.id.slot_date_and_time)?.text.toString()
                val duration = view?.findViewById<TextView>(R.id.slot_duration)?.text.toString()
                val location = view?.findViewById<TextView>(R.id.slot_location)?.text.toString()

                var bundle = Bundle()

                bundle.putString("id", id1.toString())
                bundle.putString("title", title)
                bundle.putString("description", description)
                bundle.putString("dateAndTime", dateAndTime)
                bundle.putString("duration", duration)
                bundle.putString("location", location)

                findNavController().navigate(R.id.action_nav_slot_details_to_timeSlotEditFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}