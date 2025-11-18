package com.example.dda_v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RentalApp()
            }
        }
    }
}

@Composable
fun RentalApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }

        // Normal user routes
        composable("home") { HomeScreen(navController) }
        composable("rentals_available") { RentalListScreen(navController) }
        composable("rentals_to_do") { DummyScreen("Rentals To Do") }
        composable("form") { FormScreen(navController) }
        composable("documentation") { DocumentationScreen(navController) }
        composable("conv_deed_form") { ConveyanceDeedFormScreen(navController) }
        composable("pletter_noc") { PossessionLetterNocFormScreen(navController) }
        composable("salaried") { SalariedFormScreen(navController) }
        composable("self-employeed") { SelfEmployeedFormScreen(navController) }
        composable("doc_form5") { DocumentFormScreen("Form 5") }

        // Admin routes
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("view_forms") { ViewFormsScreen(navController) }
        composable("view_all_users") {ViewAllUsersScreen(navController)}
        composable("view_all_forms") { AllFormsTabbedScreen(navController) }
        composable("view_conveyance_forms") {
            FormsViewScreen(
                navController,
                collectionName = "conveyance_forms",
                screenTitle = "Conveyance & Deed Submissions",
                accentColor = Color(0xFF4106AB)
            )
        }

        composable("view_possession_forms") {
            FormsViewScreen(
                navController,
                collectionName = "possession_noc_forms",
                screenTitle = "Possession Letter & NOC Submissions",
                accentColor = Color(0xFF0072FF)
            )
        }

        composable("view_salaried_forms") {
            FormsViewScreen(
                navController,
                collectionName = "salaried_forms",
                screenTitle = "Salaried Form Submissions",
                accentColor = Color(0xFFF7971E)
            )
        }

        composable("view_business_forms") {
            FormsViewScreen(
                navController,
                collectionName = "business_forms",
                screenTitle = "Business Form Submissions",
                accentColor = Color(0xFF56AB2F)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    var username by remember { mutableStateOf("User") } // Default placeholder
    val userId = auth.currentUser?.uid

    // 🔹 Load username from Firebase Firestore (user_details collection)
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("user_details").document(userId).get().await()
                val name = snapshot.getString("username")
                if (!name.isNullOrEmpty()) {
                    username = name
                } else {
                    // fallback to Firebase Auth display name
                    username = auth.currentUser?.displayName ?: "User"
                }
            } catch (e: Exception) {
                username = auth.currentUser?.displayName ?: "User"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 🔹 Top App Bar with personalized greeting
        TopAppBar(
            title = {
                Text(
                    text = "Hi, $username",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF4106AB),
                titleContentColor = Color.White
            )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Carousel Section
            ImageCarousel()

            Spacer(modifier = Modifier.height(24.dp))

            // 4 boxes stacked vertically
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuBox(
                    title = "Rentals Available",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("rentals_available") },
                    gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                )
                MenuBox(
                    title = "Rentals To Do",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("rentals_to_do") },
                    gradientColors = listOf(Color(0xFFDF5CEE), Color(0xFFf5576c))
                )
                MenuBox(
                    title = "Application Form",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("form") },
                    gradientColors = listOf(Color(0xFF4facfe), Color(0xFF2B41C0))
                )
                MenuBox(
                    title = "Documentation",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("documentation") },
                    gradientColors = listOf(Color(0xFF2CE56A), Color(0xFF8BC34A))
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel() {
    // New PagerState initialization (requires lambda for page count)
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // New Jetpack Compose Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (page == 0) R.drawable.image1 else R.drawable.image2
                        ),
                        contentDescription = "Carousel Image ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Simple Dot Indicator (manual since HorizontalPagerIndicator is deprecated)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index) Color(0xFF6200EE) else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                        .background(color, shape = RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
fun MenuBox(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    gradientColors: List<Color> = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DummyScreen(pageName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "You entered this page:",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                text = pageName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
        }
    }
}