package com.assist.doorstepdocuments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun RentalListScreen()
fun RentalListScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    var rentals by remember { mutableStateOf<List<Rental>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("rentals").addSnapshotListener { value, _ ->
            rentals = value?.documents?.mapNotNull { doc ->
                Rental(
                    title = doc.getString("title") ?: "",
                    address = doc.getString("address") ?: "",
                    rent = doc.getString("rent") ?: "",
                    furnishing = doc.getString("furnishing") ?: "",
                    area = doc.getString("area") ?: "",
                    phone = doc.getString("phone") ?: "",
                    images = (doc.get("images") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
            } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rentals Available", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2C78),
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate("add_rental")
                        }
                    ) {
                        Text("Add Rental", color = Color.White)
                    }
                }
            )

//            TopAppBar(
//                title = { Text("Rentals Available", fontWeight = FontWeight.Bold) },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF0A2C78),
//                    titleContentColor = Color.White
//                )
//            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFCF8F8))
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rentals) { rental ->
                RentalCard(rental)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RentalCard(rental: Rental) {

    var liked by remember { mutableStateOf(false) }
    var showPhone by remember { mutableStateOf(false) }
    var expandedAddress by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    val pagerState = rememberPagerState { rental.images.size }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            /* Header */
            Row(verticalAlignment = Alignment.CenterVertically) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(rental.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Text(
                        rental.address,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = if (expandedAddress) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            expandedAddress = !expandedAddress
                        }
                    )

                    Text(
                        if (expandedAddress) "Show less" else "Show more",
                        fontSize = 12.sp,
                        color = Color(0xFF0A2C78),
                        modifier = Modifier.clickable {
                            expandedAddress = !expandedAddress
                        }
                    )
                }

                IconButton(onClick = { liked = !liked }) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (liked) Color.Red else Color.Gray
                    )
                }

                IconButton(onClick = { showPhone = !showPhone }) {
                    Icon(Icons.Filled.Call, null, tint = Color(0xFF6200EE))
                }
            }

            Spacer(Modifier.height(8.dp))

            /* Image carousel */
            if (rental.images.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) { page ->
                    AsyncImage(
                        model = rental.images[page],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                selectedImageIndex = page
                            }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(rental.furnishing, "Type")
                InfoItem(rental.area, "Area")
                InfoItem(rental.rent, "Rent")
            }

            if (showPhone) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "📞 Contact: +91 8076768383",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF051A86)
                )
            }
        }
    }

    selectedImageIndex?.let { startIndex ->

        val fullPagerState = rememberPagerState(
            initialPage = startIndex,
            pageCount = { rental.images.size }
        )

        val coroutineScope = rememberCoroutineScope()

        Dialog(onDismissRequest = { selectedImageIndex = null }) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f)
                    .background(Color.Black, RoundedCornerShape(16.dp))
            ) {

                HorizontalPager(
                    state = fullPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    ZoomableImage(rental.images[page])
                }

                // Close button
                IconButton(
                    onClick = { selectedImageIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Left arrow
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (fullPagerState.currentPage > 0) {
                                fullPagerState.animateScrollToPage(
                                    fullPagerState.currentPage - 1
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                ) {
                    Text("◀", color = Color.White, fontSize = 28.sp)
                }

                // Right arrow
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (fullPagerState.currentPage < rental.images.lastIndex) {
                                fullPagerState.animateScrollToPage(
                                    fullPagerState.currentPage + 1
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                ) {
                    Text("▶", color = Color.White, fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String) {

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offset += pan
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .transformable(state),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun InfoItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 13.sp, color = Color.Gray)
    }
}
