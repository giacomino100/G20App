package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar

class RatingFragment : Fragment() {

    val viewModelR by viewModels<RatingVM>()
    var idBuyer = " "
    var idVendor = " "
    var idTimeSlot = " "
    var rating = " " //numero di stelle che viene settato nell'onchange
    var idWriter = " " //chi scrive la recensione: può essere il buyer oppure il vendor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_rating, container, false)
        (activity as FirebaseActivity).supportActionBar?.title = "Assigning a rating"

        arguments.let {
            if (it != null) {
                idBuyer = it.getString("idBuyer") as String
                idVendor = it.getString("idVendor") as String
                idTimeSlot = it.getString("idTimeSlot") as String
                idWriter = it.getString("idWriter") as String
            }
        }


        val rate = root.findViewById<RatingBar>(R.id.ratingBarAssign)
        rate.setOnRatingBarChangeListener { ratingBar, fl, b ->
            rating = fl.toString()
        }

        //Salvo informazioni rating tramite BackPress
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val comment = root.findViewById<EditText>(R.id.editTextRatingComment).text.toString()
                    val docData = Rating("", idVendor, idBuyer, idTimeSlot, rating, comment, idWriter)
                    Log.d("ratings", docData.toString())
                    viewModelR.addRating(docData)
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
            )


        return root
    }
}