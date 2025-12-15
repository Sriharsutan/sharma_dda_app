package com.example.dda_v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.google.firebase.storage.FirebaseStorage
import coil.compose.AsyncImage

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

@Composable
fun RentalApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("show_notification") {ShowNotificationsScreen()}
        composable("rentals_available") { RentalListScreen() }
        composable("rentals_to_do") { DummyScreen("Rentals To Do") }
        composable("form") { FormScreen(navController) }
        //composable("form") { FormPage() }
        composable("documentation") { DocumentationScreen(navController) }
        composable("home_loan") {HomeLoanScreen(navController)}
        composable("conv_deed_form") { ConveyanceDeedFormScreen(navController) }
        composable("pletter_noc") { PossessionLetterNocFormScreen(navController) }
        composable("salaried") { SalariedFormScreen(navController) }
        composable("self-employeed") { SelfEmployeedFormScreen(navController) }

        //  Admin Routes
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("view_forms") { ViewFormsScreen(navController) }
        composable("view_all_users") { ViewAllUsersScreen(navController) }
        composable("view_all_forms") { AllFormsTabbedScreen(navController) }
        composable("view_all_homeloan_forms") { IncomeFormsScreen(navController)}
        composable("upload_rental") { UploadRentalScreen(navController)}
        composable("manage_rentals") {ManageRentalsScreen()}
        composable("send_notification") {NotificationsScreen(navController)}
        composable("download_report") {DownloadReportScreen(navController)}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    var username by remember { mutableStateOf("User") }
    val userId = auth.currentUser?.uid

    // Load username
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("user_details").document(userId).get().await()
                username = snapshot.getString("username") ?: "User"
            } catch (_: Exception) {}
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Welcome, $username",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A2C78))
            )
        },
        bottomBar = {
            //  FIXED BOTTOM LOGOUT BUTTON
            LogoutButton(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    ) { paddingValues ->

        // MAIN SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)   // ENABLE SCROLLING
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // ✔ Carousel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                ImageCarousel()
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            val context = LocalContext.current

            ShimmerNotificationButton(
                title = "Notifications",
                onClick = { navController.navigate("show_notification") }
            )

            GradientMenuButton(
                title = "Rentals Available",
                icon = "🏠",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))),
                onClick = { navController.navigate("rentals_available") }
            )

            GradientMenuButton(
                title = "Documentation Forms",
                icon = "📄",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF5B86E5), Color(0xFF36D1DC))),
                onClick = {
                    if (!isEmailVerifiedOrShowMessage(context)) return@GradientMenuButton
                    navController.navigate("documentation")
                }
            )

            GradientMenuButton(
                title = "Scheme Bookings Form",
                icon = "📝",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))),
                onClick = {
                    if (!isEmailVerifiedOrShowMessage(context)) return@GradientMenuButton
                    navController.navigate("form")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContactUsCard()

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun GradientMenuButton(
    title: String,
    subtitle: String? = null,
    icon: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutButton(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onLogout() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .width(150.dp)
            .height(50.dp)
    ) {
        Text(
            text = "Logout",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel() {
    val storage = FirebaseStorage.getInstance().reference
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Load images from Firebase Storage
    LaunchedEffect(Unit) {
        try {
            val folderRef = storage.child("home_page_carousal_images")
            val result = folderRef.listAll().await()

            val urls = result.items.map { it.downloadUrl.await().toString() }
            imageUrls = urls

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // If still loading → show shimmer or placeholder
    if (imageUrls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.LightGray.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    // Auto-scroll
    LaunchedEffect(pagerState.currentPage) {
        delay(3000)
        val next = (pagerState.currentPage + 1) % pagerState.pageCount
        scope.launch { pagerState.animateScrollToPage(next) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Carousel View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(state = pagerState) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = "Carousel Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dot Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .background(
                            if (selected) Color.White else Color.White.copy(0.4f),
                            RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

fun isEmailVerifiedOrShowMessage(context: Context): Boolean {
    val user = FirebaseAuth.getInstance().currentUser

    if (user == null) {
        Toast.makeText(context, "Please login first", Toast.LENGTH_LONG).show()
        return false
    }

    if (!user.isEmailVerified) {
        Toast.makeText(
            context,
            "Please verify your email before submitting the form",
            Toast.LENGTH_LONG
        ).show()
        return false
    }

    return true
}

@Composable
fun ContactUsCard() {
    val db = Firebase.firestore

    var address by remember { mutableStateOf("Loading address...") }
    var contacts by remember { mutableStateOf(emptyList<ContactPerson>()) }

    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("contact_info")
                .document("contact_details")
                .get()
                .await()

            address = doc.getString("address") ?: "No address available"

            @Suppress("UNCHECKED_CAST")
            val list = doc.get("contacts") as? List<Map<String, String>>
            contacts = list?.map {
                ContactPerson(
                    name = it["name"] ?: "",
                    phone = it["phone"] ?: ""
                )
            } ?: emptyList()

        } catch (e: Exception) {
            address = "Unable to load address"
            contacts = emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(Color(0xFFF1EFF5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Contact Us", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(14.dp))

            // Render ALL contacts dynamically
            contacts.forEach { contact ->
                ContactRow(contact)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Divider()

            Spacer(modifier = Modifier.height(10.dp))

            Text("Office Address", fontWeight = FontWeight.Bold)
            Text(address, color = Color.DarkGray)
        }
    }
}

data class ContactPerson(val name: String, val phone: String)

@Composable
fun ContactRow(contact: ContactPerson) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT SIDE: Name + Phone
        Column(
            modifier = Modifier
                .weight(1f) // ← Forces text to take left space
        ) {
            Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(contact.phone, color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // RIGHT SIDE: Call Button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A2C78)
            ),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
            modifier = Modifier.height(38.dp)   // ← FIX: Keeps button small & aligned
        ) {
            Text("Call", color = Color.White, fontSize = 14.sp)
        }
    }
}

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