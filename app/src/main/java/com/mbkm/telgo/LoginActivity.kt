package com.mbkm.telgo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingOverlay: View
    private lateinit var logoImageView: ImageView
    private lateinit var ivShowPassword: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var telgoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        initializeViews()
        setupTextWatchers()

        // Animate entry
        animateEntrySequence()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        tvForgotPassword.setOnClickListener {
            animateButtonClick(it)
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        tvRegister.setOnClickListener {
            animateButtonClick(it)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Setup password visibility toggle
        ivShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun initializeViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        logoImageView = findViewById(R.id.logoImageView)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        welcomeText = findViewById(R.id.welcomeTextView)
        telgoText = findViewById(R.id.telgoTextView)
    }

    private fun setupTextWatchers() {
        etEmail.addTextChangedListener(createTextWatcher(tilEmail))
        etPassword.addTextChangedListener(createTextWatcher(tilPassword))
    }

    private fun createTextWatcher(textInputLayout: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun animateEntrySequence() {
        // Define animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply animations with sequence
        welcomeText.startAnimation(fadeIn)
        telgoText.startAnimation(fadeIn)

        logoImageView.alpha = 0f
        logoImageView.visibility = View.VISIBLE
        logoImageView.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        tilEmail.startAnimation(slideUp)
        tilPassword.startAnimation(slideUp)

        // Slightly delay other UI elements
        btnLogin.alpha = 0f
        btnLogin.visibility = View.VISIBLE
        btnLogin.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .start()

        tvForgotPassword.alpha = 0f
        tvForgotPassword.visibility = View.VISIBLE
        tvForgotPassword.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(500)
            .start()

        // Registration text animations
        val registerContainer = findViewById<LinearLayout>(R.id.registerContainer)
        registerContainer.alpha = 0f
        registerContainer.visibility = View.VISIBLE
        registerContainer.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(600)
            .start()
    }

    private fun animateButtonClick(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }.start()
    }

    private fun togglePasswordVisibility() {
        if (etPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivShowPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivShowPassword.setImageResource(R.drawable.ic_visibility_off)
        }
        etPassword.setSelection(etPassword.text?.length ?: 0)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            isValid = false
            shakeView(tilEmail)
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
            shakeView(tilPassword)
        }

        return isValid
    }

    private fun shakeView(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        view.startAnimation(animation)
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Update last login time and check user role
                        updateLastLoginAndCheckRole(user.uid)
                    } else {
                        showLoading(false)
                        showError("Login error: User not found")
                    }
                } else {
                    showLoading(false)
                    val errorMessage = task.exception?.message ?: "Login failed"
                    showError(errorMessage)
                    shakeView(btnLogin)
                }
            }
    }

    private fun updateLastLoginAndCheckRole(userId: String) {
        // Update last login timestamp
        val lastLoginUpdate = hashMapOf<String, Any>(
            "lastLoginDate" to Date().time
        )

        firestore.collection("users").document(userId)
            .update(lastLoginUpdate)
            .addOnSuccessListener {
                // Now check the user role and status
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        showLoading(false)

                        if (document != null && document.exists()) {
                            val role = document.getString("role") ?: "user"
                            val status = document.getString("status") ?: "unverified"

                            // Store role and status in SharedPreferences for easy access
                            val preferences = getSharedPreferences("TelGoPrefs", MODE_PRIVATE)
                            preferences.edit().apply {
                                putString("userRole", role)
                                putString("userStatus", status)
                                apply()
                            }

                            showSuccess("Login successful")

                            // Redirect based on role
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, ServicesActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                finish()
                            }, 1000)
                        } else {
                            showError("User data not found")
                        }
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        showError("Error checking user role: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                showError("Error updating login time: ${e.message}")
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingOverlay.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE

            // Fade in animation
            loadingOverlay.alpha = 0f
            loadingOverlay.animate()
                .alpha(0.7f)
                .setDuration(300)
                .start()

            btnLogin.isEnabled = false
        } else {
            // Fade out animation
            loadingOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    loadingOverlay.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
                .start()
        }
    }

    private fun showSuccess(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.green))
        snackbar.show()
    }

    private fun showError(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.red))
        snackbar.show()
    }
}