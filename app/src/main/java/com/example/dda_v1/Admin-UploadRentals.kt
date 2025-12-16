package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadRentalScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var furnishing by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upload Rental") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rent,
                onValueChange = { rent = it },
                label = { Text("Rent") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = furnishing,
                onValueChange = { furnishing = it },
                label = { Text("Furnishing") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area (Sq.Ft)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Images")
            }

            Text("Selected images: ${selectedImages.size}")

            Button(
                onClick = {
                    if (title.isBlank() || address.isBlank()) {
                        Toast.makeText(context, "Fill required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true

                    scope.launch {
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
                                isUploading = false

                                Toast.makeText(
                                    context,
                                    "Rental uploaded successfully",
                                    Toast.LENGTH_LONG
                                ).show()

                                navController.navigate("admin_dashboard") {
                                    popUpTo("upload_rental") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                                    onError = {
                                isUploading = false
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Rental")
                }
            }
        }
    }
}

suspend fun uploadRental(
    title: String,
    address: String,
    rent: String,
    furnishing: String,
    area: String,
    images: List<Uri>,
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val imageUrls = mutableListOf<String>()

        // Upload images (if any)
        for (uri in images) {
            val fileName = "rentals/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)

            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            imageUrls.add(url)
        }

        val data = hashMapOf(
            "title" to title,
            "address" to address,
            "rent" to rent,
            "furnishing" to furnishing,
            "area" to area,
            "images" to imageUrls
        )

        db.collection("rentals").add(data).await()

        onSuccess()

    } catch (e: Exception) {
        onError(e.message ?: "Upload failed")
    }
}