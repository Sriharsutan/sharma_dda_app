package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(navController: NavController) {
    val topBarColor = Color(0xFF0A2C78)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentation", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
            )
        },
        containerColor = Color(0xFFF8F6F6)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Select a Document Form",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A2C78)
            )

            // Gradient buttons using emoji icons
            GradientDocButton(
                emoji = "📄",
                title = "Conveyance Deed Form",
                subtitle = "Submit your conveyance and deed documents",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF7F00FF), Color(0xFFE100FF))),
                onClick = { navController.navigate("conv_deed_form") }
            )

            GradientDocButton(
                emoji = "🏠",
                title = "Possession Letter & NOC",
                subtitle = "Upload possession letter and NOC forms",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))),
                onClick = { navController.navigate("pletter_noc") }
            )

            GradientDocButton(
                emoji = "💼",
                title = "Get Home Loan",
                subtitle = "Forms for salaried and self-employed",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF7F00FF), Color(0xFF3D7BF9))),
                onClick = { navController.navigate("home_loan") }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun GradientDocButton(
    emoji: String,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // emoji "icon"
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}