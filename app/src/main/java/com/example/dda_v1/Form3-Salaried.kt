package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"

    // All mandatory documents
    val documentNames = listOf(
        "PAN Card",
        "Aadhaar Card",
        "Rent Agreement",
        "ID Card",
        "Assets Proof",
        "Salary Slip Latest 3 Months",
        "ITR or Form 16",
        "Bank Statement Latest 6 Months",
        "Allotment Letter",
        "Passport Size Photo"
    )

    // URIs
    val documentUris = remember { mutableStateListOf<Uri?>() }
    if (documentUris.isEmpty()) {
        repeat(documentNames.size) { documentUris.add(null) }
    }

    // Pickers
//    val pickers = documentNames.mapIndexed { index, _ ->
//        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//            if (uri != null) documentUris[index] = uri
//        }
//    }
    val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L

    val pickers = documentNames.mapIndexed { index, _ ->
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val fileSize = getFileSize(context, uri)

                if (fileSize > MAX_FILE_SIZE_BYTES) {
                    val sizeMB = fileSize / (1024.0 * 1024.0)
                    Toast.makeText(
                        context,
                        "File size (${String.format("%.2f", sizeMB)} MB) exceeded 5MB limit. Please reduce the size and re-upload.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@rememberLauncherForActivityResult
                } else {
                    documentUris[index] = uri
                }
            }
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
                .verticalScroll(rememberScrollState())
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
                Text(progressText, color = Color(0xFF0A6D92), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {

                        // Validate
                        val missing = documentNames.filterIndexed { index, _ ->
                            documentUris[index] == null
                        }

                        if (missing.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                "Please upload: ${missing.joinToString()}",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        isSubmitting = true
                        progressText = "Uploading documents..."

                        val uploadedDocs = mutableMapOf<String, Any>()

                        try {
                            documentUris.forEachIndexed { index, uri ->

                                val documentName = documentNames[index]
                                uri ?: return@forEachIndexed

                                val imageId = System.currentTimeMillis().toString()
                                val safeName =
                                    documentName.replace("[^A-Za-z0-9_]".toRegex(), "")
                                val fileName = "${safeName}_$imageId.jpg"

                                progressText = "Uploading $documentName..."

                                val ref = storage.reference
                                    .child("forms")
                                    .child("salaried")
                                    .child(userId)
                                    .child(fileName)

                                ref.putFile(uri).await()
                                val url = ref.downloadUrl.await().toString()

                                uploadedDocs[documentName] = mapOf(
                                    "imageId" to imageId,
                                    "url" to url
                                )
                            }

                            // Save Firestore data
                            val submissionId = System.currentTimeMillis().toString()

                            val userId = FirebaseAuth.getInstance().currentUser!!.uid

                            val userDetailsSnap = Firebase.firestore
                                .collection("user_details")
                                .document(userId)
                                .get()
                                .await()

                            val username = userDetailsSnap.getString("username") ?: "Unknown"

                            db.collection("salaried_forms")
                                .document(userId)
                                .collection("submissions")
                                .document(submissionId)
                                .set(
                                    hashMapOf(
                                        "userId" to userId,
                                        "username" to username,
                                        "documents" to uploadedDocs,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )
                                )

                            isSubmitting = false
                            progressText = ""
                            Toast.makeText(
                                context,
                                "Form submitted successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()

                        } catch (e: Exception) {
                            isSubmitting = false
                            progressText = ""
                            Toast.makeText(
                                context,
                                "Upload failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
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
                if (isSubmitting)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else
                    Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}