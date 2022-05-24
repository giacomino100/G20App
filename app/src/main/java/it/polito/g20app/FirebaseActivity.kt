package it.polito.g20app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.g20app.databinding.ActivityFirebaseBinding
import org.json.JSONException
import org.json.JSONObject

private var currentPhotoPath: String? = null

class FirebaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityFirebaseBinding
    private var photo: Photo = Photo()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirebaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView


        //FASE DI INIZIALIZZAZIONE DEL HEADER DEL MENU

        //Caricamento shared preferences
        val sharedPrefR = this.getPreferences(Context.MODE_PRIVATE)
        val profileInfo = "{'full name' : '${getString(R.string.full_name)}', nickname : '${getString(R.string.nickname)}', " +
                "email : '${getString(R.string.email)}', location : '${getString(R.string.location)}', skill1 : '${getString(R.string.skill1)}'," +
                " skill2 : '${getString(R.string.skill2)}', description1 : '${getString(R.string.description1)}', " +
                "description2 : '${getString(R.string.description2)}', " + "path: '${currentPhotoPath.toString()}'}"
        val json = sharedPrefR?.getString("profile", profileInfo)?.let { JSONObject(it) }

        //SETTING IMAGE PROFILE
        val headerView: View = binding.navView.getHeaderView(0)
        val img: ImageView = headerView.findViewById(R.id.nav_head_avatar)
        try {
            if (json != null) {
                if(json.get("path").toString() == "null")
                    currentPhotoPath = null
                else
                    currentPhotoPath = json.get("path").toString()
            }
        } catch (e : JSONException) {
            println(e)
        }
        if(currentPhotoPath != null){
            val bitmap: Bitmap? = photo.loadImageFromStorage(currentPhotoPath, "icon")
            img.setImageBitmap(bitmap)
        }

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        //SETTING NAMES
        val tv1: TextView = headerView.findViewById(R.id.nav_head_username)
        if (json != null) {
            tv1.text = auth.currentUser?.displayName.toString()
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_skills_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setCheckedItem(R.id.nav_skills_list)

        //questo metodo invece ci serve per abilitare un listener quando clicchiamo il menu laterale
        navView.setNavigationItemSelectedListener(this)

        //SETTING SIGNOUT BUTTON
        headerView.findViewById<Button>(R.id.signout_button).setOnClickListener {
            signOut()
            //redirect to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



        //SETTING USER PROFILE
        createProfile()
    }

    fun createProfile() {
        //CREAZIONE NUOVO DOCUMENTO PER UN NUOVO UTENTE
        //documento creato al primo login
        val docData = hashMapOf(
            "fullname" to auth.currentUser!!.displayName,
            "nickname" to "Your nickname",
            "email" to auth.currentUser!!.email,
            "location" to "Your location",
        )

        db
            .collection("profiles")
            .document(auth.uid.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        if (!document.exists()) {
                            db.collection("profiles").document(auth.uid.toString()).set(docData)
                                .addOnSuccessListener {

                                    //SE IL DOCUMENTO VIENE CREATO CORRETTAMENTE SI CREANO LE DUE SKILL VUOTE
                                    val skill1 = hashMapOf(
                                        "idUser" to auth.uid.toString(),
                                        "name" to "skill1",
                                        "description" to "description skill1"
                                    )
                                    db.collection("skills").document().set(skill1)
                                        .addOnSuccessListener {
                                            val skill2 = hashMapOf(
                                                "idUser" to auth.uid.toString(),
                                                "name" to "skill2",
                                                "description" to "description skill2"
                                            )
                                            db.collection("skills").document().set(skill2)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        this,
                                                        "Data Saved",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                }
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val drawerLayout: DrawerLayout = binding.drawerLayout

        if(item.itemId == R.id.nav_adv_list){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_adv_list)
        }
        if(item.itemId == R.id.nav_show_profile){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_show_profile)
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)  || super.onSupportNavigateUp()

    }


    //Questa servirà?

    private fun getUserProfile() {
        // [START get_user_profile]
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid
        }
        // [END get_user_profile]
    }

    private fun signOut() {
        // [START auth_sign_out]
        Firebase.auth.signOut()
        // [END auth_sign_out]
    }


}