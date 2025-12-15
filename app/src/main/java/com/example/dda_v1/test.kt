package com.example.yourapp.ui.pages

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Composable
fun FormPage() {

    var name by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val storage = FirebaseStorage.getInstance().reference
    val firestore = FirebaseFirestore.getInstance()

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            "Fill the Form",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(15.dp))

        // Place field
        OutlinedTextField(
            value = place,
            onValueChange = { place = it },
            label = { Text("Place") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Upload Image")
        }

        if (selectedImageUri != null) {
            Spacer(Modifier.height(10.dp))
            Text("Image selected ✔")
        }

        Spacer(Modifier.height(25.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && place.isNotEmpty() && selectedImageUri != null) {
                    isUploading = true
                    uploadData(
                        name,
                        place,
                        selectedImageUri!!,
                        storage,
                        firestore
                    ) {
                        isUploading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            Text(if (isUploading) "Uploading..." else "Submit")
        }
    }
}

fun uploadData(
    name: String,
    place: String,
    imageUri: Uri,
    storage: StorageReference,
    firestore: FirebaseFirestore,
    onComplete: () -> Unit
) {
    val imageId = System.currentTimeMillis().toString()
    val imageRef = storage.child("test_form/$imageId.jpg")

    // Upload image
    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->

                val data = mapOf(
                    "name" to name,
                    "place" to place,
                    "imageId" to imageId,
                    "imageUrl" to url.toString(),
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // Store in firestore
                firestore.collection("form_data")
                    .add(data)
                    .addOnSuccessListener {
                        onComplete()
                        Log.d("FORM", "Data stored successfully")
                    }
                    .addOnFailureListener {
                        onComplete()
                        Log.e("FORM", "Firestore error: ${it.message}")
                    }
            }
        }
        .addOnFailureListener {
            onComplete()
            Log.e("FORM", "Storage upload error: ${it.message}")
        }
}