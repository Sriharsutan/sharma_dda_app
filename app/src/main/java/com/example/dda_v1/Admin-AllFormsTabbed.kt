package com.example.dda_v1

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllFormsTabbedScreen(navController: NavController) {

    val tabs = listOf(
        FormTab("Conveyance", "conveyance_forms", Color(0xFF4106AB)),
        FormTab("Possession", "possession_noc_forms", Color(0xFF0072FF))
//        FormTab("Salaried", "salaried_forms", Color(0xFFF7971E)),
//        FormTab("Business", "business_forms", Color(0xFF56AB2F))
    )

    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Form Submissions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {

            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tab.title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            HorizontalPager(state = pagerState) { page ->
                FormsListWithTopDownload(
                    collectionName = tabs[page].collection,
                    accentColor = tabs[page].color
                )
            }
        }
    }
}

data class FormTab(val title: String, val collection: String, val color: Color)

@Composable
fun FormsListWithTopDownload(collectionName: String, accentColor: Color) {

    val db = Firebase.firestore
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var formList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(collectionName) {
        isLoading = true
        errorMessage = null
        try {
            val snapshot = Firebase.firestore
                .collectionGroup("submissions")
                .get()
                .await()

            formList = snapshot.documents
                .filter { it.reference.path.contains(collectionName) } // 🔥 important
                .mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                }

        } catch (e: Exception) {
            errorMessage = "Error loading forms: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    val allFileUrls = remember(formList) {
        formList.flatMap { form ->
            val docs = form["documents"] as? Map<String, Map<String, String>> ?: emptyMap()
            docs.values.mapNotNull { it["url"] }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = collectionName.replace("_", " ").uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accentColor
            )

            IconButton(
                enabled = allFileUrls.isNotEmpty() && !isDownloading,
                onClick = {
                    isDownloading = true
                    coroutineScope.launch {
                        allFileUrls.forEachIndexed { index, url ->
                            startDownload(
                                context,
                                url,
                                "$collectionName-file-$index"
                            )
                        }
                        Toast.makeText(
                            context,
                            "Downloading ${allFileUrls.size} files…",
                            Toast.LENGTH_LONG
                        ).show()
                        isDownloading = false
                    }
                }
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        color = accentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(Icons.Default.Download, "Download All", tint = accentColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = accentColor) }

            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    errorMessage ?: "Unknown error",
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }

            formList.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("No submissions found.", color = Color.Gray) }

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(formList) { form ->
                    GenericFormCard(form, accentColor)
                }
            }
        }
    }
}

fun startDownload(context: Context, url: String, baseName: String) {
    val extension = url.substringAfterLast('.', "file")

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(baseName)
        .setDescription("Downloading file…")
        .setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$baseName.$extension"
        )

    val manager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)
}