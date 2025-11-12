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
        composable("view_conveyance_forms") {ConveyanceFormsViewScreen(navController)}
    }

//    NavHost(navController = navController, startDestination = "login") {
//        composable("login") { LoginScreen(navController) }
//        composable("signup") { SignupScreen(navController) }
//        composable("home") { HomeScreen(navController) }
//        composable("rentals_available") {RentalListScreen(navController)}
//        //composable("rentals_available") { DummyScreen("Rentals Available") }
//        composable("rentals_to_do") { DummyScreen("Rentals To Do") }
//        composable("form") { FormScreen(navController) }
//        composable("documentation") { DummyScreen("Documentation") }
//    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Rental Management", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF4106AB),
                titleContentColor = Color.White
            )
        )

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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageCarousel() {
    val pagerState = rememberPagerState()

    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % 2
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            HorizontalPager(
                count = 2,
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
                    // Replace with your actual images
                    // Method 1: Using actual image resources
                    Image(
                        painter = painterResource(
                            id = if (page == 0) R.drawable.image1
                            else R.drawable.image2
                        ),
                        contentDescription = "Carousel Image ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Method 2: Keep placeholder boxes (comment out above and uncomment below)
                    /*
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (page == 0) Color(0xFF4CAF50) else Color(0xFF2196F3)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Image ${page + 1}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    */
                }
            }
        }

        // Dots indicator
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            activeColor = Color(0xFF6200EE),
            inactiveColor = Color.LightGray
        )
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