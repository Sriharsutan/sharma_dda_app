package com.example.dda_v1

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class FormData(
    val userId: String = "",
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
    val aadhaarUrl: String? = null,
    val panUrl: String? = null,
    val photoUrl: String? = null,
    val signUrl: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFormsScreen(navController: NavController) {
    val db = Firebase.firestore
    var forms by remember { mutableStateOf<List<FormData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("user_forms")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val formsList = documents.map { doc ->
                    doc.toObject(FormData::class.java)
                }
                forms = formsList
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Error loading forms: ${e.message}"
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
                        Text("← Back", color = Color.White, fontSize = 16.sp)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                forms.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No forms submitted yet",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Total Forms: ${forms.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(forms) { form ->
                            FormCard(form)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormCard(form: FormData) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "👤 ${form.userId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTimestamp(form.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 20.sp,
                    color = Color(0xFF6200EE)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Info
            InfoRow("Father's Name", form.fatherName)
            InfoRow("Bank", form.bankName)

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Personal Information
                Text(
                    text = "Personal Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                InfoRow("Mother's Name", form.motherName)
                InfoRow("Marital Status", form.maritalStatus)
                if (form.spouseName != null) {
                    InfoRow("Spouse Name", form.spouseName)
                }
                InfoRow("Category", form.category)
                InfoRow("Nationality", form.nationality)

                Spacer(modifier = Modifier.height(12.dp))

                // Bank Information
                Text(
                    text = "Bank Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                InfoRow("Branch", form.branchName)
                InfoRow("IFSC Code", form.ifscCode)
                InfoRow("Account Number", form.accountNumber)

                Spacer(modifier = Modifier.height(12.dp))

                // Address Information
                Text(
                    text = "Address Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                InfoRow("House Number", form.addressHouse)
                InfoRow("Street", form.addressStreet)
                InfoRow("Area", form.addressArea)
                InfoRow("State", form.addressState)
                InfoRow("District", form.addressDistrict)
                InfoRow("PIN Code", form.addressPin)
                InfoRow("Region", form.region)

                Spacer(modifier = Modifier.height(12.dp))

                // Documents
                Text(
                    text = "Uploaded Documents",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (form.aadhaarUrl != null) {
                        DocumentImage("Aadhaar", form.aadhaarUrl, Modifier.weight(1f))
                    }
                    if (form.panUrl != null) {
                        DocumentImage("PAN", form.panUrl, Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (form.photoUrl != null) {
                        DocumentImage("Photo", form.photoUrl, Modifier.weight(1f))
                    }
                    if (form.signUrl != null) {
                        DocumentImage("Signature", form.signUrl, Modifier.weight(1f))
                    }
                }
            }
        }
    }
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

@Composable
fun DocumentImage(label: String, url: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Unknown date"

    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(date)
}