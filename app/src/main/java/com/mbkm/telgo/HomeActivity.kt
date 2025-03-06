package com.mbkm.telgo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnLogout: Button
    private lateinit var btnUploadProject: Button
    private lateinit var btnWitelSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI components
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        btnLogout = findViewById(R.id.btnLogout)
        btnUploadProject = findViewById(R.id.btnUploadProject)
        btnWitelSearch = findViewById(R.id.btnWitelSearch)

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        // Set default selected item
        bottomNavigationView.selectedItemId = R.id.navigation_home

        // Set up logout button click listener
        btnLogout.setOnClickListener {
            // For now, just go back to login screen
            // In a real app, you would clear user session data first
            val intent = Intent(this, LoginActivity::class.java)
            // Clear back stack so user can't go back to HomeActivity after logout
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Set up upload project button click listener
        btnUploadProject.setOnClickListener {
            val intent = Intent(this, UploadProjectActivity::class.java)
            startActivity(intent)
        }

        // Set up witel search button click listener
        btnWitelSearch.setOnClickListener {
            val intent = Intent(this, WitelSearchActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                // Already on home, do nothing
                return true
            }
            R.id.navigation_history -> {
                // To be implemented later - History feature
                // You can add code here to navigate to a History activity when it's implemented
                return true
            }
            R.id.navigation_account -> {
                // Navigate to ProfileActivity
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }
}