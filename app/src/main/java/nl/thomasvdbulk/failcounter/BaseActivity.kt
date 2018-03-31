package nl.thomasvdbulk.failcounter

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_base.*
import nl.thomasvdbulk.failcounter.entity.User

abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()


        fillSliderInfo()

        FirebaseAuth.getInstance().addAuthStateListener {
            fillSliderInfo()
        }

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun fillSliderInfo(){
        if(FirebaseAuth.getInstance().currentUser != null){
            User.onCurrentUser(this){
                drawer_layout.findViewById<TextView>(R.id.slider_username).text = it.name
                drawer_layout.findViewById<TextView>(R.id.slider_email).text = it.email
            }
        } else {
            drawer_layout.findViewById<TextView>(R.id.slider_username).text = getString(R.string.anonymous)
            drawer_layout.findViewById<TextView>(R.id.slider_email).text = ""
        }
    }

    override fun setContentView(layoutResID: Int) {
        val base = findViewById<FrameLayout>(R.id.base_view)
        layoutInflater.inflate(layoutResID, base, true)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.base, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
