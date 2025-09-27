
package com.example.miniproject2

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop // Import the new library
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null //takes photo with imagecapture class
    private lateinit var outputDirectory: File //to save the image captured
    private lateinit var cameraExecutor: ExecutorService //thread to run cam to run in background
    private lateinit var cameraPreview: PreviewView
    private lateinit var btnScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraPreview = findViewById(R.id.camera_preview)
        btnScan = findViewById(R.id.btn_scan)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        btnScan.setOnClickListener { takePhoto() }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    //This function captures an image, saves it to a file, and then sends that file to the image cropper
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return  // object of cameraX
        val photoFile = File(outputDirectory, "temp-photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()  //format CameraX requires to know where and how to save the image.

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    // Photo is saved, now LAUNCH THE UCROP ACTIVITY
                    launchCropper(savedUri)
                }
            })
    }

    private fun launchCropper(sourceUri: Uri) {
        // Define where the cropped image will be saved
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))  //apps private cache directory
        // Start the uCrop activity
        UCrop.of(sourceUri, destinationUri)
//            .withAspectRatio(1f, 1f) // You can customize aspect ratio
            .withMaxResultSize(1000, 1000) // You can resize the image
            .start(this)
    }

    // This function now handles the result from the uCrop activity
    @Deprecated("This method is deprecated but required for uCrop's default implementation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                // Now run text recognition on the CROPPED image
                runTextRecognition(resultUri)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            Log.e(TAG, "Image cropping failed: ", cropError)
        }
    }

    //process that "reads" the text directly from the image.
    private fun runTextRecognition(uri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create InputImage from URI.", e)
            Toast.makeText(this, "Error preparing image.", Toast.LENGTH_SHORT).show()
            return
        }

        // DEBUG: Confirm that we are starting the recognition process
        Log.d(TAG, "Starting text recognition process...")

        btnScan.isEnabled = false
        recognizer.process(image)  //kicks off the machine learning analysis
            .addOnSuccessListener { visionText ->
                btnScan.isEnabled = true
                // DEBUG: This is the MOST important log. It tells us if the process succeeded at all.
                Log.d(TAG, "Text recognition SUCCESS listener was called.")

                val rawText = visionText.text  // extracts all the recognized text from the image into a single string variable
                if (rawText.isNullOrBlank()) {
                    // DEBUG: This tells us it succeeded but found no text.
                    Log.w(TAG, "Recognition succeeded, but the raw text is NULL or EMPTY.")
                    Toast.makeText(this, "No text found in image. Please try again with a clearer picture.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                Log.d(TAG, "Raw OCR Result: $rawText")

                val cleanIngredients = parseIngredientText(rawText)  //cleans the string

                if (cleanIngredients.isEmpty()) {
                    Log.w(TAG, "Text was found, but parsing resulted in an empty list.")
                    Toast.makeText(this, "Could not identify an ingredient list.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                Log.d(TAG, "Clean Ingredients List: $cleanIngredients")

                // Show the category selection dialog
                showCategorySelectionDialog(cleanIngredients)
            }
            .addOnFailureListener { e ->
                btnScan.isEnabled = true
                // DEBUG: This tells us if the entire ML Kit process is failing.
                Log.e(TAG, "Text recognition FAILED.", e)
                Toast.makeText(this, "Text recognition failed. See logs.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showCategorySelectionDialog(ingredients: List<String>) {
        val categories = arrayOf("Overall", "Face", "Body", "Hair", "Eyes", "Lips")

        AlertDialog.Builder(this)
            .setTitle("Select Product Category")
            .setItems(categories) { dialog, which ->
                val selectedCategory = categories[which]

                // 1. Create an Intent to start the ResultsActivity
                val intent = Intent(this, ResultsActivity::class.java).apply {
                    // 2. Add the selected category and the list of ingredients to the intent.
                    // We use keys like "EXTRA_CATEGORY" to identify the data on the next screen.
                    putExtra("EXTRA_CATEGORY", selectedCategory)
                    putStringArrayListExtra("EXTRA_INGREDIENTS", ArrayList(ingredients))
                }

                startActivity(intent)


            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }



    private fun parseIngredientText(rawText: String?): List<String> {
        if (rawText.isNullOrBlank()) {
            return emptyList()
        }

        // 1. Start by finding "INGREDIENTS:" (case-insensitive) and taking everything after it.
        var processedText = rawText.substringAfter("INGREDIENTS:", "")
        if (processedText.isBlank()) {
            processedText = rawText // If keyword not found, use the whole text
        }

        // 2. Normalize the text: replace all line breaks with a comma. This is better
        // than a space because ingredients are often separated by line breaks.
        processedText = processedText.replace('\n', ',')

        // 3. Fix hyphenated words that span lines (now joined by a comma)
        // e.g., "Metho-,xydibenzoylmethane" becomes "Methoxydibenzoylmethane"
        processedText = processedText.replace(Regex("-,\\s*"), "")

        // 4. Remove any text inside parentheses, e.g., (AQUA) or (FRAGRANCE)
        processedText = processedText.replace(Regex("\\(.*?\\)"), "")

        // 5. Now, split by comma and clean up each individual ingredient string
        return processedText
            .split(",")
            .map {
                it.trim() // Remove leading/trailing whitespace
                    .replace(Regex("\\s+"), " ") // Replace multiple spaces with a single space
                    .lowercase() // Make it lowercase for matching
            }
            .filter { it.length > 1 } // Remove any empty or single-character items
            .distinct() // Remove any duplicate ingredients
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { File(it, getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    companion object {
        private const val TAG = "CameraXGlowly"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}