package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PossessionLetterNocFormScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore
    val storage = Firebase.storage

    // Mandatory & Optional document lists
    val mandatoryDocs = listOf(
        "Annexure B (Allottee)",
        "Affidavit from Spouse or Single Person (Allottee)",
        "Undertaking (Allottee)",
        "Passport Size Photo (Allottee)",
        "Signature (Allottee)",
        "Aadhaar Card (Allottee)",
        "PAN Card (Allottee)",
        "Copy of Loan Sanction Letter (Attested by Bank)",
        "Copy of Bank Statement (Name, Address, Amount)",
        "Copy of Challans of All Payments to DDA",
        "Specimen Signature with Photo and 3 Signatures"
    )

    val optionalDocs = listOf(
        "Co-Allottee Passport Size Photo (If Any)",
        "Co-Allottee Signature (If Any)",
        "Co-Allottee Aadhaar Card (If Any)",
        "Co-Allottee PAN Card (If Any)",
        "Income Certificate (If EWS Category)",
        "Allottee Spouse PAN Card (If Any)"
    )

    val allDocs = mandatoryDocs + optionalDocs

    // Remember URIs for uploaded files
    val documentUris = remember { mutableStateListOf<Uri?>() }
    repeat(allDocs.size) { documentUris.add(null) }

    // File pickers
    val pickers = allDocs.mapIndexed { index, _ ->
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) documentUris[index] = uri
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

            // Upload buttons for each document
            itemsIndexed(allDocs) { index, docName ->
                UploadDocButton(
                    label = docName,
                    imageUri = documentUris[index],
                    onClick = { pickers[index].launch("image/*") }
                )
            }

            item {
                if (progressText.isNotEmpty()) {
                    Text(
                        text = progressText,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            // ✅ Validate mandatory uploads
                            val missingMandatory = mandatoryDocs.filterIndexed { index, _ ->
                                documentUris[index] == null
                            }

                            if (missingMandatory.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please upload: ${missingMandatory.joinToString(", ")}",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            isSubmitting = true
                            progressText = "Uploading documents..."
                            val uploadedDocs = mutableMapOf<String, String>()

                            try {
                                allDocs.forEachIndexed { index, name ->
                                    documentUris[index]?.let { uri ->
                                        val safeName = name.replace(" ", "_")
                                        progressText = "Uploading $name..."
                                        val ref = storage.reference.child(
                                            "possession_letter_noc/${System.currentTimeMillis()}_$safeName.jpg"
                                        )
                                        ref.putFile(uri).await()
                                        val url = ref.downloadUrl.await().toString()
                                        uploadedDocs[name] = url
                                    }
                                }

                                // Save data in Firestore
                                db.collection("possession_noc_forms")
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
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
}