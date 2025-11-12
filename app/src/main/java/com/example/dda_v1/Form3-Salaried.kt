package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalariedFormScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore
    val storage = Firebase.storage

    // Document List (All are compulsory)
    val documentNames = listOf(
        "PAN Card",
        "Aadhaar Card",
        "Rent Agreement",
        "ID Card",
        "Assets Proof",
        "Salary Slip (Latest 3 Months)",
        "ITR (2 Years) OR Form 16 (Part A/B, Q1–Q4)",
        "Bank Statement (Latest 6 Months)",
        "Allotment Letter",
        "Passport Size Photo"
    )

    // Remember URI for each upload
    val documentUris = remember { mutableStateListOf<Uri?>() }
    if (documentUris.isEmpty()) {
        repeat(documentNames.size) { documentUris.add(null) }
    }

    // File Pickers
    val pickers = documentNames.mapIndexed { index, _ ->
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) documentUris[index] = uri
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salaried Employee Form", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A6D92),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ✅ scrollable
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Upload Required Documents (All Mandatory)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A6D92)
            )

            // Upload buttons
            documentNames.forEachIndexed { index, docName ->
                UploadDocButton(
                    label = docName,
                    imageUri = documentUris[index],
                    onClick = { pickers[index].launch("image/*") }
                )
            }

            if (progressText.isNotEmpty()) {
                Text(
                    text = progressText,
                    color = Color(0xFF0A6D92),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        // Validate all uploads
                        val missingDocs = documentNames.filterIndexed { index, _ ->
                            documentUris[index] == null
                        }

                        if (missingDocs.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                "Please upload: ${missingDocs.joinToString(", ")}",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        isSubmitting = true
                        progressText = "Uploading documents..."
                        val uploadedDocs = mutableMapOf<String, String>()

                        try {
                            documentUris.forEachIndexed { index, uri ->
                                uri?.let {
                                    val safeName = documentNames[index].replace(" ", "_")
                                    progressText = "Uploading ${documentNames[index]}..."
                                    val ref = storage.reference.child(
                                        "salaried_forms/${System.currentTimeMillis()}_$safeName.jpg"
                                    )
                                    ref.putFile(it).await()
                                    val url = ref.downloadUrl.await().toString()
                                    uploadedDocs[documentNames[index]] = url
                                }
                            }

                            // Save metadata in Firestore
                            db.collection("salaried_forms")
                                .add(
                                    hashMapOf(
                                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                        "documents" to uploadedDocs
                                    )
                                )

                            progressText = ""
                            isSubmitting = false
                            Toast.makeText(context, "Form Submitted Successfully!", Toast.LENGTH_LONG).show()
                            navController.popBackStack()

                        } catch (e: Exception) {
                            isSubmitting = false
                            progressText = ""
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A6D92)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}