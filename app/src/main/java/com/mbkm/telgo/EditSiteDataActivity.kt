package com.mbkm.telgo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.webkit.MimeTypeMap



class EditSiteDataActivity : AppCompatActivity() {

    // Original UI Components
    private lateinit var tvSiteId: TextView
    private lateinit var tvWitel: TextView
    private lateinit var tvStatus: TextView
    private lateinit var etLastIssue: EditText
    private lateinit var tvKoordinat: TextView

    private lateinit var btnUploadDaftarMitra: Button
    private lateinit var tvDaftarMitraFileName: TextView

    private lateinit var btnUploadEmailOrder: Button
    private lateinit var btnUploadTelkomselPermit: Button
    private lateinit var btnUploadTelPartner: Button

    private lateinit var btnCaptureLocation: Button
    private lateinit var btnCaptureFoundation: Button
    private lateinit var btnCaptureInstallation: Button
    private lateinit var btnCaptureCabinet: Button
    private lateinit var btnCaptureInet: Button
    private lateinit var btnCaptureUctv: Button
    private lateinit var btnCaptureTelephone: Button

    private lateinit var ivLocation: ImageView
    private lateinit var ivFoundation: ImageView
    private lateinit var ivInstallation: ImageView
    private lateinit var ivCabinet: ImageView
    private lateinit var ivInet: ImageView
    private lateinit var ivUctv: ImageView
    private lateinit var ivTelephone: ImageView

    private lateinit var btnSaveChanges: Button
    private lateinit var btnCancel: Button

    private lateinit var tvEmailOrderFileName: TextView
    private lateinit var tvTelkomselPermitFileName: TextView
    private lateinit var tvTelPartnerFileName: TextView

    // New UI Components
    private lateinit var btnToggleUpdateFields: Button
    private lateinit var cardUpdateFields: CardView
    private lateinit var dropdownStatus: AutoCompleteTextView
    private lateinit var dropdownKendala: AutoCompleteTextView
    private lateinit var etTglPlanOa: TextInputEditText

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Data
    private lateinit var siteId: String
    private lateinit var witel: String
    private var documentId: String = ""
    private var isUpdateFieldsVisible = false

    // Status and Kendala options
    private val statusOptions = listOf(
        "OA", "MAT DEL", "DONE", "SURVEY", "POWER ON",
        "DROP", "MOS", "INTEGRASI", "DONE SURVEY", "DONE UT"
    )

    private val kendalaOptions = listOf(
        "COMMCASE", "NEW PLN", "NO ISSUE", "PERMIT", "PONDASI",
        "RELOC", "SFP BIDI", "UPGRADE PLN", "WAITING OTN", "WAITING UPLINK"
    )

    // Request codes
    private val REQUEST_CAMERA_PERMISSION = 101
    private val REQUEST_IMAGE_CAPTURE = 102
    private val REQUEST_DOCUMENT_PICK = 103

    // Currently selected image type for camera
    private var currentImageType: String = ""

    // File URIs
    private val documentUris = mutableMapOf<String, Uri>()
    private val imageUris = mutableMapOf<String, Uri>()
    private var currentPhotoPath: String = ""

    // Calendar for date picker
    private val calendar = Calendar.getInstance()

    // Tambahkan variabel untuk menyimpan nilai awal field
    private var initialStatus = ""
    private var initialKendala = ""
    private var initialLastIssue = ""
    private var initialTglPlanOa = ""

