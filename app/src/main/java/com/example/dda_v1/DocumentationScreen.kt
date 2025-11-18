package com.example.dda_v1

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.People
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentation", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2C78),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A2C78),
                            Color(0xFF3EB8C4)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Select a Document Form",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Each Documentation Card
                DocumentationCard(
                    icon = Icons.Default.Description,
                    title = "Conveyance Deed Form",
                    subtitle = "Submit your conveyance and deed documents",
                    onClick = { navController.navigate("conv_deed_form") },
                    gradientColors = listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                )

                DocumentationCard(
                    icon = Icons.Default.HomeWork,
                    title = "Possession Letter & NOC Form",
                    subtitle = "Upload your possession letter and NOC forms",
                    onClick = { navController.navigate("pletter_noc") },
                    gradientColors = listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                )

                DocumentationCard(
                    icon = Icons.Default.People,
                    title = "Applicant & Co-Applicant (Salaried)",
                    subtitle = "Documents for salaried applicants",
                    onClick = { navController.navigate("salaried") },
                    gradientColors = listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                )

                DocumentationCard(
                    icon = Icons.Default.BusinessCenter,
                    title = "Self-Employed / Business",
                    subtitle = "Upload business or self-employed related docs",
                    onClick = { navController.navigate("self-employeed") },
                    gradientColors = listOf(Color(0xFF0A2C78), Color(0xFF3EB8C4))
                )
            }
        }
    }
}

@Composable
fun DocumentationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A2C78)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}