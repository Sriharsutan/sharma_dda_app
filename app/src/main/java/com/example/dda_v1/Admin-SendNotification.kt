package com.example.dda_v1

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.listOf
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // Required fields
    var notificationText by remember { mutableStateOf("") }
    var schemeName by remember { mutableStateOf("") }
    var bookingDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Optional uploads
    var img1 by remember { mutableStateOf<Uri?>(null) }
    var img2 by remember { mutableStateOf<Uri?>(null) }
    var img3 by remember { mutableStateOf<Uri?>(null) }
    var img4 by remember { mutableStateOf<Uri?>(null) }
    var brochure by remember { mutableStateOf<Uri?>(null) }

    // Pickers
    val pickImage1 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { img1 = it }

    val pickImage2 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { img2 = it }

    val pickImage3 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { img3 = it }

    val pickImage4 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { img4 = it }

    val pickPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { brochure = it }

    val sc = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Notification", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(sc),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = notificationText,
                onValueChange = { notificationText = it },
                label = { Text("Notification Text *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = schemeName,
                onValueChange = { schemeName = it },
                label = { Text("Scheme Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bookingDate,
                onValueChange = { bookingDate = it },
                label = { Text("Booking Date *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Upload Flat Images (Optional)", style = MaterialTheme.typography.titleMedium)

            Button(onClick = { pickImage1.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (img1 != null) "Image 1 Selected ✓" else "Upload Flat Image 1")
            }
            Button(onClick = { pickImage2.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (img2 != null) "Image 2 Selected ✓" else "Upload Flat Image 2")
            }
            Button(onClick = { pickImage3.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (img3 != null) "Image 3 Selected ✓" else "Upload Flat Image 3")
            }
            Button(onClick = { pickImage4.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (img4 != null) "Image 4 Selected ✓" else "Upload Flat Image 4")
            }

            Text("Upload Brochure (PDF) – Optional", style = MaterialTheme.typography.titleMedium)

            Button(onClick = { pickPdf.launch("application/pdf") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (brochure != null) "Brochure Selected ✓" else "Upload Brochure PDF")
            }

            Spacer(modifier = Modifier.height(8.dp))

            var isSubmitting by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    if (
                        notificationText.isBlank() ||
                        schemeName.isBlank() ||
                        bookingDate.isBlank() ||
                        location.isBlank()
                    ) {
                        Toast.makeText(context, "Fill all required fields!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    isSubmitting = true

                    scope.launch {
                        uploadNotificationData(
                            db = db,
                            storage = storage,
                            notification = notificationText,
                            scheme = schemeName,
                            date = bookingDate,
                            location = location,
                            img1 = img1,
                            img2 = img2,
                            img3 = img3,
                            img4 = img4,
                            brochure = brochure,
                            context = context,
                            navController = navController
                        )
                        isSubmitting = false
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Submit Notification",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

suspend fun uploadNotificationData(
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    notification: String,
    scheme: String,
    date: String,
    location: String,
    img1: Uri?,
    img2: Uri?,
    img3: Uri?,
    img4: Uri?,
    brochure: Uri?,
    context: android.content.Context,
    navController: NavController
) {
    try {
        // 1) Get / increment counter
        val counterRef = db.collection("meta").document("notification_counter")

        val index = db.runTransaction { txn ->
            val snap = txn.get(counterRef)
            val current = snap.getLong("value") ?: 0L
            txn.set(counterRef, mapOf("value" to current + 1))
            current + 1
        }.await()

        val folderName = "notification$index"
        val rootPath = "notifications/$folderName"

        // 2) Upload helper
        suspend fun upload(uri: Uri?, name: String): String? {
            uri ?: return null

            val mime = context.contentResolver.getType(uri)
            val extension = mime?.substringAfter("/") ?: "file"

            val ref = storage.reference
                .child(rootPath)
                .child("$name.$extension")

            ref.putFile(uri).await()
            return ref.downloadUrl.await().toString()
        }

        // 3) Upload all files (optional)
        val images = mutableMapOf<String, String>()

        upload(img1, "image1")?.let { images["image1"] = it }
        upload(img2, "image2")?.let { images["image2"] = it }
        upload(img3, "image3")?.let { images["image3"] = it }
        upload(img4, "image4")?.let { images["image4"] = it }

        val brochureUrl = upload(brochure, "brochure")

        // 4) Save Firestore document
        val data = hashMapOf(
            "notificationText" to notification,
            "schemeName" to scheme,
            "bookingDate" to date,
            "location" to location,
            "storageFolder" to folderName,
            "images" to images,          // map of name -> url
            "brochurePdf" to brochureUrl,
            "timestamp" to Timestamp.now()
        )

        db.collection("notification_data").add(data).await()

        Toast.makeText(context, "Notification Posted Successfully!", Toast.LENGTH_LONG).show()
        navController.popBackStack()

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Failed to post notification: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        // You can also Log.e("Notifications", "Error", e) if you want details in Logcat
    }
}

@Composable
fun ShimmerNotificationButton(
    title: String,
    onClick: () -> Unit
) {
    val shimmerColors = listOf(
        Color(0x55FFFFFF),
        Color(0xFFFFFFFF),
        Color(0x55FFFFFF)
    )

    val transition = rememberInfiniteTransition(label = "")
    val xShimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = ""
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(xShimmer, 0f),
        end = Offset(xShimmer + 200f, 200f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    //colors = listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                    colors = listOf(Color(0xFF5B86E5), Color(0xFF36D1DC))

            )
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(shimmerBrush),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}