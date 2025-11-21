package com.example.dda_v1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UploadRentalScreen(navController: NavController) {

    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var furnishing by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upload Rental") })
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Rent") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = furnishing, onValueChange = { furnishing = it }, label = { Text("Furnishing (Fully/Semi/Unfurnished)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area (Sq.Ft)") }, modifier = Modifier.fillMaxWidth())

            // Pick image button
            Button(
                onClick = { /* launch image picker here */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Images")
            }

            Text("Selected images: ${selectedImages.size}")

            Button(
                onClick = {
                    uploadRental(
                        title = title,
                        address = address,
                        rent = rent,
                        furnishing = furnishing,
                        area = area,
                        images = selectedImages,
                        db = db,
                        storage = storage,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Rental")
            }
        }
    }
}
fun uploadRental(
    title: String,
    address: String,
    rent: String,
    furnishing: String,
    area: String,
    images: List<Uri>,
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    onSuccess: () -> Unit
) {
    val imageUrls = mutableListOf<String>()

    // CASE 1 → No images selected → Direct upload
    if (images.isEmpty()) {
        val data = hashMapOf(
            "title" to title,
            "address" to address,
            "rent" to rent,
            "furnishing" to furnishing,
            "area" to area,
            "images" to emptyList<String>()
        )
        db.collection("rentals").add(data).addOnSuccessListener {
            onSuccess()
        }
        return
    }

    // CASE 2 → Images selected → Upload each image then store URLs
    val total = images.size
    var uploaded = 0

    images.forEach { uri ->
        val fileName = "rentals/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)

        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                imageUrls.add(url.toString())
                uploaded++

                if (uploaded == total) {
                    val data = hashMapOf(
                        "title" to title,
                        "address" to address,
                        "rent" to rent,
                        "furnishing" to furnishing,
                        "area" to area,
                        "images" to imageUrls
                    )
                    db.collection("rentals").add(data).addOnSuccessListener {
                        onSuccess()
                    }
                }
            }
        }
    }
}