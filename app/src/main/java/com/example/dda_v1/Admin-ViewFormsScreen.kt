package com.example.dda_v1

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await

data class DocumentMeta(
    val imageId: String = "",
    val url: String = ""
)

data class FormData(
    val submissionId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val maritalStatus: String = "",
    val spouseName: String? = null,
    val category: String = "",
    val bankName: String = "",
    val branchName: String = "",
    val ifscCode: String = "",
    val accountNumber: String = "",
    val nationality: String = "",
    val addressHouse: String = "",
    val addressStreet: String = "",
    val addressArea: String = "",
    val addressState: String = "",
    val addressDistrict: String = "",
    val addressPin: String = "",
    val region: String = "",

    val aadhaar: DocumentMeta? = null,
    val pan: DocumentMeta? = null,
    val photo: DocumentMeta? = null,
    val signature: DocumentMeta? = null,

    val timestamp: com.google.firebase.Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFormsScreen(navController: NavController) {

    val db = Firebase.firestore
    var forms by remember { mutableStateOf<List<FormData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collectionGroup("submissions")
                //.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

//            forms = snapshot.documents.map { doc ->
//                doc.toObject(FormData::class.java)!!.copy(
//                    submissionId = doc.id
//                )
//            }
            forms = snapshot.documents
                // ONLY take submissions from user_forms
                .filter { it.reference.path.startsWith("user_forms/") }
                .mapNotNull { doc ->
                    doc.toObject(FormData::class.java)?.copy(
                        submissionId = doc.id
                    )
                }

            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submitted Forms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Back", color = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {

            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null ->
                    Text("Error: $error", color = Color.Red, modifier = Modifier.align(Alignment.Center))

                forms.isEmpty() ->
                    Text("No forms submitted yet",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray)

                else -> LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(forms) { form ->
                        FormCard(form)
                    }
                }
            }
        }
    }
}

@Composable
fun FormCard(form: FormData) {

    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val documents: List<Pair<String, String>> = buildList {
        form.aadhaar?.url?.let { add("Aadhaar Card" to it) }
        form.pan?.url?.let { add("PAN Card" to it) }
        form.photo?.url?.let { add("Photograph" to it) }
        form.signature?.url?.let { add("Signature" to it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {

                Column {
                    Text(
                        text = "👤 ${form.userEmail}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE)
                    )
                    Text(
                        text = formatTimestamp(form.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(if (expanded) "▲" else "▼", fontSize = 18.sp)
            }

            Spacer(Modifier.height(8.dp))
            InfoRow("Father Name", form.fatherName)
            InfoRow("Bank", form.bankName)

            if (expanded) {

                //Divider(Modifier.padding(vertical = 10.dp))
                HorizontalDivider(Modifier.padding(vertical = 10.dp))

                InfoRow("Mother Name", form.motherName)
                InfoRow("Marital Status", form.maritalStatus)

                if (form.maritalStatus == "Married") {
                    InfoRow("Spouse Name", form.spouseName ?: "N/A")
                }

                InfoRow("Category", form.category)
                InfoRow("Nationality", form.nationality)

                Spacer(Modifier.height(6.dp))

                Text(
                    "Bank Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                InfoRow("Bank Name", form.bankName)
                InfoRow("IFSC Code", form.ifscCode)
                InfoRow("Account Number", form.accountNumber)
                InfoRow("Branch Name", form.branchName)

                Spacer(Modifier.height(6.dp))

                Text(
                    "Address",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                InfoRow("House", form.addressHouse)
                InfoRow("Street", form.addressStreet)
                InfoRow("Area", form.addressArea)
                InfoRow("District", form.addressDistrict)
                InfoRow("State", form.addressState)
                InfoRow("PIN Code", form.addressPin)
                InfoRow("Region", form.region)

                HorizontalDivider(Modifier.padding(vertical = 10.dp))

                documents.forEach { (label, url) ->
                    Column(Modifier.padding(bottom = 8.dp)) {
                        Text(label, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val safeUsername = sanitizeEmailForFilename(form.userEmail)
                        documents.forEach { (label, url) ->
                            downloadFile(context, url, "${safeUsername}_$label")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download All Documents")
                }
            }
        }
    }
}


fun downloadFile(context: Context, url: String, name: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${name.replace(" ", "_")}.jpg"
        )

    val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    mgr.enqueue(request)
}

fun formatTimestamp(ts: com.google.firebase.Timestamp?): String {
    if (ts == null) return "Unknown date"
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        .format(ts.toDate())
}

fun sanitizeEmailForFilename(email: String): String {
    return email
        .replace("@gmail.com", "", ignoreCase = true)
        .replace(".", "_")
        .trim()
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value.ifEmpty { "N/A" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}