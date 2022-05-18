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

        Log.d("codice_token", auth.uid.toString())
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
                R.id.nav_adv_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setCheckedItem(R.id.nav_adv_list)

        //questo metodo invece ci serve per abilitare un listener quando clicchiamo il menu laterale
        navView.setNavigationItemSelectedListener(this)

        //SETTING SIGNOUT BUTTON
        headerView.findViewById<Button>(R.id.signout_button).setOnClickListener {
            Log.d("signout", "Inside the on click listener")
            signOut()
            Log.d("signout", "Now the user is: " + (auth.currentUser?.displayName ?: "nothing"))
            //redirect to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val drawerLayout: DrawerLayout = binding.drawerLayout

        if(item.itemId == R.id.nav_adv_list){
            //This if is only a reminder that this is the home of app
        }
        if(item.itemId == R.id.nav_show_profile){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_adv_list_to_nav_show_profile)
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)  || super.onSupportNavigateUp()

    }

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