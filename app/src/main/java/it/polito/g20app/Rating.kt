package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RatingAdapter(val data: MutableList<Rating>): RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {
    private var displayData = data.toMutableList()

    class RatingViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        private val title: TextView = v.findViewById(R.id.ratingCardUserFullname)
        val comment: TextView = v.findViewById(R.id.ratingCardComment)
        private val commentTitle: TextView = v.findViewById(R.id.ratingCardCommentTitle)
        private val card: CardView = v.findViewById(R.id.ratingCardView)
        private val rate: RatingBar = v.findViewById(R.id.ratingBarRatingsCard)

        fun bind(rating: Rating, action: (v: View)->Unit) {
            title.text = "Loading..."
            //Getting buyer name from the db: it's used to set the chat card title
            db.collection("profiles").document(rating.idWriter).get().addOnSuccessListener {
                val fullname = it.get("fullname") as String
                title.text = fullname
            }.addOnFailureListener {
                Log.d("database", "Error getting profile info")
            }
            card.setOnClickListener(action)
            if(rating!= null) {
                rate.rating = rating.rate.toFloat()
                rate.setIsIndicator(true)
                if(comment.text != ""){
                    comment.text = rating.comment
                }else{
                    comment.visibility = View.GONE
                    commentTitle.visibility = View.GONE
                }
            }
            //TODO prendere i rating necessari
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rating, parent, false)
        return RatingViewHolder(vg)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val item = displayData[position]
        holder.bind(item) {
            val bundle = Bundle()
            bundle.putString("id", item.id) //idRating
            bundle.putString("idVendor", item.idVendor)
            bundle.putString("idBuyer", item.idBuyer)
            bundle.putString("idTimeSlot", item.idTimeSlot)
            bundle.putString("rate", item.rate)
            bundle.putString("comment", item.comment)
            Log.d("contentBundle", bundle.toString())

        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}