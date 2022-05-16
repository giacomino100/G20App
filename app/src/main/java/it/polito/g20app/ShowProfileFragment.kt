package it.polito.g20app

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.json.JSONException
import org.json.JSONObject

private var currentPhotoPath: String? = null

class ShowProfileFragment : Fragment(R.layout.fragment_home) {
    private var photo: Photo = Photo()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //LOADING shared preferences
        Log.d("load", "caricamento SP show profile")
        val sharedPrefR = this.activity?.getPreferences(MODE_PRIVATE)
        val profileInfo = "{'full name' : '${getString(R.string.full_name)}', nickname : '${getString(R.string.nickname)}', " +
                "email : '${getString(R.string.email)}', location : '${getString(R.string.location)}', skill1 : '${getString(R.string.skill1)}'," +
                " skill2 : '${getString(R.string.skill2)}', description1 : '${getString(R.string.description1)}', " +
                "description2 : '${getString(R.string.description2)}', " + "path: '${currentPhotoPath.toString()}'}"

        val json = sharedPrefR?.getString("profile", profileInfo)?.let { JSONObject(it) }
        val img: ImageView = root.findViewById(R.id.imageView)

        val tv1: TextView = root.findViewById(R.id.fullname)
        val tv2: TextView = root.findViewById(R.id.nickname)
        val tv3: TextView = root.findViewById(R.id.email)
        val tv4: TextView = root.findViewById(R.id.location)
        val tv5: TextView = root.findViewById(R.id.skill1)
        val tv6: TextView = root.findViewById(R.id.skill2)
        val tv7: TextView = root.findViewById(R.id.description1)
        val tv8: TextView = root.findViewById(R.id.description2)

        if(json != null){
            tv1.text = (json.get("full name").toString())
            tv2.text = (json.get("nickname").toString())
            tv3.text = (json.get("email").toString())
            tv4.text = (json.get("location").toString())
            tv5.text = (json.get("skill1").toString())
            tv6.text = (json.get("skill2").toString())
            tv7.text = (json.get("description1").toString())
            tv8.text = (json.get("description2").toString())

            try {
                if(json.get("path").toString() == "null")
                    currentPhotoPath = null
                else
                    currentPhotoPath = json.get("path").toString()
            } catch (e : JSONException) {
                println(e)
            }
        }
        if(currentPhotoPath != null){
            val bitmap: Bitmap? = photo.loadImageFromStorage(currentPhotoPath, "icon")
            img.setImageBitmap(bitmap)
        }

        return root

    }


    /* Enable option menu with icon for edit profile */
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(show_profile_menu: Menu, inflater: MenuInflater) {
        //menu item that showing the edit icon
        inflater.inflate(R.menu.show_profile_menu, show_profile_menu)
        super.onCreateOptionsMenu(show_profile_menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.edit_profile_icon -> {
                //Opening edit profile fragment
                val fullname = view?.findViewById<TextView>(R.id.fullname)?.text.toString()
                val nickname = view?.findViewById<TextView>(R.id.nickname)?.text.toString()
                val email = view?.findViewById<TextView>(R.id.email)?.text.toString()
                val location = view?.findViewById<TextView>(R.id.location)?.text.toString()
                val skill1 = view?.findViewById<TextView>(R.id.skill1)?.text.toString()
                val skill2 = view?.findViewById<TextView>(R.id.skill2)?.text.toString()
                val description1 = view?.findViewById<TextView>(R.id.description1)?.text.toString()
                val description2 = view?.findViewById<TextView>(R.id.description2)?.text.toString()

                var bundle = Bundle()

                bundle.putString("full name", fullname)
                bundle.putString("nickname", nickname)
                bundle.putString("email", email)
                bundle.putString("location", location)
                bundle.putString("skill1", skill1)
                bundle.putString("skill2", skill2)
                bundle.putString("description1", description1)
                bundle.putString("description2", description2)
                bundle.putString("path", currentPhotoPath)

                findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}