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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotEditFragment : Fragment() {

    val vm by viewModels<TimeSlotVM>()
    val vm_skill by viewModels<SkillVM>()

    var cal = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_time_slot_edit, container, false)

        // android:configChanges="orientation|screenSize" da aggiungere sotto activity nel manifest
        // serve per avere la stessa toolbar quando ruotiamo lo schermo

        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as FirebaseActivity).supportActionBar?.setHomeButtonEnabled(false)

        return root

    }

    // create an OnDateSetListener
    private val dateSetListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                               dayOfMonth: Int) {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(context,
                timeSetListener,
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE), true).show()

        }

    }

    // create an OnDateSetListener
    private val timeSetListener = object : TimePickerDialog.OnTimeSetListener {
        override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
            cal.set(Calendar.HOUR, hour)
            cal.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }
    }

    private fun updateTimeInView() {
        view?.findViewById<TextView>(R.id.slot_date_and_time_edit)?.text = cal.time.toString()
        Log.d("data", cal.time.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var auth: FirebaseAuth = Firebase.auth

        var l: ListenerRegistration
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        view.findViewById<Button>(R.id.chooseDateAndTime).setOnClickListener{
            DatePickerDialog(this.requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()

        }


        var idTimeSlot = " "
        var isAddCase: Boolean = true
        //costrutto necessario per leggere dai dati passati dal bundle
        arguments?.let { it ->
            isAddCase = it.isEmpty
            idTimeSlot = it.getString("id").toString()
        }

        if(isAddCase){
            //title of action bar changed
            (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.create_new_time_slot)
            requireActivity()
                .onBackPressedDispatcher
                .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    @SuppressLint("CutPasteId")
                    override fun handleOnBackPressed() {
                        view.findViewById<EditText>(R.id.slot_title_edit).text = view.findViewById<EditText>(R.id.slot_title_edit).text
                        view.findViewById<EditText>(R.id.slot_description_edit).text = view.findViewById<EditText>(R.id.slot_description_edit).text
                        view.findViewById<TextView>(R.id.slot_date_and_time_edit).text = view.findViewById<TextView>(R.id.slot_date_and_time_edit).text.toString()
                        view.findViewById<EditText>(R.id.slot_duration_edit).text = view.findViewById<EditText>(R.id.slot_duration_edit).text
                        view.findViewById<EditText>(R.id.slot_location_edit).text = view.findViewById<EditText>(R.id.slot_location_edit).text
                        val adv = TimeSlot()

                        adv.title = view.findViewById<EditText>(R.id.slot_title_edit).text.toString()
                        adv.description = view.findViewById<EditText>(R.id.slot_description_edit).text.toString()
                        adv.date = view.findViewById<TextView>(R.id.slot_date_and_time_edit).text.toString()
                        adv.duration = view.findViewById<EditText>(R.id.slot_duration_edit).text.toString()
                        adv.location = view.findViewById<EditText>(R.id.slot_location_edit).text.toString()

                        //setting idUser per il nuovo timeslot
                        adv.idUser = auth.uid!!

                        //setting idSkill per il nuovo timeslot
                        //recuperiamo l'id della nuova skill
                        var idSkillSelected = " "
                        Log.d("selectedItem", view.findViewById<Spinner>(R.id.spinner).selectedItem.toString())
                        /*vm_skill.skills.value?.filter { it.idUser == auth.uid && it.name == view.findViewById<Spinner>(R.id.spinner).selectedItem.toString()}?.forEach {
                            idSkillSelected = it.id
                        }*/
                        adv.idSkill = idSkillSelected

                        //ADD NEW ADV
                        vm.addTimeSlot(adv)

                        //Management snackbar
                        val root = view.rootView
                        Snackbar.make(root, "Time slot added", Snackbar.LENGTH_LONG)
                            .show()

                        // if you want onBackPressed() to be called as normal afterwards
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                })
            } else {
                //CASO EDIT: modifica di un fragment esistente

                //title of action bar changed
                (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.edit_time_slot)

                vm.timeSlots.observe(viewLifecycleOwner){ list ->
                    val ts = list.filter { it.id == idTimeSlot }[0]
                    view.findViewById<TextView>(R.id.slot_title_edit).text = ts.title
                    view.findViewById<TextView>(R.id.slot_description_edit).text = ts.description
                    view.findViewById<TextView>(R.id.slot_date_and_time_edit).text = ts.date
                    view.findViewById<TextView>(R.id.slot_duration_edit).text = ts.duration
                    view.findViewById<TextView>(R.id.slot_location_edit).text = ts.location

                    Log.d("Spinnerchoices", vm_skill.skills.value.toString())
                    Log.d("Spinnerchoices", ts.idSkill)



                    val spinnerChoices = mutableListOf<String>()
                    //spinnerChoices.add(vm_skill.skills.value!!.filter { s -> s.id == ts.idSkill }[0].name)
                    //spinnerChoices.addAll(vm_skill.skills.value!!.filter { s -> s.id != ts.idSkill }.map { s -> s.name })
                    //view.findViewById<Spinner>(R.id.spinner).adapter = ArrayAdapter(this.requireContext(),
                        //android.R.layout.simple_spinner_item, spinnerChoices)
                }

            requireActivity()
                    .onBackPressedDispatcher
                    .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            Log.d("back", "Fragment back pressed invoked from edit")
                            // Do custom work here
                              val updatedTimeSlot = TimeSlot()

                              updatedTimeSlot.title = view.findViewById<EditText>(R.id.slot_title_edit).text.toString()
                              updatedTimeSlot.description = view.findViewById<EditText>(R.id.slot_description_edit).text.toString()
                              updatedTimeSlot.date = view.findViewById<TextView>(R.id.slot_date_and_time_edit).text.toString()
                              updatedTimeSlot.duration = view.findViewById<EditText>(R.id.slot_duration_edit).text.toString()
                              updatedTimeSlot.location = view.findViewById<EditText>(R.id.slot_location_edit).text.toString()
                                //setting idSkill per il timeslot da aggiornare
                                //recuperiamo l'id dell'eventuale skill modificata
                                var idSkillSelected = " "
                                /*vm_skill.skills.value?.filter { it.idUser == auth.uid && it.name == view.findViewById<Spinner>(R.id.spinner).selectedItem.toString()}?.forEach {
                                    idSkillSelected = it.id
                                }*/
                            updatedTimeSlot.idSkill = idSkillSelected
                              // if you want onBackPressed() to be called as normal afterwards

                              //UPDATE
                              updatedTimeSlot.id = idTimeSlot
                              updatedTimeSlot.idUser = auth.uid.toString()

                              vm.updateTimeSlot(updatedTimeSlot)

                              //Management snackbar
                              val root = view.rootView
                              Snackbar.make(root, "Time slot updated", Snackbar.LENGTH_LONG)
                                  .setAction("Redo") {
                                      //TODO: Responds to click on the action for example reopening the editfragment
                                  }
                                  .show()

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
