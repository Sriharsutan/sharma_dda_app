package com.example.dda_v1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentation", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4106AB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select a Document Form",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4106AB),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 5 documentation buttons
            DocumentationButton("Conveyance Deed Form", onClick = { navController.navigate("conv_deed_form") })
            DocumentationButton("Possession Letter and NOC Form", onClick = { navController.navigate("pletter_noc") })
            DocumentationButton("Applicant & Co-applicant (Salaried)", onClick = { navController.navigate("salaried") })
            DocumentationButton("Self-employeed / Business", onClick = { navController.navigate("self-employeed") })
            DocumentationButton("Form 5", onClick = { navController.navigate("doc_form5") })
        }
    }
}

@Composable
fun DocumentationButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentFormScreen(formTitle: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(formTitle, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4106AB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Form for $formTitle will appear here", fontSize = 20.sp)
        }
    }
}