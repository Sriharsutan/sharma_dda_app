package com.example.dda_v1

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLoanScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Loan", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2C78)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 📌 Salaried Applicants Button
            GradientDocButton(
                emoji = "👤",
                title = "Applicant & Co-Applicant for Salaried",
                subtitle = "Documents for salaried applicants",
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))
                ),
                onClick = { navController.navigate("salaried") }
            )

            // 📌 Self-Employed Applicants Button
            GradientDocButton(
                emoji = "💼",
                title = "Form for Self-Employed / Business",
                subtitle = "Upload business or self-employed documents",
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFF7F00FF), Color(0xFF3D7BF9))
                ),
                onClick = { navController.navigate("self-employeed") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Section Title
            Text(
                text = "We have tie up from all these banks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A2C78)
            )

            // Bank Logos Horizontal Scroll
            AutoScrollingBankLogos()
        }
    }
}


@Composable
fun AutoScrollingBankLogos() {
    val logos = listOf(
        R.drawable.sbi,
        R.drawable.pnb,
        R.drawable.bob,
        R.drawable.icici,
        R.drawable.hdfc,
        R.drawable.axis
    )

    // Duplicate list so scrolling appears infinite
    val repeatedLogos = remember { logos + logos }

    val scrollState = rememberScrollState(0)
    //val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val maxScroll = scrollState.maxValue

            // Auto-scroll speed
            scrollState.animateScrollTo(
                value = maxScroll,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 10000,  // scroll speed
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
            // Reset to start
            scrollState.scrollTo(0)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeatedLogos.forEach { logo ->
            Card(
                modifier = Modifier
                    .size(120.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = painterResource(id = logo),
                    contentDescription = "Bank Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}