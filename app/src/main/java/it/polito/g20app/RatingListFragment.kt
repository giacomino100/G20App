package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RatingListFragment : Fragment(R.layout.fragment_list_rating) {

    private val viewModelR by viewModels<RatingVM>()
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list_rating, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.ratings_rv)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        //TODO change title
        (activity as FirebaseActivity).supportActionBar?.title = "Ratings"

        rv.layoutManager = LinearLayoutManager(root.context)

        viewModelR.ratings.observe(viewLifecycleOwner) {
            val adapter = RatingAdapter(it as MutableList<Rating>)
            rv.adapter = adapter
        }
        return root
    }
}