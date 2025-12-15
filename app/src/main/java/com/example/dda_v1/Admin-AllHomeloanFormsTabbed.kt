package com.example.dda_v1

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IncomeFormsScreen(navController: NavController) {

    val tabs = listOf(
        FormTab("Salaried", "salaried_forms", Color(0xFF0083B0)),
        FormTab("Self-Employed", "self_employed_forms", Color(0xFF00B4DB))
    )

    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income-Based Forms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0055A4),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {

            // Tabs
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(tab.title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Pages
            HorizontalPager(state = pagerState) { page ->
                FormsListWithTopDownload(
                    collectionName = tabs[page].collection,
                    accentColor = tabs[page].color
                )
            }
        }
    }
}


@Composable
fun GenericFormCard(form: Map<String, Any>, accentColor: Color) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    @Suppress("UNCHECKED_CAST")
    val documents = try {
        form["documents"] as? Map<String, Map<String, String>> ?: emptyMap()
    } catch (e: Exception) {
        emptyMap<String, Map<String, String>>()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ---------- Header Row with User ID and Download Button ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👤 ${form["username"] ?: "Unknown User"}",
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            documents.forEach { (docName, data) ->
                                val url = data["url"] ?: return@forEach
                                startDownload(
                                    context = context,
                                    url = url,
                                    baseName = "${form["username"]}_$docName"
                                )
                            }

                            Toast.makeText(
                                context,
                                "Downloading ${documents.size} files...",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    enabled = documents.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download All",
                        tint = if (documents.isNotEmpty()) accentColor else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            val allowedDocuments = setOf(
                "Aadhaar Card",
                "PAN Card",
                "Passport Size Photo",
                "Aadhar Card (Allotte)",
                "PAN Card (Allotte)",
                "Annexure B (Allottee)",
                "Possession Letter"
            )

            val filteredDocuments = documents.filterKeys { it in allowedDocuments }


            // ---------- Images ----------
            if (documents.isEmpty()) {
                Text(
                    "No documents attached",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                if (filteredDocuments.isEmpty()) {
                    Text(
                        "No preview documents available",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    filteredDocuments.forEach { (docName, data) ->
                        data["url"]?.let { url ->
                            Text(
                                docName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = docName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}