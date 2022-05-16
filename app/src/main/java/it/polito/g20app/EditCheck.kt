package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class EditCheck : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_check, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linkInitFirebase = "https://firebase.google.com/docs/android/setup"
        val initFirebase = "E' stata seguita la guida a questo link" + linkInitFirebase + "seguendo option 1"

        val linkSignIn = "https://firebase.google.com/docs/auth/android/google-signin"
        val linkVideo = "https://www.youtube.com/watch?v=clU6s0M88OE"

    }


}
