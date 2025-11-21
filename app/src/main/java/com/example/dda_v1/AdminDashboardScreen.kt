package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2C78)
                )
            )
        }
    ) { padding ->

        // SAME BACKGROUND AS HOME SCREEN
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                    )
                )
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Welcome Box (Non-blurred)
                HomeGlassCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Welcome, Admin!",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            auth.currentUser?.email ?: "admin@dda.com",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Quick Actions",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // MENU ITEMS SAME AS HOME SCREEN
                HomeGlassMenu("📋 View Booking Forms") { navController.navigate("view_forms") }
                Spacer(modifier = Modifier.height(12.dp))
                HomeGlassMenu("👤 View All Users") { navController.navigate("view_all_users") }
                Spacer(modifier = Modifier.height(12.dp))
                HomeGlassMenu("🗂️ View Other Forms") { navController.navigate("view_all_forms") }

                Spacer(modifier = Modifier.height(16.dp))

                // RENTALS SECTION
                HomeGlassCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "🏠 Rentals",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HomeGlassMenuSmall("Upload Rentals") { navController.navigate("upload_rental") }
                        Spacer(modifier = Modifier.height(12.dp))
                        HomeGlassMenuSmall("Manage Rentals") { navController.navigate("manage_rentals") }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                HomeGlassMenu("🚪 Logout") {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}

// Reusable Glass UI
@Composable
fun HomeGlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Column(content = content)
        }
    }
}

@Composable
fun HomeGlassMenu(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun HomeGlassMenuSmall(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.20f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}