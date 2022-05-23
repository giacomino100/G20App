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
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotEditFragment : Fragment() {

    val vm by viewModels<TimeSlotVM>()
    val vm_skill by viewModels<SkillVM>()

    var cal = Calendar.getInstance()

    lateinit var idSkill1: String
    lateinit var idSkill2: String

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

    private fun updateDateInView() {
        val dateView = view?.findViewById<TextView>(R.id.slot_date_and_time_edit)
        val myFormat = "dd/MM/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.ITALIAN)
        if (dateView != null) {
            dateView.text = sdf.format(cal.getTime())
            Log.d("data", dateView.text.toString())
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

        var skills = listOf<Skill>()
        view.findViewById<Button>(R.id.chooseDateAndTime).setOnClickListener{
            DatePickerDialog(this.requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()

        }

        //load skills
        vm_skill.skills.observe(viewLifecycleOwner){
            idSkill1 = it[0].id
            idSkill2= it[1].id
            var skills = it.filter { it.idUser == auth.uid  }
            idSkill1 = skills[0].id
            idSkill2= skills[1].id
            var name_skills = it.filter { it.idUser == auth.uid  }.map { it.name }
            view.findViewById<Spinner>(R.id.spinner).adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, name_skills)
        }

        //costrutto necessario per leggere dai dati passati dal bundle
        arguments?.let { it ->
            if(it.isEmpty){
                //CASO ADD: in questo if andiamo a creare un nuovo time slot

                //title of action bar changed
                (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.create_new_time_slot)

                Log.d("back","bundle is empty")
                requireActivity()
                    .onBackPressedDispatcher
                    .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        @SuppressLint("CutPasteId")
                        override fun handleOnBackPressed() {
                            Log.d("back", "Fragment back pressed invoked from fab")

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
                            vm_skill.skills.value?.filter { it.idUser == auth.uid && it.name == view.findViewById<Spinner>(R.id.spinner).selectedItem.toString()}?.forEach {
                                idSkillSelected = it.id
                            }
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
                    }
                    )
            } else {
                //CASO EDIT: modifica di un fragment esistente
                Log.d("back","bundle is not empty")

                //title of action bar changed
                (activity as FirebaseActivity).supportActionBar?.setTitle(R.string.edit_time_slot)

                //settaggio campi nella edit
                view.findViewById<EditText>(R.id.slot_title_edit).setText(it.getString("title"))
                view.findViewById<EditText>(R.id.slot_description_edit).setText(it.getString("description"))
                view.findViewById<TextView>(R.id.slot_date_and_time_edit).setText(it.getString("dateAndTime"))
                view.findViewById<EditText>(R.id.slot_duration_edit).setText(it.getString("duration"))
                view.findViewById<EditText>(R.id.slot_location_edit).setText(it.getString("location"))

                //TODO settaggio campi nello spinner
                val skillSelected = vm_skill.skills.value?.filter{ item -> item.id == it.getString("id").toString()}?.map { it.name }

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
                                vm_skill.skills.value?.filter { it.idUser == auth.uid && it.name == view.findViewById<Spinner>(R.id.spinner).selectedItem.toString()}?.forEach {
                                    idSkillSelected = it.id
                                }
                            updatedTimeSlot.idSkill = idSkillSelected
                              // if you want onBackPressed() to be called as normal afterwards

                              //UPDATE
                              updatedTimeSlot.id = it.getString("id").toString()
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


}
