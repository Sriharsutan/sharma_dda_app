// FormScreen.kt
// Location: dda_v1/app/src/main/java/com/example/dda_v1/FormScreen.kt
package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Application Form", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
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

            FormTextField(
                value = fatherName,
                onValueChange = { fatherName = it },
                label = "Father's Name"
            )

            FormTextField(
                value = motherName,
                onValueChange = { motherName = it },
                label = "Mother's Name"
            )

            // Marital Status Radio Buttons
            RadioButtonGroup(
                label = "Marital Status",
                options = listOf("Single", "Married"),
                selectedOption = maritalStatus,
                onOptionSelected = { maritalStatus = it }
            )

            // Spouse Name (conditional)
            if (maritalStatus == "Married") {
                FormTextField(
                    value = spouseName,
                    onValueChange = { spouseName = it },
                    label = "Spouse Name"
                )
            }

            // Category
            FormTextField(
                value = category,
                onValueChange = { category = it },
                label = "Category"
            )

            // Bank Information Section
            SectionHeader(
                text = "Bank Information",
                modifier = Modifier.padding(top = 16.dp)
            )

            // Bank Name Dropdown
            DropdownField(
                value = bankName,
                label = "Bank Name",
                options = bankList,
                expanded = bankExpanded,
                onExpandedChange = { bankExpanded = it },
                onOptionSelected = { bankName = it }
            )

            FormTextField(
                value = branchName,
                onValueChange = { branchName = it },
                label = "Branch Name"
            )

            FormTextField(
                value = ifscCode,
                onValueChange = { ifscCode = it },
                label = "IFSC Code"
            )

            FormTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = "Account Number"
            )

            // Nationality Radio Buttons
            RadioButtonGroup(
                label = "Nationality",
                options = listOf("Indian", "Other"),
                selectedOption = nationality,
                onOptionSelected = { nationality = it },
                modifier = Modifier.padding(top = 16.dp)
            )

            // Address Section
            SectionHeader(
                text = "Address Information",
                modifier = Modifier.padding(top = 16.dp)
            )

            FormTextField(
                value = addressHouse,
                onValueChange = { addressHouse = it },
                label = "House Number"
            )

            FormTextField(
                value = addressStreet,
                onValueChange = { addressStreet = it },
                label = "Street"
            )

            FormTextField(
                value = addressArea,
                onValueChange = { addressArea = it },
                label = "Area/Sector"
            )

            // State Dropdown
            DropdownField(
                value = addressState,
                label = "State",
                options = stateList,
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it },
                onOptionSelected = { addressState = it }
            )

            // District Dropdown
            DropdownField(
                value = addressDistrict,
                label = "District",
                options = districtList,
                expanded = districtExpanded,
                onExpandedChange = { districtExpanded = it },
                onOptionSelected = { addressDistrict = it }
            )

            FormTextField(
                value = addressPin,
                onValueChange = { addressPin = it },
                label = "PIN Code"
            )

            // Region Radio Buttons
            RadioButtonGroup(
                label = "Region",
                options = listOf("1", "2"),
                selectedOption = region,
                onOptionSelected = { region = it },
                modifier = Modifier.padding(top = 16.dp),
                displayPrefix = "Region "
            )

            // Submit Button
            Button(
                onClick = {
                    // TODO: Store values in database
                    println("Form Submitted")
                    println("Father: $fatherName, Mother: $motherName")
                    println("Marital Status: $maritalStatus, Spouse: $spouseName")
                    println("Category: $category")
                    println("Bank: $bankName, Branch: $branchName")
                    println("IFSC: $ifscCode, Account: $accountNumber")
                    println("Nationality: $nationality")
                    println("Address: $addressHouse, $addressStreet, $addressArea")
                    println("State: $addressState, District: $addressDistrict, PIN: $addressPin")
                    println("Region: $region")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text(
                    text = "Submit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Reusable Components

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6200EE),
        modifier = modifier
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
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
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedOption == option,
                            onClick = { onOptionSelected(option) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { onOptionSelected(option) }
                    )
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
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