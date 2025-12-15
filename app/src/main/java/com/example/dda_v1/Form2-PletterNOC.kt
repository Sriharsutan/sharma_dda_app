package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.provider.OpenableColumns
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PossessionLetterNocFormScreen(navController: NavController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore
    val storage = Firebase.storage
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"

    // Mandatory & optional documents
    val mandatoryDocs = listOf(
        "Annexure B (Allottee)",
        "Affidavit (Spouse OR Single Allottee)",
        "Undertaking (Allottee)",
        "Passport Size Photo (Allottee)",
        "Signature (Allottee)",
        "Aadhaar Card (Allottee)",
        "PAN Card (Allottee)",
        "Copy of Challans of all payments to DDA"
    )

    val optionalDocs = listOf(
        "Co-Allottee Passport Size Photo",
        "Co-Allottee Signature",
        "Co-Allottee Aadhaar Card",
        "Co-Allottee PAN Card",
        "Allottee Spouse PAN Card",
        "Income Certificate (EWS Category)",
        "Specimen Signature with Photo ",
        "Specimen Signature"
    )

    val financialDocs = listOf(
        "Loan Sanction Letter (Attested by Bank)",
        "Bank Statement (Name, Address & Amount)"
    )


    val allDocs = mandatoryDocs + optionalDocs + financialDocs

    // Document URIs
    val documentUris = remember { mutableStateListOf<Uri?>() }
    repeat(allDocs.size) { documentUris.add(null) }

//    val pickers = allDocs.mapIndexed { index, _ ->
//        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//            if (uri != null) documentUris[index] = uri
//        }
//    }
    val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L

    val pickers = allDocs.mapIndexed { index, _ ->
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
                title = { Text("Possession Letter & NOC", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Upload Required Documents",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            itemsIndexed(allDocs) { index, docName ->
                UploadDocButton(
                    label = docName,
                    imageUri = documentUris[index],
                    onClick = { pickers[index].launch("image/*") }
                )
            }

            item {

                if (progressText.isNotEmpty()) {
                    Text(progressText, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {

                            // Validate mandatory documents
                            val missing = mandatoryDocs.filterIndexed { index, _ ->
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
                                allDocs.forEachIndexed { index, name ->

                                    val uri = documentUris[index] ?: return@forEachIndexed

                                    val imageId = System.currentTimeMillis().toString()
                                    val safeName =
                                        name.replace("[^A-Za-z0-9_]".toRegex(), "")
                                    val fileName = "${safeName}_$imageId.jpg"

                                    progressText = "Uploading $name..."

                                    val ref = storage.reference
                                        .child("forms")
                                        .child("possession_letter_noc")
                                        .child(userId)
                                        .child(fileName)

                                    ref.putFile(uri).await()
                                    val url = ref.downloadUrl.await().toString()

                                    uploadedDocs[name] = mapOf(
                                        "imageId" to imageId,
                                        "url" to url
                                    )
                                }
                                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                                val userDetailsSnap = Firebase.firestore
                                    .collection("user_details")
                                    .document(userId)
                                    .get()
                                    .await()

                                val username = userDetailsSnap.getString("username") ?: "Unknown"
                                val submissionId = System.currentTimeMillis().toString()
                                db.collection("possession_noc_forms")
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting)
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    else
                        Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UploadDocButton(label: String, imageUri: Uri?, onClick: () -> Unit) {
    Column {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF777D80)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(label, fontWeight = FontWeight.Bold)
        }

        if (imageUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

fun getFileSize(context: Context, uri: Uri): Long {
    var fileSize = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("FileSizeCheck", "Error getting file size", e)
    }
    return fileSize
}