package com.mbkm.telgo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var tvFullName: TextView
    private lateinit var tvNIK: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvBirthDate: TextView
    private lateinit var tvWitelRegion: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvUnit: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnEditProfile: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        tvFullName = findViewById(R.id.tvFullName)
        tvNIK = findViewById(R.id.tvNIK)

        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvUnit = findViewById(R.id.tvUnit)
        tvPosition = findViewById(R.id.tvPosition)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        // Load user data
        loadUserData()

        // Setup button click listener
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from EditProfileActivity
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Set email from auth
            tvEmail.text = currentUser.email

            // Get additional user data from Firestore
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Set user data from Firestore
                        val fullName = document.getString("fullName") ?: ""
                        val nik = document.getString("nik") ?: ""



                        val companyName = document.getString("companyName") ?: ""
                        val unit = document.getString("unit") ?: ""
                        val position = document.getString("position") ?: ""
                        val phone = document.getString("phone") ?: ""

                        tvFullName.text = if (fullName.isNotEmpty()) fullName else "Not set"
                        tvNIK.text = if (nik.isNotEmpty()) nik else "Not set"



                        tvCompanyName.text = if (companyName.isNotEmpty()) companyName else "Not set"
                        tvUnit.text = if (unit.isNotEmpty()) unit else "Not set"
                        tvPosition.text = if (position.isNotEmpty()) position else "Not set"
                        tvPhone.text = if (phone.isNotEmpty()) phone else "Not set"
                    } else {
                        // No document found
                        setDefaultValues()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    setDefaultValues()
                }
        } else {
            // User not logged in, redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setDefaultValues() {
        tvFullName.text = "Not set"
        tvNIK.text = "Not set"



        tvCompanyName.text = "Not set"
        tvUnit.text = "Not set"
        tvPosition.text = "Not set"
        tvPhone.text = "Not set"
    }
}