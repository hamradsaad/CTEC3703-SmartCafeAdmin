package com.example.smartcafeadmin.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartcafeadmin.R
import com.example.smartcafeadmin.data.DatabaseHelper
import com.example.smartcafeadmin.data.ImageStorage
import com.example.smartcafeadmin.logic.Categories
import com.example.smartcafeadmin.logic.MenuValidator
import com.example.smartcafeadmin.model.MenuItem
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Locale

// Add / edit a menu item with category dropdown and photo (CR-01, CR-02, CR-03)
class EditItemActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var textTitle: TextView
    private lateinit var imagePreview: ImageView
    private lateinit var textPhotoPlaceholder: TextView
    private lateinit var buttonAddPhoto: Button
    private lateinit var buttonRemovePhoto: Button
    private lateinit var editName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editPrice: EditText
    private lateinit var editDescription: EditText
    private lateinit var switchAvailable: MaterialSwitch
    private lateinit var textError: TextView
    private lateinit var buttonDelete: Button

    private var existing: MenuItem? = null
    private var originalImagePath = ""
    private var currentImagePath = ""
    private var saved = false
    private lateinit var categoryOptions: List<String>

    // Android photo picker: no storage permission needed, user picks one image
    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult   // user cancelled

        val path = ImageStorage.saveFromUri(this, uri)
        if (path == null) {
            Toast.makeText(this, "Could not load that picture. Please try another one.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        // Remove a picture picked earlier in this session that was never saved
        if (currentImagePath.isNotEmpty() && currentImagePath != originalImagePath) {
            ImageStorage.delete(currentImagePath)
        }
        currentImagePath = path
        showImage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)

        textTitle = findViewById(R.id.textTitle)
        imagePreview = findViewById(R.id.imagePreview)
        textPhotoPlaceholder = findViewById(R.id.textPhotoPlaceholder)
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonRemovePhoto = findViewById(R.id.buttonRemovePhoto)
        editName = findViewById(R.id.editName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        editPrice = findViewById(R.id.editPrice)
        editDescription = findViewById(R.id.editDescription)
        switchAvailable = findViewById(R.id.switchAvailable)
        textError = findViewById(R.id.textError)
        buttonDelete = findViewById(R.id.buttonDelete)

        val itemId = intent.getIntExtra("itemId", -1)
        existing = if (itemId != -1) db.getMenuItem(itemId) else null

        // Category dropdown (keeps any older category that isn't in the fixed list)
        val options = Categories.ALL.toMutableList()
        existing?.category?.let { if (it !in options) options.add(it) }
        categoryOptions = options
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)

        existing?.let { item ->
            textTitle.text = "Edit item"
            editName.setText(item.name)
            editPrice.setText(String.format(Locale.UK, "%.2f", item.price))
            editDescription.setText(item.description)
            switchAvailable.isChecked = item.available
            spinnerCategory.setSelection(categoryOptions.indexOf(item.category))
            originalImagePath = item.imagePath
            currentImagePath = item.imagePath
            buttonDelete.visibility = View.VISIBLE
        }

        showImage()

        buttonAddPhoto.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        buttonRemovePhoto.setOnClickListener {
            if (currentImagePath != originalImagePath) ImageStorage.delete(currentImagePath)
            currentImagePath = ""
            showImage()
        }
        findViewById<Button>(R.id.buttonSave).setOnClickListener { save() }
        buttonDelete.setOnClickListener { confirmDelete() }
        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }
    }

    private fun showImage() {
        val bitmap = ImageStorage.loadThumbnail(currentImagePath, 800)
        if (bitmap != null) {
            imagePreview.setImageBitmap(bitmap)
            imagePreview.visibility = View.VISIBLE
            textPhotoPlaceholder.visibility = View.GONE
            buttonAddPhoto.text = "Change photo"
            buttonRemovePhoto.visibility = View.VISIBLE
        } else {
            imagePreview.setImageDrawable(null)
            imagePreview.visibility = View.GONE
            textPhotoPlaceholder.visibility = View.VISIBLE
            buttonAddPhoto.text = "Add photo"
            buttonRemovePhoto.visibility = View.GONE
        }
    }

    private fun save() {
        val name = editName.text.toString().trim()
        val category = spinnerCategory.selectedItem?.toString() ?: ""
        val priceText = editPrice.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val available = switchAvailable.isChecked

        val error = MenuValidator.validate(name, category, priceText)
        if (error != null) {
            textError.text = error
            textError.visibility = View.VISIBLE
            return
        }
        val price = priceText.toDouble()

        val current = existing
        if (current == null) {
            val result = db.addMenuItem(name, category, price, available, description, currentImagePath)
            if (result == -1L) {
                textError.text = "Could not save item. Please try again."
                textError.visibility = View.VISIBLE
                return
            }
            Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
        } else {
            val updated = current.copy(
                name = name,
                category = category,
                price = price,
                available = available,
                description = description,
                imagePath = currentImagePath
            )
            db.updateMenuItem(updated)
            Toast.makeText(this, "$name updated", Toast.LENGTH_SHORT).show()
        }

        // Old picture was replaced or removed, so delete the old file
        if (originalImagePath.isNotEmpty() && originalImagePath != currentImagePath) {
            ImageStorage.delete(originalImagePath)
        }
        saved = true
        finish()
    }

    private fun confirmDelete() {
        val item = existing ?: return
        AlertDialog.Builder(this)
            .setTitle("Delete item")
            .setMessage("Delete ${item.name} from the menu?")
            .setPositiveButton("Delete") { _, _ ->
                db.deleteMenuItem(item.id)
                ImageStorage.delete(originalImagePath)
                if (currentImagePath != originalImagePath) ImageStorage.delete(currentImagePath)
                saved = true
                Toast.makeText(this, "${item.name} deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Leaving without saving: remove any new picture that was picked
    override fun onDestroy() {
        super.onDestroy()
        if (!saved && isFinishing && currentImagePath.isNotEmpty() && currentImagePath != originalImagePath) {
            ImageStorage.delete(currentImagePath)
        }
    }
}