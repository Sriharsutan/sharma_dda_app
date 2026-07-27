package com.assist.doorstepdocuments

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------------- SCREEN ----------------

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
                images = imagesMap?.values?.filterIsInstance<String>() ?: emptyList()
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

        LazyColumn(
            modifier = Modifier.padding(padding).padding(12.dp),
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

// ---------------- DATA ----------------

data class NotificationItem(
    val schemeName: String,
    val notificationText: String,
    val bookingDate: String,
    val location: String,
    val brochureUrl: String?,
    val images: List<String>
)

// ---------------- CARD ----------------

@Composable
fun NotificationCard(item: NotificationItem, isFirst: Boolean, context: Context) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(item.schemeName, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(10.dp))

            if (item.images.isNotEmpty()) {
                AutoScrollingImageCarousel(item.images)
                Spacer(Modifier.height(10.dp))
            }

            Text("Location: ${item.location}")
            Text("Booking Date: ${item.bookingDate}")

            item.brochureUrl?.let { url ->
                Spacer(Modifier.height(10.dp))
                Button(onClick = { downloadPDF(context, url, item.schemeName) }) {
                    Text("Download Brochure")
                }
            }

            if (isFirst) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "FIRST COME FIRST SERVE!!!",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

// ---------------- CAROUSEL ----------------

@Composable
fun AutoScrollingImageCarousel(images: List<String>) {

    val scrollState = rememberScrollState()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

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
        modifier = Modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val doubled = images + images

        doubled.forEachIndexed { index, url ->
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .height(150.dp)
                    .clickable { selectedIndex = index % images.size },
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    selectedIndex?.let {
        ImagePopupViewer(
            images = images,
            startIndex = it,
            onDismiss = { selectedIndex = null }
        )
    }
}

// ---------------- POPUP VIEWER ----------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePopupViewer(
    images: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { images.size }
    )
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f)
                .background(Color.Black, RoundedCornerShape(16.dp))
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(images[page])
            }

            // Close
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }

            // Left
            IconButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage > 0)
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text("◀", color = Color.White, fontSize = 28.sp)
            }

            // Right
            IconButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < images.lastIndex)
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("▶", color = Color.White, fontSize = 28.sp)
            }
        }
    }
}

// ---------------- ZOOMABLE IMAGE ----------------

//@Composable
//fun ZoomableImage(imageUrl: String) {
//
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    val transformState = rememberTransformableState { zoom, pan, _ ->
//        scale = (scale * zoom).coerceIn(1f, 5f)
//        offset += pan
//    }
//
//    AsyncImage(
//        model = imageUrl,
//        contentDescription = null,
//        modifier = Modifier
//            .fillMaxSize()
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//                translationX = offset.x
//                translationY = offset.y
//            }
//            .transformable(transformState),
//        contentScale = ContentScale.Fit
//    )
//}

// ---------------- PDF DOWNLOAD ----------------

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