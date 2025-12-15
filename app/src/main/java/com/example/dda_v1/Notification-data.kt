package com.example.dda_v1

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowNotificationsScreen() {

    val db = FirebaseFirestore.getInstance()
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val snapshot = db.collection("notification_data")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        notifications = snapshot.documents.mapNotNull { doc ->

            val imagesMap = doc.get("images") as? Map<*, *>

            NotificationItem(
                schemeName = doc.getString("schemeName") ?: "",
                notificationText = doc.getString("notificationText") ?: "",
                bookingDate = doc.getString("bookingDate") ?: "",
                location = doc.getString("location") ?: "",
                brochureUrl = doc.getString("brochurePdf"),
                images = imagesMap
                    ?.values
                    ?.filterIsInstance<String>()
                    ?: emptyList()
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFF0A2C78))
            )
        }
    ) { padding ->

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Stay tuned for new notifications.",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(notifications.size) { idx ->
                NotificationCard(
                    item = notifications[idx],
                    isFirst = idx == 0,
                    context = context
                )
            }
        }
    }
}

data class NotificationItem(
    val schemeName: String,
    val notificationText: String,
    val bookingDate: String,
    val location: String,
    val brochureUrl: String?,
    val images: List<String>
)

@Composable
fun NotificationCard(item: NotificationItem, isFirst: Boolean, context: Context) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = item.schemeName,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (item.images.isNotEmpty()) {
                AutoScrollingImageCarousel(images = item.images)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text("Scheme: ${item.schemeName}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Text("Location: ${item.location}")
            Spacer(modifier = Modifier.height(6.dp))

            Text("Booking Date: ${item.bookingDate}")
            Spacer(modifier = Modifier.height(12.dp))

            item.brochureUrl?.let { url ->
                Button(onClick = { downloadPDF(context, url, item.schemeName) }) {
                    Text("Download Brochure")
                }
            }

            if (isFirst) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "FIRST COME FIRST SERVE!!!",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun AutoScrollingImageCarousel(images: List<String>) {

    val scrollState = rememberScrollState()
    var selectedImage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(images) {
        while (true) {
            val maxScroll = scrollState.maxValue
            scrollState.animateScrollTo(
                maxScroll,
                tween(durationMillis = 30000, easing = LinearEasing)
            )
            scrollState.scrollTo(0)
        }
    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        val doubledImages = images + images  // infinite loop effect

        doubledImages.forEach { url ->
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .height(150.dp)
                    .clickable { selectedImage = url },   //  CLICK
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Flat Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    //  FULL SCREEN IMAGE VIEW
    selectedImage?.let { imageUrl ->
        FullScreenImageDialog(
            imageUrl = imageUrl,
            onDismiss = { selectedImage = null }
        )
    }
}

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }, // tap to close
                contentScale = ContentScale.Fit
            )

            //  Close Button (optional)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

// PDF Downloader
fun downloadPDF(context: Context, url: String, schemeName: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("$schemeName Brochure")
        .setDescription("Downloading...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${schemeName}_Brochure.pdf"
        )

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}