    // Tambahkan variable untuk menandai jika pernah menekan toggle
    private var hasToggledUpdateFields = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_site_data)

        // Get data from intent
        siteId = intent.getStringExtra("SITE_ID") ?: ""
        witel = intent.getStringExtra("WITEL") ?: ""

        if (siteId.isEmpty() || witel.isEmpty()) {
            showToast("Invalid site information")
            finish()
            return
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI components
        initializeViews()

        // Set up adapters for dropdowns
        setupDropdowns()

        // Set up date picker
        setupDatePicker()

        // Set up button listeners
        setupButtonListeners()

        // Load site data
        loadSiteData()

        // Set up button listeners
        setupButtonListeners()

        // Load site data
        loadSiteData()

    }

    private fun initializeViews() {
        // Original views
        tvSiteId = findViewById(R.id.tvSiteId)
        tvWitel = findViewById(R.id.tvWitel)
        tvStatus = findViewById(R.id.tvStatus)
        etLastIssue = findViewById(R.id.etLastIssue)
        tvKoordinat = findViewById(R.id.tvKoordinat)

        btnUploadEmailOrder = findViewById(R.id.btnUploadEmailOrder)
        btnUploadTelkomselPermit = findViewById(R.id.btnUploadTelkomselPermit)
        btnUploadTelPartner = findViewById(R.id.btnUploadTelPartner)

        tvEmailOrderFileName = findViewById(R.id.tvEmailOrderFileName)
        tvTelkomselPermitFileName = findViewById(R.id.tvTelkomselPermitFileName)
        tvTelPartnerFileName = findViewById(R.id.tvTelPartnerFileName)

        btnCaptureLocation = findViewById(R.id.btnCaptureLocation)
        btnCaptureFoundation = findViewById(R.id.btnCaptureFoundation)
        btnCaptureInstallation = findViewById(R.id.btnCaptureInstallation)
        btnCaptureCabinet = findViewById(R.id.btnCaptureCabinet)
        btnCaptureInet = findViewById(R.id.btnCaptureInet)
        btnCaptureUctv = findViewById(R.id.btnCaptureUctv)
        btnCaptureTelephone = findViewById(R.id.btnCaptureTelephone)

        ivLocation = findViewById(R.id.ivLocation)
        ivFoundation = findViewById(R.id.ivFoundation)
        ivInstallation = findViewById(R.id.ivInstallation)
        ivCabinet = findViewById(R.id.ivCabinet)
        ivInet = findViewById(R.id.ivInet)
        ivUctv = findViewById(R.id.ivUctv)
        ivTelephone = findViewById(R.id.ivTelephone)

        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnCancel = findViewById(R.id.btnCancel)

        // New views
        btnToggleUpdateFields = findViewById(R.id.btnToggleUpdateFields)
        cardUpdateFields = findViewById(R.id.cardUpdateFields)
        dropdownStatus = findViewById(R.id.dropdownStatus)
        dropdownKendala = findViewById(R.id.dropdownKendala)
        etTglPlanOa = findViewById(R.id.etTglPlanOa)

        btnUploadDaftarMitra = findViewById(R.id.btnUploadDaftarMitra)
        tvDaftarMitraFileName = findViewById(R.id.tvDaftarMitraFileName)
    }

    private fun setupDropdowns() {
        // Set up Status dropdown
        val statusAdapter = ArrayAdapter(this, R.layout.dropdown_item, statusOptions)
        dropdownStatus.setAdapter(statusAdapter)

        // Set up Kendala dropdown
        val kendalaAdapter = ArrayAdapter(this, R.layout.dropdown_item, kendalaOptions)
        dropdownKendala.setAdapter(kendalaAdapter)
    }

    // Ganti fungsi calculateIdLopOlt dengan fungsi berikut:
    private fun calculateIdLopOlt(
        platform: String?,
        kontrakPengadaan: String?,
        kodeSto: String?,
        sizeOlt: String?,
        jmlModul: String?,
        siteId: String?,
        kodeIhld: String?,
        status: String?
    ): String {
        // Formula: platform/kontrakPengadaan(12 char)/kodeSto/sizeOlt/jmlModul/siteId/kodeIhld==>status

        if (platform.isNullOrEmpty() || kodeSto.isNullOrEmpty() || sizeOlt.isNullOrEmpty() ||
            jmlModul.isNullOrEmpty() || siteId.isNullOrEmpty() || kodeIhld.isNullOrEmpty() || status.isNullOrEmpty()) {
            return ""
        }

        val kontrakPrefix = if (!kontrakPengadaan.isNullOrEmpty()) {
            if (kontrakPengadaan.length > 12) kontrakPengadaan.substring(0, 12) else kontrakPengadaan
        } else ""

        return "$platform/$kontrakPrefix/$kodeSto/$sizeOlt/$jmlModul/$siteId/$kodeIhld==>$status"
    }

    private fun setupDatePicker() {
        etTglPlanOa.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(calendar.time)
            etTglPlanOa.setText(dateStr)
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupButtonListeners() {
        // Tambahkan logika untuk mencatat jika toggle ditekan
        btnToggleUpdateFields.setOnClickListener {
            isUpdateFieldsVisible = !isUpdateFieldsVisible
            hasToggledUpdateFields = true
            cardUpdateFields.visibility = if (isUpdateFieldsVisible) View.VISIBLE else View.GONE
        }

        // Toggle update fields button
        btnToggleUpdateFields.setOnClickListener {
            isUpdateFieldsVisible = !isUpdateFieldsVisible
            cardUpdateFields.visibility = if (isUpdateFieldsVisible) View.VISIBLE else View.GONE
        }

        // Original document upload buttons
        btnUploadEmailOrder.setOnClickListener { openDocumentPicker("email_order") }
        btnUploadTelkomselPermit.setOnClickListener { openDocumentPicker("telkomsel_permit") }
        btnUploadTelPartner.setOnClickListener { openDocumentPicker("mitra_tel") }
        btnUploadDaftarMitra.setOnClickListener { openDocumentPicker("daftar_mitra") }

        // Image capture buttons
        btnCaptureLocation.setOnClickListener { captureImage("site_location") }
        btnCaptureFoundation.setOnClickListener { captureImage("foundation_shelter") }
        btnCaptureInstallation.setOnClickListener { captureImage("installation_process") }
        btnCaptureCabinet.setOnClickListener { captureImage("cabinet") }
        btnCaptureInet.setOnClickListener { captureImage("3p_inet") }
        btnCaptureUctv.setOnClickListener { captureImage("3p_useetv") }
        btnCaptureTelephone.setOnClickListener { captureImage("3p_telephone") }

        // Save & Cancel buttons
        btnSaveChanges.setOnClickListener { saveChanges() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun loadSiteData() {
        firestore.collection("projects")
            .whereEqualTo("siteId", siteId)
            .whereEqualTo("witel", witel)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showToast("Site not found")
                    finish()
                    return@addOnSuccessListener
                }

                val document = documents.documents[0]
                documentId = document.id
                val site = document.data

                // Set basic site information
                tvSiteId.text = siteId
                tvWitel.text = witel
                tvStatus.text = site?.get("status").toString()
                tvKoordinat.text = site?.get("koordinat").toString()

                // Set last issue if available
//                val lastIssueHistory = site?.get("lastIssueHistory") as? List<String>
//                if (!lastIssueHistory.isNullOrEmpty()) {
//                    // Extract just the content part after the timestamp
//                    val lastIssue = lastIssueHistory[0]
//                    val parts = lastIssue.split(" - ", limit = 2)
//                    if (parts.size > 1) {
//                        etLastIssue.setText(parts[1])
//                    } else {
//                        etLastIssue.setText(lastIssue)
//                    }
//                }

                // Set values for new editable fields
                // Simpan nilai awal dari field-field yang bisa diubah
                site?.get("status")?.toString()?.let {
                    initialStatus = it
                    if (it.isNotEmpty()) {
                        dropdownStatus.setText(it, false)
                    }
                }

                site?.get("kendala")?.toString()?.let {
                    initialKendala = it
                    if (it.isNotEmpty()) {
                        dropdownKendala.setText(it, false)
                    }
                }

                val lastIssueHistory = site?.get("lastIssueHistory") as? List<String>
                if (!lastIssueHistory.isNullOrEmpty()) {
                    // Extract just the content part after the timestamp
                    val lastIssue = lastIssueHistory[0]
                    val parts = lastIssue.split(" - ", limit = 2)
                    if (parts.size > 1) {
                        initialLastIssue = parts[1]
                        etLastIssue.setText(parts[1])
                    } else {
                        initialLastIssue = lastIssue
                        etLastIssue.setText(lastIssue)
                    }
                }

                site?.get("tglPlanOa")?.toString()?.let {
                    initialTglPlanOa = it
                    if (it.isNotEmpty()) {
                        etTglPlanOa.setText(it)

                        // Also set the calendar to this date for the date picker
                        try {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = dateFormat.parse(it)
                            if (date != null) {
                                calendar.time = date
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Load existing documents and images from storage
                loadExistingFiles()
            }
            .addOnFailureListener { e ->
                showToast("Error loading site: ${e.message}")
            }
    }

    private fun updateWeekPlanOa(dateStr: String): String {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateStr)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date

                val month = when (calendar.get(Calendar.MONTH)) {
                    Calendar.JANUARY -> "Jan"
                    Calendar.FEBRUARY -> "Feb"
                    Calendar.MARCH -> "Mar"
                    Calendar.APRIL -> "Apr"
                    Calendar.MAY -> "May"
                    Calendar.JUNE -> "Jun"
                    Calendar.JULY -> "Jul"
                    Calendar.AUGUST -> "Aug"
                    Calendar.SEPTEMBER -> "Sep"
                    Calendar.OCTOBER -> "Oct"
                    Calendar.NOVEMBER -> "Nov"
                    Calendar.DECEMBER -> "Dec"
                    else -> ""
                }

                // Calculate week of month (W1, W2, W3, W4, W5)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val weekOfMonth = when {
                    dayOfMonth <= 7 -> "W1"
                    dayOfMonth <= 14 -> "W2"
                    dayOfMonth <= 21 -> "W3"
                    dayOfMonth <= 28 -> "W4"
                    else -> "W5"
                }

                // Format "Mar W2"
                return "$month $weekOfMonth"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    private fun loadExistingFiles() {
        // Load existing documents
        val documentTypes = listOf(
            "email_order" to tvEmailOrderFileName,
            "telkomsel_permit" to tvTelkomselPermitFileName,
            "mitra_tel" to tvTelPartnerFileName, // Changed from "tel_partner"
            "daftar_mitra" to tvDaftarMitraFileName // Added new document type
        )

        for ((docType, textView) in documentTypes) {
            val docRef = storage.reference.child("documents/$witel/$siteId/$docType.pdf")
            docRef.downloadUrl
                .addOnSuccessListener { uri ->
                    // Mark document as existing
                    updateDocumentButtonStatus(docType, true)

                    // Display filename (using the document type as a fallback)
                    val fileName = "$docType.pdf"
                    textView.text = fileName
                    textView.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    // Document doesn't exist
                    updateDocumentButtonStatus(docType, false)
                    textView.visibility = View.GONE
                }
        }

        // Load existing images
        val imageTypes = listOf(
            "site_location" to ivLocation,
            "foundation_shelter" to ivFoundation,
            "installation_process" to ivInstallation,
            "cabinet" to ivCabinet,
            "3p_inet" to ivInet,
            "3p_useetv" to ivUctv, // Changed from "3p_uctv"
            "3p_telephone" to ivTelephone
        )

        for ((imageType, imageView) in imageTypes) {
            val imageRef = storage.reference.child("images/$witel/$siteId/$imageType.jpg")
            imageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    // Image exists, load it into ImageView
                    loadImageIntoView(uri, imageView)
                    updateImageButtonStatus(imageType, true)
                }
                .addOnFailureListener {
                    // Image doesn't exist
                    updateImageButtonStatus(imageType, false)
                }
        }
    }

    private fun updateDocumentButtonStatus(docType: String, exists: Boolean) {
        val button = when (docType) {
            "email_order" -> btnUploadEmailOrder
            "telkomsel_permit" -> btnUploadTelkomselPermit
            "mitra_tel" -> btnUploadTelPartner  // Perbaikan: Menggunakan string baru "mitra_tel"
            "daftar_mitra" -> btnUploadDaftarMitra  // Penambahan: Support untuk dokumen baru
            else -> return
        }

        button.text = if (exists) "Replace Document" else "Upload Document"
    }

    private fun updateImageButtonStatus(imageType: String, exists: Boolean) {
        val button = when (imageType) {
            "site_location" -> btnCaptureLocation
            "foundation_shelter" -> btnCaptureFoundation
            "installation_process" -> btnCaptureInstallation
            "cabinet" -> btnCaptureCabinet
            "3p_inet" -> btnCaptureInet
            "3p_useetv" -> btnCaptureUctv // FIXED: Use the new name
            "3p_telephone" -> btnCaptureTelephone
            else -> return
        }

        button.text = if (exists) "Replace Image" else "Capture Image"
    }

    private fun loadImageIntoView(uri: Uri, imageView: ImageView) {
        try {
            // For images from camera, we need to handle the file:// URI differently
            if (uri.scheme == "file") {
                val bitmap = BitmapFactory.decodeFile(uri.path)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                } else {
                    showToast("Error loading image: Could not decode bitmap")
                }
            } else {
                // For content:// URIs (from storage)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
//            showToast("Error loading image: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun openDocumentPicker(docType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // Support untuk PDF, Word, dan Excel
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }

        // Store the document type to use when handling the result
        currentImageType = docType
        startActivityForResult(intent, REQUEST_DOCUMENT_PICK)
    }

    private fun captureImage(imageType: String) {
        if (checkCameraPermission()) {
            // Store the image type to use when handling the result
            currentImageType = imageType

            // Create file for the photo
            val photoFile = createImageFile()
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.mbkm.telgo.fileprovider",
                    photoFile
                )

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )

            // Save the file path for use with the camera
            currentPhotoPath = image.absolutePath
            return image
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, capture image
                    captureImage(currentImageType)
                } else {
                    showToast("Camera permission is required to capture images")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                // Image captured from camera
                if (currentPhotoPath.isNotEmpty()) {
                    // Create URI from file path
                    val photoFile = File(currentPhotoPath)
                    val photoUri = Uri.fromFile(photoFile)

                    // Store the URI for later upload
                    imageUris[currentImageType] = photoUri

                    // Display the captured image in the appropriate ImageView
                    val imageView = when (currentImageType) {
                        "site_location" -> ivLocation
                        "foundation_shelter" -> ivFoundation
                        "installation_process" -> ivInstallation
                        "cabinet" -> ivCabinet
                        "3p_inet" -> ivInet
                        "3p_useetv" -> ivUctv  // PERBAIKAN: Gunakan string baru "3p_useetv"
                        "3p_telephone" -> ivTelephone
                        else -> null
                    }

                    imageView?.let {
                        // Load and display the image
                        try {
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            it.setImageBitmap(bitmap)
                            it.visibility = View.VISIBLE
                        }
                        catch (e: Exception) {
                            showToast("Error displaying image: ${e.message}")
                            e.printStackTrace()
                        }

                        // Update button text to indicate replacement is possible
                        updateImageButtonStatus(currentImageType, true)
                    }

                    showToast("Image captured successfully")
                } else {
                    showToast("Error: Image file not created")
                }
            }

            REQUEST_DOCUMENT_PICK -> {
                // Document selected
                data?.data?.let { uri ->
                    // Take a persistent URI permission
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    // Store the URI for later upload
                    documentUris[currentImageType] = uri

                    // Get the filename and mime type from the URI
                    val fileName = getFileNameFromUri(uri)
                    val mimeType = contentResolver.getType(uri) ?: "application/pdf"

                    // Update the corresponding TextView based on the document type
                    when (currentImageType) {
                        "email_order" -> {
                            tvEmailOrderFileName.text = fileName
                            tvEmailOrderFileName.visibility = View.VISIBLE
                        }
                        "telkomsel_permit" -> {
                            tvTelkomselPermitFileName.text = fileName
                            tvTelkomselPermitFileName.visibility = View.VISIBLE
                        }
                        "mitra_tel" -> {
                            tvTelPartnerFileName.text = fileName
                            tvTelPartnerFileName.visibility = View.VISIBLE
                        }
                        "daftar_mitra" -> {
                            tvDaftarMitraFileName.text = fileName
                            tvDaftarMitraFileName.visibility = View.VISIBLE
                        }
                    }

                    // Update button text
                    updateDocumentButtonStatus(currentImageType, true)

                    showToast("Document selected: $fileName")
                }
            }
        }
    }

    // Add this helper method to get the filename from a URI
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "Unknown file"

        // Try to get the display name from content provider
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }

        return fileName
    }

    // In the saveChanges() method, add the current user tracking

    private fun saveChanges() {
        // Get current user before proceeding
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            showToast("Anda harus login terlebih dahulu")
            return
        }

        val userEmail = currentUser.email ?: "unknown"
        val lastIssue = etLastIssue.text.toString().trim()

        // Check if fields are updated
        val currentStatus = dropdownStatus.text.toString().trim()
        val currentKendala = dropdownKendala.text.toString().trim()
        val currentTglPlanOa = etTglPlanOa.text.toString().trim()

        val isStatusChanged = currentStatus.isNotEmpty() && currentStatus != initialStatus
        val isKendalaChanged = currentKendala.isNotEmpty() && currentKendala != initialKendala
        val isTglPlanOaChanged = currentTglPlanOa.isNotEmpty() && currentTglPlanOa != initialTglPlanOa
        val isIssueUpdated = lastIssue.isNotEmpty() && lastIssue != initialLastIssue

        // PERBAIKAN: Cek apakah ada field yang berubah (tanpa mempertimbangkan toggle)
        val anyFieldsChanged = isStatusChanged || isKendalaChanged || isTglPlanOaChanged || isIssueUpdated

        // Disable save button to prevent multiple clicks
        btnSaveChanges.isEnabled = false

        // PERBAIKAN: Jika ada field yang berubah, lakukan update untuk semua field
        if (anyFieldsChanged) {
            // Membuat Map untuk update data
            val updateMap = mutableMapOf<String, Any>()
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Tambahkan nilai yang berubah ke updateMap
            if (isStatusChanged) {
                updateMap["status"] = currentStatus
            }

            if (isKendalaChanged) {
                updateMap["kendala"] = currentKendala
            }

            if (isTglPlanOaChanged) {
                updateMap["tglPlanOa"] = currentTglPlanOa

                // Calculate weekPlanOa from tglPlanOa
                val weekPlanOa = updateWeekPlanOa(currentTglPlanOa)
                if (weekPlanOa.isNotEmpty()) {
                    updateMap["weekPlanOa"] = weekPlanOa
                }

                // Calculate sisaHariThdpPlanOa
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val planDate = dateFormat.parse(currentTglPlanOa)
                    val currentDate = Date()

                    if (planDate != null) {
                        val diffInMillis = planDate.time - currentDate.time
                        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

                        updateMap["sisaHariThdpPlanOa"] = diffInDays.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Tambahkan metadata update
            updateMap["updatedAt"] = currentTime
            updateMap["lastUpdatedBy"] = userEmail

            // Jika status berubah, perbarui idLopOlt
            if (isStatusChanged) {
                firestore.collection("projects").document(documentId)
                    .get()
                    .addOnSuccessListener { document ->
                        val data = document.data
                        if (data != null) {
                            // Ambil semua field yang diperlukan untuk perhitungan ID LOP OLT
                            val platform = data["platform"] as? String
                            val kontrakPengadaan = data["kontrakPengadaan"] as? String
                            val kodeSto = data["kodeSto"] as? String
                            val sizeOlt = data["sizeOlt"] as? String
                            val jmlModul = data["jmlModul"] as? String
                            val kodeIhld = data["kodeIhld"] as? String

                            // Hitung ID LOP OLT
                            val idLopOlt = calculateIdLopOlt(
                                platform,
                                kontrakPengadaan,
                                kodeSto,
                                sizeOlt,
                                jmlModul,
                                siteId,
                                kodeIhld,
                                currentStatus
                            )

                            // Tambahkan ke updateMap jika berhasil dihitung
                            if (idLopOlt.isNotEmpty()) {
                                updateMap["idLopOlt"] = idLopOlt
                            }

                            // Lanjutkan dengan updateLastIssue dan finalisasi
                            continueWithUpdates(userEmail, lastIssue, updateMap, isIssueUpdated)
                        } else {
                            // Lanjutkan tanpa mengupdate idLopOlt
                            continueWithUpdates(userEmail, lastIssue, updateMap, isIssueUpdated)
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error retrieving site data: ${e.message}")
                        btnSaveChanges.isEnabled = true
                    }
            } else {
                // Tidak perlu mengambil data untuk idLopOlt
                continueWithUpdates(userEmail, lastIssue, updateMap, isIssueUpdated)
            }
        } else {
            // Jika tidak ada field yang berubah, cukup upload file
            uploadAllFiles {
                // Update user's edit history
                updateUserEditHistory(userEmail, siteId) {
                    showToast("No data changes detected. Files uploaded successfully")
                    finish()
                }
            }
        }
    }

    // TAMBAHAN: Metode baru untuk melanjutkan update setelah idLopOlt diproses
    private fun continueWithUpdates(userEmail: String, lastIssue: String, updateMap: MutableMap<String, Any>, isIssueUpdated: Boolean) {
        if (isIssueUpdated) {
            // Update lastIssue dan sisanya dalam satu operasi
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val issueWithTimestamp = "$currentTime - $lastIssue"

            // Get existing issue history
            firestore.collection("projects").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    val existingData = document.data
                    val existingIssueHistory = existingData?.get("lastIssueHistory") as? List<String> ?: listOf()

                    // Create updated issue history with new issue at the beginning
                    val updatedIssueHistory = mutableListOf(issueWithTimestamp)
                    updatedIssueHistory.addAll(existingIssueHistory)

                    // Tambahkan lastIssueHistory ke updateMap
                    updateMap["lastIssueHistory"] = updatedIssueHistory

                    // Update semua data sekaligus
                    updateAllData(userEmail, updateMap)
                }
                .addOnFailureListener { e ->
                    showToast("Error retrieving site data: ${e.message}")
                    btnSaveChanges.isEnabled = true
                }
        } else {
            // Tidak ada perubahan issue, langsung update data lainnya
            updateAllData(userEmail, updateMap)
        }
    }

    // TAMBAHAN: Metode untuk update semua data sekaligus
    private fun updateAllData(userEmail: String, updateMap: MutableMap<String, Any>) {
        // Update in Firestore
        firestore.collection("projects").document(documentId)
            .update(updateMap)
            .addOnSuccessListener {
                // Upload file dan update history
                uploadAllFiles {
                    updateUserEditHistory(userEmail, siteId) {
                        showToast("All changes saved successfully")
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error updating site data: ${e.message}")
                btnSaveChanges.isEnabled = true
            }
    }


    // Modify updateLastIssue to include the user email
    private fun updateLastIssue(userEmail: String, onComplete: () -> Unit) {
        if (documentId.isEmpty()) {
            showToast("Error: Site document ID not found")
            btnSaveChanges.isEnabled = true
            return
        }

        val lastIssue = etLastIssue.text.toString().trim()
        if (lastIssue.isEmpty()) {
            onComplete() // Skip if empty
            return
        }

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val issueWithTimestamp = "$currentTime - $lastIssue"

        // Get existing issue history
        firestore.collection("projects").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val existingData = document.data
                val existingIssueHistory = existingData?.get("lastIssueHistory") as? List<String> ?: listOf()

                // Create updated issue history with new issue at the beginning
                val updatedIssueHistory = mutableListOf(issueWithTimestamp)
                updatedIssueHistory.addAll(existingIssueHistory)

                // Update in Firestore with the user email
                firestore.collection("projects").document(documentId)
                    .update(
                        mapOf(
                            "lastIssueHistory" to updatedIssueHistory,
                            "updatedAt" to currentTime,
                            "lastUpdatedBy" to userEmail  // Add this line to track who made the update
                        )
                    )
                    .addOnSuccessListener {
                        // Continue with next step
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error updating issue: ${e.message}")
                        btnSaveChanges.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showToast("Error retrieving site data: ${e.message}")
                btnSaveChanges.isEnabled = true
            }
    }

    // Modify updateSiteData to include the user email
    private fun updateSiteData(userEmail: String, onComplete: () -> Unit) {
        if (documentId.isEmpty()) {
            showToast("Error: Site document ID not found")
            btnSaveChanges.isEnabled = true
            return
        }

        // Get values from new fields
        val status = dropdownStatus.text.toString().trim()
        val kendala = dropdownKendala.text.toString().trim()
        val tglPlanOa = etTglPlanOa.text.toString().trim()

        // Create update map with only non-empty fields
        val updateMap = mutableMapOf<String, Any>()

        // Always add the user who made this update
        updateMap["lastUpdatedBy"] = userEmail

        if (status.isNotEmpty() && status != initialStatus) {
            updateMap["status"] = status

            // Ambil data yang diperlukan dari Firestore untuk menghitung ID LOP OLT
            firestore.collection("projects").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    if (data != null) {
                        // Ambil semua field yang diperlukan untuk perhitungan ID LOP OLT
                        val platform = data["platform"] as? String
                        val kontrakPengadaan = data["kontrakPengadaan"] as? String
                        val kodeSto = data["kodeSto"] as? String
                        val sizeOlt = data["sizeOlt"] as? String
                        val jmlModul = data["jmlModul"] as? String
                        val kodeIhld = data["kodeIhld"] as? String

                        // Hitung ID LOP OLT dengan formula yang benar
                        val idLopOlt = calculateIdLopOlt(
                            platform,
                            kontrakPengadaan,
                            kodeSto,
                            sizeOlt,
                            jmlModul,
                            siteId,
                            kodeIhld,
                            status
                        )

                        // Tambahkan ke updateMap jika berhasil dihitung
                        if (idLopOlt.isNotEmpty()) {
                            updateMap["idLopOlt"] = idLopOlt
                        }

                        // Lanjutkan dengan kode yang sudah ada
                        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
                    } else {
                        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error retrieving site data: ${e.message}")
                    btnSaveChanges.isEnabled = true
                }
            return // Return early since we're handling in the callback
        }

        // If status is not updated, continue with other updates
        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
    }

    // Add a new function to update the user's edit history
    private fun updateUserEditHistory(userEmail: String, siteId: String, onComplete: () -> Unit) {
        // Get user document by email
        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // User document not found, complete anyway
                    onComplete()
                    return@addOnSuccessListener
                }

                val userDoc = documents.documents[0]
                val userId = userDoc.id

                // Get current list of edited sites or create new one
                var editedSites = userDoc.get("editedSites") as? MutableList<String> ?: mutableListOf()

                // Check if this site is already in the list
                if (!editedSites.contains(siteId)) {
                    // Add site if not already in the list
                    editedSites.add(siteId)

                    // Update user document with the new list
                    firestore.collection("users").document(userId)
                        .update("editedSites", editedSites)
                        .addOnSuccessListener {
                            // Continue to next step
                            onComplete()
                        }
                        .addOnFailureListener { e ->
                            // Log error but continue anyway to not block the flow
                            Log.e("EditSiteActivity", "Error updating user's edit history: ${e.message}")
                            onComplete()
                        }
                } else {
                    // Site already in the list, just complete
                    onComplete()
                }
            }
            .addOnFailureListener { e ->
                // Log error but continue anyway to not block the flow
                Log.e("EditSiteActivity", "Error finding user document: ${e.message}")
                onComplete()
            }
    }

    private fun updateLastIssue(onComplete: () -> Unit) {
        if (documentId.isEmpty()) {
            showToast("Error: Site document ID not found")
            btnSaveChanges.isEnabled = true
            return
        }

        val lastIssue = etLastIssue.text.toString().trim()
        if (lastIssue.isEmpty()) {
            onComplete() // Skip if empty
            return
        }

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val issueWithTimestamp = "$currentTime - $lastIssue"

        // Get existing issue history
        firestore.collection("projects").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val existingData = document.data
                val existingIssueHistory = existingData?.get("lastIssueHistory") as? List<String> ?: listOf()

                // Create updated issue history with new issue at the beginning
                val updatedIssueHistory = mutableListOf(issueWithTimestamp)
                updatedIssueHistory.addAll(existingIssueHistory)

                // Update in Firestore
                firestore.collection("projects").document(documentId)
                    .update(
                        mapOf(
                            "lastIssueHistory" to updatedIssueHistory,
                            "updatedAt" to currentTime
                        )
                    )
                    .addOnSuccessListener {
                        // Continue with next step
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error updating issue: ${e.message}")
                        btnSaveChanges.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showToast("Error retrieving site data: ${e.message}")
                btnSaveChanges.isEnabled = true
            }
    }

    private fun updateSiteData(onComplete: () -> Unit) {
        if (documentId.isEmpty()) {
            showToast("Error: Site document ID not found")
            btnSaveChanges.isEnabled = true
            return
        }

        // Get values from new fields
        val status = dropdownStatus.text.toString().trim()
        val kendala = dropdownKendala.text.toString().trim()
        val tglPlanOa = etTglPlanOa.text.toString().trim()

        // Create update map with only non-empty fields
        val updateMap = mutableMapOf<String, Any>()

        if (status.isNotEmpty()) {
            updateMap["status"] = status

            // Ambil data yang diperlukan dari Firestore untuk menghitung ID LOP OLT
            firestore.collection("projects").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    if (data != null) {
                        // Ambil semua field yang diperlukan untuk perhitungan ID LOP OLT
                        val platform = data["platform"] as? String
                        val kontrakPengadaan = data["kontrakPengadaan"] as? String
                        val kodeSto = data["kodeSto"] as? String
                        val sizeOlt = data["sizeOlt"] as? String
                        val jmlModul = data["jmlModul"] as? String
                        val kodeIhld = data["kodeIhld"] as? String

                        // Hitung ID LOP OLT dengan formula yang benar
                        val idLopOlt = calculateIdLopOlt(
                            platform,
                            kontrakPengadaan,
                            kodeSto,
                            sizeOlt,
                            jmlModul,
                            siteId,
                            kodeIhld,
                            status
                        )

                        // Tambahkan ke updateMap jika berhasil dihitung
                        if (idLopOlt.isNotEmpty()) {
                            updateMap["idLopOlt"] = idLopOlt
                        }

                        // Lanjutkan dengan kode yang sudah ada
                        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
                    } else {
                        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error retrieving site data: ${e.message}")
                    btnSaveChanges.isEnabled = true
                }
            return // Return early since we're handling in the callback
        }

        // If status is not updated, continue with other updates
        completeUpdateProcess(updateMap, kendala, tglPlanOa, onComplete)
    }

    // Helper method to complete the update process
    private fun completeUpdateProcess(updateMap: MutableMap<String, Any>, kendala: String, tglPlanOa: String, onComplete: () -> Unit) {
        // Add kendala if not empty and changed
        if (kendala.isNotEmpty() && kendala != initialKendala) {
            updateMap["kendala"] = kendala
        }

        // Process tglPlanOa
        if (tglPlanOa.isNotEmpty() && tglPlanOa != initialTglPlanOa) {
            updateMap["tglPlanOa"] = tglPlanOa

            // Calculate weekPlanOa from tglPlanOa using our new function
            val weekPlanOa = updateWeekPlanOa(tglPlanOa)
            if (weekPlanOa.isNotEmpty()) {
                updateMap["weekPlanOa"] = weekPlanOa
            }

            // Calculate sisaHariThdpPlanOa
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val planDate = dateFormat.parse(tglPlanOa)
                val currentDate = Date()

                if (planDate != null) {
                    val diffInMillis = planDate.time - currentDate.time
                    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

                    updateMap["sisaHariThdpPlanOa"] = diffInDays.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If no fields to update, skip to next step
        if (updateMap.isEmpty() || updateMap.size == 1 && updateMap.containsKey("lastUpdatedBy")) {
            onComplete()
            return
        }

        // Add updatedAt field
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        updateMap["updatedAt"] = currentTime

        // Update in Firestore
        firestore.collection("projects").document(documentId)
            .update(updateMap)
            .addOnSuccessListener {
                // Continue to next step
                onComplete()
            }
            .addOnFailureListener { e ->
                showToast("Error updating site data: ${e.message}")
                btnSaveChanges.isEnabled = true
            }
    }

    private fun uploadAllFiles(onComplete: () -> Unit) {
        // Disable the save button to prevent multiple clicks
        btnSaveChanges.isEnabled = false

        var pendingUploads = documentUris.size + imageUris.size

        // If no files to upload, call completion handler immediately
        if (pendingUploads == 0) {
            onComplete()
            return
        }

        val onFileUploadComplete = {
            pendingUploads--
            if (pendingUploads <= 0) {
                onComplete()
            }
        }

        // Upload documents
        for ((docType, uri) in documentUris) {
            uploadDocument(docType, uri, onFileUploadComplete)
        }

        // Upload images
        for ((imageType, uri) in imageUris) {
            uploadImage(imageType, uri, onFileUploadComplete)
        }
    }

    private fun uploadDocument(docType: String, uri: Uri, onComplete: () -> Unit) {
        val contentResolver = applicationContext.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"

        val storageRef = storage.reference.child("documents/$witel/$siteId/$docType.$extension")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                showToast("Document $docType uploaded successfully")
                onComplete()
            }
            .addOnFailureListener { e ->
                showToast("Error uploading document $docType: ${e.message}")
                onComplete() // Still call complete to ensure we don't block the process
            }
    }

    private fun uploadImage(imageType: String, uri: Uri, onComplete: () -> Unit) {
        val storageRef = storage.reference.child("images/$witel/$siteId/$imageType.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                showToast("Image $imageType uploaded successfully")
                onComplete()
            }
            .addOnFailureListener { e ->
                showToast("Error uploading image $imageType: ${e.message}")
                onComplete() // Still call complete to ensure we don't block the process
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}