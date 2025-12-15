package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val topBarColor = Color(0xFF0A2C78)
    val pageBackground = Color(0xFFF8F6F6)

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
                    containerColor = topBarColor
                )
            )
        },
        containerColor = pageBackground
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                WelcomeCard(auth = auth)
            }

            item { SectionTitle("Quick Actions") }

            item {
                GradientActionButton(
                    emoji = "📋",
                    title = "Scheme Booking Forms",
                    gradient = Brush.horizontalGradient(
                        //listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                        listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))
                    )
                ) { navController.navigate("view_forms") }
            }

            item {
                GradientActionButton(
                    emoji = "👥",
                    title = "View All Users",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))
                    )
                ) { navController.navigate("view_all_users") }
            }

            item {
                GradientActionButton(
                    emoji = "🗂️",
                    title = "Conveyance and Possession Forms",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))
                    )
                ) { navController.navigate("view_all_forms") }
            }

            item {
                GradientActionButton(
                    emoji = "🗂️",
                    title = "Home Loan Forms",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))
                    )
                ) { navController.navigate("view_all_homeloan_forms") }
            }

            item { SectionTitle("Rentals") }

            item {
                GradientActionButton(
                    emoji = "⬆️",
                    title = "Upload Rentals",
                    gradient = Brush.horizontalGradient(
                        //listOf(Color(0xFF7F00FF), Color(0xFF3D7BF9))
                        listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))

                    )
                ) { navController.navigate("upload_rental") }
            }

            item {
                GradientActionButton(
                    emoji = "🛠️",
                    title = "Manage Rentals",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))
                    )
                ) { navController.navigate("manage_rentals") }
            }

            item {
                GradientActionButton(
                    emoji = "📥",
                    title = "Send Notification",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFFFF9800), Color(0xFFEA2467))
                    )
                ) { navController.navigate("send_notification") }
            }

            item {
                GradientActionButton(
                    emoji = "📊",
                    title = "Download Monthly Report",
                    gradient = Brush.horizontalGradient(
                        //listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                        listOf(Color(0xFFEA2467), Color(0xFFFF9800))

                    )
                ) { navController.navigate("download_report") }
            }

            item {
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        //contentColor = Color(0xFF0A2C78)
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(auth: FirebaseAuth) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome, Admin!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A2C78)
            )
            Text(
                text = auth.currentUser?.email ?: "admin@dda.com",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A2C78)
        )
    }
}

@Composable
fun GradientActionButton(
    emoji: String,
    title: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical =3.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Emoji "icon" box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}