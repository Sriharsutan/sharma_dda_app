package com.assist.doorstepdocuments

import android.net.Uri
import android.widget.Toast
import android.media.MediaPlayer
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRentalScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var furnishing by remember { mutableStateOf("Unfurnished") }
    var area by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var uploadProgress by remember { mutableStateOf(0f) }
    var isUploading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        mediaUris = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Rental") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                onValueChange = { rent = it.filter { c -> c.isDigit() }.take(10) },
                label = { Text("Rent") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth()
            )

//            OutlinedTextField(
//                value = furnishing,
//                onValueChange = { furnishing = it },
//                label = { Text("Furnishing") },
//                modifier = Modifier.fillMaxWidth()
//            )

            Text(
                text = "Furnishing",
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = furnishing == "Unfurnished",
                        onClick = { furnishing = "Unfurnished" }
                    )
                    Text("Unfurnished")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = furnishing == "Semi Furnished",
                        onClick = { furnishing = "Semi Furnished" }
                    )
                    Text("Semi Furnished")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = furnishing == "Fully Furnished",
                        onClick = { furnishing = "Fully Furnished" }
                    )
                    Text("Fully Furnished")
                }
            }

            OutlinedTextField(
                value = area,
                onValueChange = { area = it.filter { c -> c.isDigit() } },
                label = { Text("Area") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                trailingIcon = { Text("sqft") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                label = { Text("Contact Number") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { picker.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Images / Videos")
            }

            if (mediaUris.isNotEmpty()) {

                Text("Preview")

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    items(mediaUris) { uri ->

                        if (uri.toString().contains("video")) {

                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(uri)
                                        setOnPreparedListener { mp: MediaPlayer ->
                                            mp.isLooping = true
                                            start()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(150.dp)
                            )

                        } else {

                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }
                }
            }

            if (isUploading) {

                LinearProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Uploading ${(uploadProgress * 100).toInt()}%")
            }

            Button(
                onClick = {

                    if (title.isBlank() || address.isBlank()) {

                        Toast.makeText(
                            context,
                            "Please fill required fields",
                            Toast.LENGTH_LONG
                        ).show()

                        return@Button
                    }

                    scope.launch {

                        isUploading = true

                        val urls = mutableListOf<String>()

                        val totalFiles = mediaUris.size
                        var uploadedCount = 0

                        for (uri in mediaUris) {

                            val fileName = "rentals/${UUID.randomUUID()}"

                            val ref = storage.reference.child(fileName)

                            val uploadTask = ref.putFile(uri)

                            uploadTask.addOnProgressListener {

                                val progress =
                                    it.bytesTransferred.toFloat() / it.totalByteCount

                                uploadProgress =
                                    (uploadedCount + progress) / totalFiles
                            }

                            uploadTask.await()

                            val url = ref.downloadUrl.await()

                            urls.add(url.toString())

                            uploadedCount++

                            uploadProgress = uploadedCount.toFloat() / totalFiles
                        }

                        val data = hashMapOf(
                            "title" to title,
                            "address" to address,
                            "rent" to rent,
                            "furnishing" to furnishing,
                            "area" to area,
                            "images" to urls
                        )

                        db.collection("rentals").add(data).await()

                        isUploading = false

                        Toast.makeText(
                            context,
                            "Rental Uploaded Successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.popBackStack()
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

                    Text("Add Rental")
                }
            }
        }
    }
}