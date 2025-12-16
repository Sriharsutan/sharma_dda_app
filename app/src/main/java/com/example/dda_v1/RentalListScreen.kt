package com.example.dda_v1

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalListScreen() {

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
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        if (rentals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No rentals available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stay tuned, new rentals coming soon!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RentalCard(rental: Rental) {

    var liked by remember { mutableStateOf(false) }
    var showPhone by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val pagerState = rememberPagerState { rental.images.size }
    val viewers = remember { (10..40).random() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rental.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        rental.address,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { liked = !liked }) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (liked) Color.Red else Color.Gray
                    )
                }

                IconButton(onClick = { showPhone = !showPhone }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF6200EE)
                    )
                }
            }

            Box(modifier = Modifier.height(200.dp)) {

                if (rental.images.isNotEmpty()) {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    indication = null, //  important
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    selectedImage = rental.images[page]
                                }
                        ) {
                            AsyncImage(
                                model = rental.images[page],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                    }

                    /* Viewer badge */
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(bottomEnd = 8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "⚡ $viewers people viewing now",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    /* Page indicator */
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(rental.images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(2.dp)
                                    .background(
                                        if (pagerState.currentPage == index)
                                            Color.White else Color.LightGray,
                                        CircleShape
                                    )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Images", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(rental.furnishing, "Type")
                InfoItem(rental.area, "Area in Sqft.")
                InfoItem(rental.rent, "Rent")
            }

            if (showPhone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📞 Contact: +91 8076768383",
                    color = Color(0xFF051A86),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    selectedImage?.let { imageUrl ->
        Dialog(onDismissRequest = { selectedImage = null }) {

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
                        .clickable { selectedImage = null },
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 13.sp, color = Color.Gray)
    }
}