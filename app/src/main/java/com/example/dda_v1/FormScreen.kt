package com.example.dda_v1

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import android.widget.Toast
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavHostController) {

    val coroutineScope = rememberCoroutineScope()  // <-- define this at top of FormScreen()

    // State variables for form fields
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("Single") }
    var spouseName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var bankName by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Indian") }
    var addressHouse by remember { mutableStateOf("") }
    var addressStreet by remember { mutableStateOf("") }
    var addressArea by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var addressDistrict by remember { mutableStateOf("") }
    var addressPin by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("1") }

    // Dropdown states
    var bankExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val bankList = listOf("State Bank of India", "Bank of Baroda", "HDFC Bank", "ICICI Bank", "Punjab National Bank", "Axis Bank")
    val stateList = listOf("Delhi", "Maharashtra", "Karnataka", "Tamil Nadu", "Gujarat", "Rajasthan")
    val districtList = listOf("North", "South", "East", "West", "North West", "North East", "South West", "South East", "Central")

    // Document upload states
    val imageMimeTypes = arrayOf("image/png", "image/jpeg", "image/jpg")

    var aadhaarUri by remember { mutableStateOf<Uri?>(null) }
    var panUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var signUri by remember { mutableStateOf<Uri?>(null) }

    val aadhaarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) aadhaarUri = uri
    }
    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) panUri = uri
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri
    }
    val signLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) signUri = uri
    }

    // --- Firebase and State variables ---
    val context = LocalContext.current
    // --- REMOVED: val auth = Firebase.auth ---
    val db = Firebase.firestore

    var isLoading by remember { mutableStateOf(false) }

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Application Form", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0284BE),
                titleContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Personal Information Section
            SectionHeader(text = "Personal Information")

            FormTextField(value = fatherName, onValueChange = { fatherName = it }, label = "Father's Name")
            FormTextField(value = motherName, onValueChange = { motherName = it }, label = "Mother's Name")

            RadioButtonGroup(
                label = "Marital Status",
                options = listOf("Single", "Married"),
                selectedOption = maritalStatus,
                onOptionSelected = { maritalStatus = it }
            )

            if (maritalStatus == "Married") {
                FormTextField(value = spouseName, onValueChange = { spouseName = it }, label = "Spouse Name")
            }

            FormTextField(value = category, onValueChange = { category = it }, label = "Category")

            // Bank Information
            SectionHeader(text = "Bank Information", modifier = Modifier.padding(top = 16.dp))

            DropdownField(
                value = bankName,
                label = "Bank Name",
                options = bankList,
                expanded = bankExpanded,
                onExpandedChange = { bankExpanded = it },
                onOptionSelected = { bankName = it }
            )

            FormTextField(value = branchName, onValueChange = { branchName = it }, label = "Branch Name")
            FormTextField(value = ifscCode, onValueChange = { ifscCode = it }, label = "IFSC Code")
            FormTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = "Account Number")

            // Nationality
            RadioButtonGroup(
                label = "Nationality",
                options = listOf("Indian", "Other"),
                selectedOption = nationality,
                onOptionSelected = { nationality = it },
                modifier = Modifier.padding(top = 16.dp)
            )

            // Address Section
            SectionHeader(text = "Address Information", modifier = Modifier.padding(top = 16.dp))

            FormTextField(value = addressHouse, onValueChange = { addressHouse = it }, label = "House Number")
            FormTextField(value = addressStreet, onValueChange = { addressStreet = it }, label = "Street")
            FormTextField(value = addressArea, onValueChange = { addressArea = it }, label = "Area/Sector")

            DropdownField(
                value = addressState,
                label = "State",
                options = stateList,
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it },
                onOptionSelected = { addressState = it }
            )

            DropdownField(
                value = addressDistrict,
                label = "District",
                options = districtList,
                expanded = districtExpanded,
                onExpandedChange = { districtExpanded = it },
                onOptionSelected = { addressDistrict = it }
            )

            FormTextField(value = addressPin, onValueChange = { addressPin = it }, label = "PIN Code")

            RadioButtonGroup(
                label = "Region",
                options = listOf("1", "2"),
                selectedOption = region,
                onOptionSelected = { region = it },
                modifier = Modifier.padding(top = 16.dp),
                displayPrefix = "Region "
            )

            // Document Upload Section
            SectionHeader(text = "Document Uploads", modifier = Modifier.padding(top = 16.dp))

            UploadButton("Upload Aadhaar Card", aadhaarUri) {
                aadhaarLauncher.launch("image/*")
            }
            UploadButton("Upload PAN Card", panUri) {
                panLauncher.launch("image/*")
            }
            UploadButton("Upload Passport Size Photo", photoUri) {
                photoLauncher.launch("image/*")
            }
            UploadButton("Upload Signature", signUri) {
                signLauncher.launch("image/*")
            }

            Spacer(modifier = Modifier.height(16.dp))

//            // Submit Button
//            Button(
//                onClick = {
//                    println("Form Submitted")
//                    println("Aadhaar: $aadhaarUri")
//                    println("PAN: $panUri")
//                    println("Photo: $photoUri")
//                    println("Sign: $signUri")
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//                    .padding(vertical = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF6200EE)
//                )
//            ) {
//                Text(text = "Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//            }
//
//        }
//    }
//}
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (fatherName.isBlank() || bankName.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true

                            val formData = hashMapOf(
                                "fatherName" to fatherName,
                                "motherName" to motherName,
                                "maritalStatus" to maritalStatus,
                                "spouseName" to if (maritalStatus == "Married") spouseName else null,
                                "category" to category,
                                "bankName" to bankName,
                                "branchName" to branchName,
                                "ifscCode" to ifscCode,
                                "accountNumber" to accountNumber,
                                "nationality" to nationality,
                                "addressHouse" to addressHouse,
                                "addressStreet" to addressStreet,
                                "addressArea" to addressArea,
                                "addressState" to addressState,
                                "addressDistrict" to addressDistrict,
                                "addressPin" to addressPin,
                                "region" to region,
                                "aadhaarUri" to aadhaarUri?.toString(),
                                "panUri" to panUri?.toString(),
                                "photoUri" to photoUri?.toString(),
                                "signUri" to signUri?.toString(),
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )

                            db.collection("user_forms")
                                .add(formData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Form Submitted Successfully!", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                    coroutineScope.launch {
                                        delay(1000)
                                        navController.navigate("home") {
                                            popUpTo("form") { inclusive = true }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text(text = "Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// Reusable Components
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF051C9B),
        modifier = modifier
    )
}

@Composable
fun FormTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun RadioButtonGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    displayPrefix: String = ""
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .selectable(selected = selectedOption == option, onClick = { onOptionSelected(option) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedOption == option, onClick = { onOptionSelected(option) })
                    Text(text = displayPrefix + option)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    value: String,
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun UploadButton(label: String, imageUri: Uri?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF777D80)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = label, fontWeight = FontWeight.Bold)
        }

        if (imageUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}