package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as FirebaseActivity).supportActionBar?.title = "Ratings"

        viewModelR.ratings.observe(viewLifecycleOwner){
            //show Buyer ratings
            val emptyListMsg = root.findViewById<TextView>(R.id.emptyVendorReview)

            val ratings = it.filter{
                it1 ->
                it1.idVendor == auth.uid && it1.idWriter != auth.uid
            }
            Log.d("ratingdiprova", ratings.toString())
            if(ratings.isEmpty()){
                emptyListMsg.visibility = View.VISIBLE
            }else{
                emptyListMsg.visibility = View.GONE
                val rv = root.findViewById<RecyclerView>(R.id.vendorRV)
                rv.layoutManager = LinearLayoutManager(root.context)
                val adapter = RatingAdapter(ratings as MutableList<Rating>)
                rv.adapter = adapter
            }

        }

        viewModelR.ratings.observe(viewLifecycleOwner){
            //show Vendor ratings
            val emptyListMsg = root.findViewById<TextView>(R.id.emptyBuyerReviews)
            val ratings = it.filter{
                it1 ->
                it1.idBuyer == auth.uid && it1.idWriter != auth.uid
            }
            Log.d("ratingdiprova", ratings.toString())
            if(ratings.isEmpty()){
                emptyListMsg.visibility = View.VISIBLE
            }else{
                emptyListMsg.visibility = View.GONE
                val rv = root.findViewById<RecyclerView>(R.id.buyerRV)
                rv.layoutManager = LinearLayoutManager(root.context)
                val adapter = RatingAdapter(ratings as MutableList<Rating>)
                rv.adapter = adapter
            }
        }
        return root
    }
}