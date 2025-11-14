package com.example.dda_v1

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllFormsTabbedScreen(navController: NavController) {
    val tabs = listOf(
        FormTab("Conveyance", "conveyance_forms", Color(0xFF4106AB)),
        FormTab("Possession", "possession_noc_forms", Color(0xFF0072FF)),
        FormTab("Salaried", "salaried_forms", Color(0xFFF7971E)),
        FormTab("Business", "business_forms", Color(0xFF56AB2F))
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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

            // Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFF6200EE)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(tab.title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Pager Content
            HorizontalPager(state = pagerState) { page ->
                FormsList(
                    collectionName = tabs[page].collection,
                    accentColor = tabs[page].color
                )
            }
        }
    }
}

data class FormTab(val title: String, val collection: String, val color: Color)

@Composable
fun FormsList(collectionName: String, accentColor: Color) {
    val db = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var formList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(collectionName) {
        coroutineScope.launch {
            try {
                val snapshot = db.collection(collectionName).get().await()
                formList = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    data?.plus("id" to doc.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = accentColor
            )

            formList.isEmpty() -> Text(
                "No submissions found.",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray,
                fontSize = 18.sp
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(formList) { form ->
                    GenericFormCard(form, accentColor)
                }
            }
        }
    }
}

@Composable
fun GenericFormCard(form: Map<String, Any>, accentColor: Color) {
    val documents = form["documents"] as? Map<String, String> ?: emptyMap()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Submission ID: ${form["id"]}",
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = "Uploaded Documents:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            documents.forEach { (docName, url) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(docName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = docName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
