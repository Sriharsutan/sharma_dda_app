package com.example.dda_v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.ExperimentalFoundationApi

// 🌟 Main Activity
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

data class Rental(
    val id: String = "",
    val title: String = "",
    val address: String = "",
    val images: List<String> = emptyList(),
    val rent: String = "",
    val furnishing: String = "",
    val area: String = ""
)

// 🌈 Navigation Host
@Composable
fun RentalApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("rentals_available") { RentalListScreen(navController) }
        composable("rentals_to_do") { DummyScreen("Rentals To Do") }
        composable("form") { FormScreen(navController) }
        composable("documentation") { DocumentationScreen(navController) }
        composable("conv_deed_form") { ConveyanceDeedFormScreen(navController) }
        composable("pletter_noc") { PossessionLetterNocFormScreen(navController) }
        composable("salaried") { SalariedFormScreen(navController) }
        composable("self-employeed") { SelfEmployeedFormScreen(navController) }

        // 🧩 Admin Routes
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("view_forms") { ViewFormsScreen(navController) }
        composable("view_all_users") { ViewAllUsersScreen(navController) }
        composable("view_all_forms") { AllFormsTabbedScreen(navController) }
        composable("upload_rental") { UploadRentalScreen(navController)}
        composable("manage_rentals") {ManageRentalsScreen(navController)}
    }
}

// 🏠 Enhanced Home Screen (DDA Themed)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("User") }
    val userId = auth.currentUser?.uid

    // Load username from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("user_details").document(userId).get().await()
                username = snapshot.getString("username") ?: auth.currentUser?.displayName ?: "User"
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hi, $username",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2C78)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 📸 Carousel
                ImageCarousel()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Quick Actions",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🧭 Menu Grid
                MenuGrid(navController)
            }
        }
    }
}

// 🖼️ Image Carousel with Auto-scroll
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            scope.launch { pagerState.animateScrollToPage(nextPage) }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 📸 Carousel Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(state = pagerState) { page ->
                Image(
                    painter = painterResource(
                        id = if (page == 0) R.drawable.image1 else R.drawable.image2
                    ),
                    contentDescription = "Banner ${page + 1}",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillWidth // keeps full width, avoids zoom-crop
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dot Indicators
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            repeat(pagerState.pageCount) { index ->
                val color =
                    if (pagerState.currentPage == index) Color.White else Color.White.copy(0.4f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .background(color, RoundedCornerShape(50))
                )
            }
        }
    }
}

// 🧭 Menu Section
@Composable
fun MenuGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassMenuBox("🏠 Rentals Available") { navController.navigate("rentals_available") }
        GlassMenuBox("📄 Documentation") { navController.navigate("documentation") }
        GlassMenuBox("📝 Application Form") { navController.navigate("form") }
        //GlassMenuBox("📋 Rentals To Do") { navController.navigate("rentals_to_do") }
    }
}

// 🧊 Glass-style Menu Card
@Composable
fun GlassMenuBox(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(1.dp)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ⚙️ Dummy Screen
@Composable
fun DummyScreen(pageName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("You entered this page:", fontSize = 18.sp, color = Color.White)
            Text(
                pageName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}