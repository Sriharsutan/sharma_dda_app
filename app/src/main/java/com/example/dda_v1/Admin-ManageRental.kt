package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRentalsScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var rentals by remember { mutableStateOf<List<Rental>>(emptyList()) }

    // Fetch all rentals
    LaunchedEffect(true) {
        db.collection("rentals").addSnapshotListener { value, _ ->
            rentals = value?.documents?.map { doc ->
                Rental(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    address = doc.getString("address") ?: "",
                    rent = doc.getString("rent") ?: "",
                    furnishing = doc.getString("furnishing") ?: "",
                    area = doc.getString("area") ?: "",
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Rentals") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EE), titleContentColor = Color.White)
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rentals) { rental ->
                ManageRentalCard(
                    rental = rental,
                    onEdit = {
                        navController.navigate("edit_rental/${rental.id}")
                    },
                    onDelete = {
                        deleteRental(rental, db, storage)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ManageRentalCard(
    rental: Rental,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val pagerState = rememberPagerState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Title row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(rental.title, fontWeight = FontWeight.Bold)
                Text(rental.rent)
            }

            Text(rental.address, color = Color.Gray, maxLines = 1)

            // Image carousel
            if (rental.images.isNotEmpty()) {
                HorizontalPager(
                    count = rental.images.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) { page ->
                    AsyncImage(
                        model = rental.images[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Admin edit + delete
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
//                Button(
//                    onClick = onEdit,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF4CAF50),
//                        contentColor = Color.White
//                    )
//                ) {
//                    Icon(Icons.Filled.Edit, contentDescription = null)
//                    Spacer(Modifier.width(6.dp))
//                    Text("Edit")
//                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

fun deleteRental(
    rental: Rental,
    db: FirebaseFirestore,
    storage: FirebaseStorage
) {
    // Delete images
    rental.images.forEach { url ->
        storage.getReferenceFromUrl(url).delete()
    }

    // Delete Firestore document
    db.collection("rentals").document(rental.id).delete()
}