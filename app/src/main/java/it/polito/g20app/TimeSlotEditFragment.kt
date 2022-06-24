package it.polito.g20app

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class TimeSlotEditFragment : Fragment() {

    val viewModelT by viewModels<TimeSlotVM>()
    val viewModelS by viewModels<SkillVM>()

    private var cal: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_time_slot_edit, container, false)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as FirebaseActivity).supportActionBar?.setHomeButtonEnabled(false)

        return root

    }

    // create an OnDateSetListener
    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(context,
                timeSetListener,
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE), true).show()
        }

    // create an OnDateSetListener
    private val timeSetListener =
        TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR, hour)
            cal.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }

    private fun updateTimeInView() {
        view?.findViewById<TextView>(R.id.slot_date_and_time_edit)?.text = cal.time.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val auth: FirebaseAuth = Firebase.auth
        val spinner: Spinner = view.findViewById(R.id.spinner)

        view.findViewById<Button>(R.id.chooseDateAndTime).setOnClickListener{
            DatePickerDialog(this.requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()

        }

        if(arguments.let { it!!.isEmpty }){
            //Create a new TimeSlot
            (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.create_new_time_slot)

            viewModelS.skillsProfile.observe(viewLifecycleOwner) {
                val spinnerChoices = mutableListOf("Select the skill")
                Log.d("skillsFiltrate", viewModelS.skills.value?.filter { s -> it.map { sp -> sp.idSkill }.contains(s.id) }.toString())
                spinnerChoices.addAll(viewModelS.skills.value?.filter { s -> it.map { sp -> sp.idSkill }.contains(s.id) }!!.map { skill -> skill.name })
                spinner.adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, spinnerChoices)
            }

            requireActivity()
                .onBackPressedDispatcher
                .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    @SuppressLint("CutPasteId")
                    override fun handleOnBackPressed() {
                        val newTimeSlot = TimeSlot()
                        newTimeSlot.id = " "
                        newTimeSlot.idUser = auth.uid!!
                        newTimeSlot.idSkill = viewModelS.skills.value?.filter { s -> s.name == spinner.selectedItem }?.map { s -> s.id }!![0]
                        newTimeSlot.title = view.findViewById<EditText>(R.id.slot_title_edit).text.toString()
                        newTimeSlot.description = view.findViewById<EditText>(R.id.slot_description_edit).text.toString()
                        newTimeSlot.location = view.findViewById<EditText>(R.id.slot_location_edit).text.toString()
                        newTimeSlot.duration = view.findViewById<EditText>(R.id.slot_duration_edit).text.toString()
                        newTimeSlot.date = view.findViewById<TextView>(R.id.slot_date_and_time_edit).text.toString()
                        newTimeSlot.credits = view.findViewById<EditText>(R.id.slot_credits_edit).text.toString()
                        newTimeSlot.userInterested = listOf()
                        newTimeSlot.buyer = " "
                        newTimeSlot.taken = false

                        Log.d("credits", newTimeSlot.toString())
                        viewModelT.addTimeSlot(newTimeSlot)

                        //Management snack bar
                        val root = view.rootView
                        Snackbar.make(root, "TimeSlot created", Snackbar.LENGTH_LONG).show()

                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            )
        } else {
            //Update an existing TimeSlot
            (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.edit_time_slot)

            var initSkill = "empty"

            viewModelT.timeSlots.observe(viewLifecycleOwner) {
                initSkill = it.filter { t -> t.id == arguments.let { b -> b?.get("id").toString() } }[0].idSkill
            }

            viewModelS.skills.observe(viewLifecycleOwner) {
                val spinnerChoices = mutableListOf(it.filter { skill -> skill.id == initSkill }[0].name)
                spinnerChoices.addAll(it.filter { skill -> skill.id != initSkill }.map { skill -> skill.name })
                spinner.adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, spinnerChoices)
            }

            viewModelT.timeSlots.observe(viewLifecycleOwner){ list ->
                val ts = list.filter { it.id == arguments.let { b -> b?.get("id") } }[0]
                view.findViewById<TextView>(R.id.slot_title_edit).text = ts.title
                view.findViewById<TextView>(R.id.slot_description_edit).text = ts.description
                view.findViewById<TextView>(R.id.slot_date_and_time_edit).text = ts.date
                view.findViewById<TextView>(R.id.slot_duration_edit).text = ts.duration
                view.findViewById<TextView>(R.id.slot_location_edit).text = ts.location
                view.findViewById<TextView>(R.id.slot_credits_edit).text = ts.credits.toString()

            }

        requireActivity()
                .onBackPressedDispatcher
                .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            // Do custom work here
                            val updatedTimeSlot = TimeSlot()
                            updatedTimeSlot.id = arguments.let { b -> b?.get("id") }.toString()
                            updatedTimeSlot.title = view.findViewById<EditText>(R.id.slot_title_edit).text.toString()
                            updatedTimeSlot.description = view.findViewById<EditText>(R.id.slot_description_edit).text.toString()
                            updatedTimeSlot.date = view.findViewById<TextView>(R.id.slot_date_and_time_edit).text.toString()
                            updatedTimeSlot.duration = view.findViewById<EditText>(R.id.slot_duration_edit).text.toString()
                            updatedTimeSlot.location = view.findViewById<EditText>(R.id.slot_location_edit).text.toString()
                            updatedTimeSlot.idSkill = viewModelS.skills.value?.filter { s -> s.name == spinner.selectedItem }?.map { s -> s.id }!![0]
                            updatedTimeSlot.idUser = auth.uid.toString()
                            updatedTimeSlot.credits = view.findViewById<EditText>(R.id.slot_credits_edit).text.toString()
                            viewModelT.updateTimeSlot(updatedTimeSlot)

                            //Management snack bar
                            val root = view.rootView
                            Snackbar.make(root, "Time slot updated", Snackbar.LENGTH_LONG).show()

                            if (isEnabled) {
                                isEnabled = false
                                requireActivity().onBackPressed()
                            }
                        }
                    }
                )

        }
        }
}
