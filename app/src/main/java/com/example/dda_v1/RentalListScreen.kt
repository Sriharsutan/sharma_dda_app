package com.example.dda_v1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay

data class Rental(
    val title: String,
    val address: String,
    val images: List<Int>,
    val rent: String,
    val furnishing: String,
    val area: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun RentalListScreen(navController: NavController) {
    val rentals = listOf(
        Rental(
            "3 BHK Flat in Indiranagar for Rent",
            "13th E Main HAL 2nd Stage, Indiranagar, Bangalore",
            listOf(R.drawable.image1, R.drawable.image2, R.drawable.image1),
            "₹ 56,000",
            "Semi furnished",
            "2,020 Sq.Ft."
        ),
        Rental(
            "2 BHK Flat in Whitefield for Rent",
            "Nallurhalli Main Road, Whitefield, Bangalore",
            listOf(R.drawable.image2, R.drawable.image1),
            "₹ 42,000",
            "Fully furnished",
            "1,500 Sq.Ft."
        )
    )

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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RentalCard(rental: Rental) {
    var liked by remember { mutableStateOf(false) }
    var showPhone by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState()
    val viewers = remember { (10..40).random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Title + Icons row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rental.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = rental.address,
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
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF6200EE)
                    )
                }
            }

            // Image carousel
            Box(modifier = Modifier.height(180.dp)) {
                HorizontalPager(
                    count = rental.images.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = painterResource(id = rental.images[page]),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Viewer overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ $viewers people viewing this property now",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // Page indicator
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    activeColor = Color.White,
                    inactiveColor = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rent info row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rental.furnishing, fontWeight = FontWeight.Bold)
                    Text("Type", color = Color.Gray, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rental.area, fontWeight = FontWeight.Bold)
                    Text("Built Up Area", color = Color.Gray, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rental.rent, fontWeight = FontWeight.Bold)
                    Text("Rent", color = Color.Gray, fontSize = 13.sp)
                }
            }

            if (showPhone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📞 Contact: +91 98765 43210",
                    color = Color(0xFF051A86),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
