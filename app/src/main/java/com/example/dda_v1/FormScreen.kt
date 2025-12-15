package com.example.dda_v1

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class to hold image info (id + url)
data class UploadedImage(
    val imageId: String,
    val downloadUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val storage = Firebase.storage

    // State variables for form fields
    var userName by remember { mutableStateOf("") }
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
    var region by remember { mutableStateOf("Delhi") }

    // Dropdown states
    var bankExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val bankList = listOf(
        "Allahabad Bank", "Andhra Bank", "AU Small Finance Bank", "Axis Bank",
        "Bank of Baroda", "Bank of India", "Bank of Maharashtra", "Canara Bank",
        "Central Bank of India", "City Union Bank", "Corporation Bank",
        "Deutsche Bank", "Development Credit Bank", "Dhanlaxmi Bank",
        "Federal Bank", "HDFC Bank", "ICICI Bank", "IDBI Bank",
        "IDFC FIRST Bank", "Indian Bank", "Indian Overseas Bank",
        "IndusInd Bank", "ING Vysya Bank", "Jammu and Kashmir Bank",
        "Karnataka Bank Ltd", "Karur Vysya Bank", "Kotak Mahindra Bank",
        "Laxmi Vilas Bank", "Oriental Bank of Commerce", "Punjab & Sind Bank",
        "Punjab National Bank", "Shamrao Vitthal Co-operative Bank",
        "South Indian Bank", "Standard Chartered Bank", "State Bank of India",
        "Syndicate Bank", "Tamilnad Mercantile Bank Ltd",
        "The Delhi State Co Operative Banks", "The Kangra Coop Bank Ltd",
        "UCO Bank", "Union Bank of India"
    )

    val stateList = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
        "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan",
        "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh",
        "Uttarakhand", "West Bengal"
    )

    val districtMap = mapOf(
        "Delhi" to listOf(
            "Central Delhi", "East Delhi", "New Delhi", "North Delhi",
            "North East Delhi", "North West Delhi", "Shahdara", "South Delhi",
            "South East Delhi", "South West Delhi", "West Delhi"
        ),

        "Haryana" to listOf(
            "Ambala", "Bhiwani", "Charkhi Dadri", "Faridabad", "Fatehabad",
            "Gurugram", "Hisar", "Jhajjar", "Jind", "Kaithal", "Karnal",
            "Kurukshetra", "Mahendragarh", "Nuh", "Palwal", "Panchkula",
            "Panipat", "Rewari", "Rohtak", "Sirsa", "Sonipat", "Yamunanagar"
        ),

        "Madhya Pradesh" to listOf(
            "Agar Malwa", "Alirajpur", "Anuppur", "Ashoknagar", "Balaghat",
            "Barwani", "Betul", "Bhind", "Bhopal", "Burhanpur", "Chhatarpur",
            "Chhindwara", "Damoh", "Datia", "Dewas", "Dhar", "Dindori",
            "Guna", "Gwalior", "Harda", "Hoshangabad", "Indore", "Jabalpur",
            "Jhabua", "Katni", "Khandwa", "Khargone", "Mandla", "Mandsaur",
            "Morena", "Narsinghpur", "Neemuch", "Niwari", "Panna", "Raisen",
            "Rajgarh", "Ratlam", "Rewa", "Sagar", "Satna", "Sehore",
            "Seoni", "Shahdol", "Shajapur", "Sheopur", "Shivpuri", "Sidhi",
            "Singrauli", "Tikamgarh", "Ujjain", "Umaria", "Vidisha"
        ),

        "Arunachal Pradesh" to listOf(
            "Anjaw", "Changlang", "Dibang Valley", "East Kameng", "East Siang",
            "Kamle", "Kra Daadi", "Kurung Kumey", "Lepa Rada", "Lohit",
            "Longding", "Lower Dibang Valley", "Lower Siang", "Lower Subansiri",
            "Namsai", "Pakke Kessang", "Papum Pare", "Shi Yomi", "Siang",
            "Tawang", "Tirap", "Upper Siang", "Upper Subansiri", "West Kameng",
            "West Siang"
        ),

        "Assam" to listOf(
            "Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar",
            "Charaideo", "Chirang", "Darrang", "Dhemaji", "Dhubri",
            "Dibrugarh", "Dima Hasao", "Goalpara", "Golaghat", "Hailakandi",
            "Hojai", "Jorhat", "Kamrup", "Kamrup Metropolitan", "Karbi Anglong",
            "Karimganj", "Kokrajhar", "Lakhimpur", "Majuli", "Morigaon",
            "Nagaon", "Nalbari", "Sivasagar", "Sonitpur", "South Salmara-Mankachar",
            "Tinsukia", "Udalguri", "West Karbi Anglong"
        ),

        "Bihar" to listOf(
            "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai", "Bhagalpur",
            "Bhojpur", "Buxar", "Darbhanga", "East Champaran", "Gaya",
            "Gopalganj", "Jamui", "Jehanabad", "Kaimur", "Katihar", "Khagaria",
            "Kishanganj", "Lakhisarai", "Madhepura", "Madhubani", "Munger",
            "Muzaffarpur", "Nalanda", "Nawada", "Patna", "Purnia", "Rohtas",
            "Saharsa", "Samastipur", "Saran", "Sheikhpura", "Sheohar", "Sitamarhi",
            "Siwan", "Supaul", "Vaishali", "West Champaran"
        ),

        "Chhattisgarh" to listOf(
            "Balod", "Baloda Bazar", "Balrampur", "Bastar", "Bemetara",
            "Bijapur", "Bilaspur", "Dantewada", "Dhamtari", "Durg",
            "Gariaband", "Gaurela-Pendra-Marwahi", "Janjgir-Champa", "Jashpur",
            "Kabirdham", "Kanker", "Kondagaon", "Korba", "Koriya", "Mahasamund",
            "Mungeli", "Narayanpur", "Raigarh", "Raipur", "Rajnandgaon",
            "Sukma", "Surajpur", "Surguja"
        ),

        "Gujarat" to listOf(
            "Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha",
            "Bharuch", "Bhavnagar", "Botad", "Chhota Udaipur", "Dahod",
            "Dang", "Devbhoomi Dwarka", "Gandhinagar", "Gir Somnath", "Jamnagar",
            "Junagadh", "Kheda", "Kutch", "Mahisagar", "Mehsana", "Morbi",
            "Narmada", "Navsari", "Panchmahal", "Patan", "Porbandar",
            "Rajkot", "Sabarkantha", "Surat", "Surendranagar", "Tapi",
            "Vadodara", "Valsad"
        ),

        "Himachal Pradesh" to listOf(
            "Bilaspur", "Chamba", "Hamirpur", "Kangra", "Kinnaur", "Kullu",
            "Lahaul and Spiti", "Mandi", "Shimla", "Sirmaur", "Solan", "Una"
        ),

        "Jharkhand" to listOf(
            "Bokaro", "Chatra", "Deoghar", "Dhanbad", "Dumka", "East Singhbhum",
            "Garhwa", "Giridih", "Godda", "Gumla", "Hazaribagh", "Jamtara",
            "Khunti", "Koderma", "Latehar", "Lohardaga", "Pakur", "Palamu",
            "Ramgarh", "Ranchi", "Sahebganj", "Seraikela-Kharsawan", "Simdega",
            "West Singhbhum"
        ),

        "Maharashtra" to listOf(
            "Ahmednagar", "Akola", "Amravati", "Aurangabad", "Beed", "Bhandara",
            "Buldhana", "Chandrapur", "Dhule", "Gadchiroli", "Gondia", "Hingoli",
            "Jalgaon", "Jalna", "Kolhapur", "Latur", "Mumbai City", "Mumbai Suburban",
            "Nagpur", "Nanded", "Nandurbar", "Nashik", "Osmanabad", "Palghar",
            "Parbhani", "Pune", "Raigad", "Ratnagiri", "Sangli", "Satara",
            "Sindhudurg", "Solapur", "Thane", "Wardha", "Washim", "Yavatmal"
        ),

        "Rajasthan" to listOf(
            "Ajmer", "Alwar", "Banswara", "Baran", "Barmer", "Bharatpur",
            "Bhilwara", "Bikaner", "Bundi", "Chittorgarh", "Churu", "Dausa",
            "Dholpur", "Dungarpur", "Ganganagar", "Hanumangarh", "Jaipur",
            "Jaisalmer", "Jalore", "Jhalawar", "Jhunjhunu", "Jodhpur", "Karauli",
            "Kota", "Nagaur", "Pali", "Pratapgarh", "Rajsamand", "Sawai Madhopur",
            "Sikar", "Sirohi", "Tonk", "Udaipur"
        ),

        "Uttar Pradesh" to listOf(
            "Agra", "Aligarh", "Ambedkar Nagar", "Amethi", "Amroha", "Auraiya",
            "Azamgarh", "Baghpat", "Bahraich", "Ballia", "Balrampur", "Banda",
            "Barabanki", "Bareilly", "Basti", "Bhadohi", "Bijnor", "Budaun",
            "Bulandshahr", "Chandauli", "Chitrakoot", "Deoria", "Etah", "Etawah",
            "Ayodhya", "Farrukhabad", "Fatehpur", "Firozabad", "Gautam Buddha Nagar",
            "Ghaziabad", "Ghazipur", "Gonda", "Gorakhpur", "Hamirpur", "Hapur",
            "Hardoi", "Hathras", "Jalaun", "Jaunpur", "Jhansi", "Kannauj",
            "Kanpur Dehat", "Kanpur Nagar", "Kasganj", "Kaushambi", "Kheri",
            "Kushinagar", "Lalitpur", "Lucknow", "Maharajganj", "Mahoba",
            "Mainpuri", "Mathura", "Mau", "Meerut", "Mirzapur", "Moradabad",
            "Muzaffarnagar", "Pilibhit", "Pratapgarh", "Prayagraj", "Raebareli",
            "Rampur", "Saharanpur", "Sambhal", "Sant Kabir Nagar", "Shahjahanpur",
            "Shamli", "Shravasti", "Siddharthnagar", "Sitapur", "Sonbhadra",
            "Sultanpur", "Unnao", "Varanasi"
        ),

        "Uttarakhand" to listOf(
            "Almora", "Bageshwar", "Chamoli", "Champawat", "Dehradun",
            "Haridwar", "Nainital", "Pauri Garhwal", "Pithoragarh",
            "Rudraprayag", "Tehri Garhwal", "Udham Singh Nagar", "Uttarkashi"
        ),

        "West Bengal" to listOf(
            "Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur",
            "Darjeeling", "Hooghly", "Howrah", "Jalpaiguri", "Jhargram",
            "Kalimpong", "Kolkata", "Malda", "Murshidabad", "Nadia",
            "North 24 Parganas", "Paschim Bardhaman", "Paschim Medinipur",
            "Purba Bardhaman", "Purba Medinipur", "Purulia", "South 24 Parganas",
            "Uttar Dinajpur"
        ),

        "Karnataka" to listOf(
            "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
            "Bidar", "Chamarajanagar", "Chikballapur", "Chikkamagaluru", "Chitradurga",
            "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan",
            "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya",
            "Mysuru", "Raichur", "Ramanagara", "Shivamogga", "Tumakuru",
            "Udupi", "Uttara Kannada", "Vijayapura", "Yadgir"
        ),

        "Tamil Nadu" to listOf(
            "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
            "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kanchipuram",
            "Kanyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
            "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
            "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
            "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
            "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
            "Vellore", "Viluppuram", "Virudhunagar"
        )
    )

    // Document upload states
    var aadhaarUri by remember { mutableStateOf<Uri?>(null) }
    var panUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var signUri by remember { mutableStateOf<Uri?>(null) }

    // Progress states (0–100)
    var aadhaarProgress by remember { mutableStateOf(0) }
    var panProgress by remember { mutableStateOf(0) }
    var photoProgress by remember { mutableStateOf(0) }
    var signProgress by remember { mutableStateOf(0) }

    var isLoading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }

    // Image pickers
    val pickAadhaar = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            aadhaarUri = uri
        }
    }

    val pickPan = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            panUri = uri
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            photoUri = uri
        }
    }

    val pickSign = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            signUri = uri
        }
    }

    // --------- SUSPEND FUNCTION TO UPLOAD SINGLE IMAGE ------------
    suspend fun uploadImageToStorage(
        uri: Uri,
        filePrefix: String,
        onProgress: (Int) -> Unit
    ): UploadedImage? {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                return null
            }

            val imageId = System.currentTimeMillis().toString()
            //val submissionId = System.currentTimeMillis().toString()
            // Path: form_uploads/<userId>/<prefix>_<imageId>.jpg
            val storageRef = storage.reference
                .child("scheme_booking_form_uploads")
                .child(userId)
                .child("${filePrefix}_$imageId.jpg")

            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { snapshot ->
                val progress =
                    (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await()

            UploadedImage(
                imageId = imageId,
                downloadUrl = downloadUrl.toString()
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error uploading $filePrefix: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
    }

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
            // Personal Information
            SectionHeader(text = "Personal Information")

            FormTextField(
                value = userName,
                onValueChange = { userName = it },
                label = "User Name"
            )
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

            RadioButtonGroup(
                label = "Marital Status",
                options = listOf("Single", "Married"),
                selectedOption = maritalStatus,
                onOptionSelected = { maritalStatus = it }
            )

            if (maritalStatus == "Married") {
                FormTextField(
                    value = spouseName,
                    onValueChange = { spouseName = it },
                    label = "Spouse Name"
                )
            }

            FormTextField(
                value = category,
                onValueChange = { category = it },
                label = "Category"
            )

            // Bank Information
            SectionHeader(text = "Bank Information")

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

            // Nationality
            RadioButtonGroup(
                label = "Nationality",
                options = listOf("Indian", "Other"),
                selectedOption = nationality,
                onOptionSelected = { nationality = it }
            )

            // Address
            SectionHeader(text = "Address Information")

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

            DropdownField(
                value = addressState,
                label = "State",
                options = stateList,
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it },
                onOptionSelected = {
                    addressState = it
                    addressDistrict = ""
                }
            )

            val districtList = districtMap[addressState] ?: emptyList()
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

            RadioButtonGroup(
                label = "Region",
                options = listOf("Delhi", "Outside Delhi"),
                selectedOption = region,
                onOptionSelected = { region = it },
                displayPrefix = " "
            )

            // Document Uploads
            SectionHeader(text = "Document Uploads")

            // Aadhaar
            UploadButton("Upload Aadhaar Card", aadhaarUri) {
                pickAadhaar.launch("image/*")
            }
            if (aadhaarProgress > 0) {
                Text("Aadhaar upload: $aadhaarProgress%")
                LinearProgressIndicator(
                    progress = { aadhaarProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // PAN
            UploadButton("Upload PAN Card", panUri) {
                pickPan.launch("image/*")
            }
            if (panProgress > 0) {
                Text("PAN upload: $panProgress%")
                LinearProgressIndicator(
                    progress = { panProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Photo
            UploadButton("Upload Passport Size Photo", photoUri) {
                pickPhoto.launch("image/*")
            }
            if (photoProgress > 0) {
                Text("Photo upload: $photoProgress%")
                LinearProgressIndicator(
                    progress = { photoProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Signature
            UploadButton("Upload Signature", signUri) {
                pickSign.launch("image/*")
            }
            if (signProgress > 0) {
                Text("Signature upload: $signProgress%")
                LinearProgressIndicator(
                    progress = { signProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uploadStatus.isNotEmpty()) {
                Text(
                    text = uploadStatus,
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Button(
                    onClick = {
                        // Validate required fields
                        if (fatherName.isBlank() || bankName.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please fill all required fields.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (aadhaarUri == null || panUri == null ||
                            photoUri == null || signUri == null
                        ) {
                            Toast.makeText(
                                context,
                                "Please upload all four documents.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isLoading = true
                        uploadStatus = "Uploading documents..."

                        coroutineScope.launch {
                            try {
                                // Aadhaar
                                uploadStatus = "Uploading Aadhaar..."
                                aadhaarProgress = 0
                                val aadhaar = uploadImageToStorage(
                                    uri = aadhaarUri!!,
                                    filePrefix = "aadhaar",
                                    onProgress = { aadhaarProgress = it }
                                ) ?: run {
                                    isLoading = false
                                    uploadStatus = ""
                                    return@launch
                                }

                                // PAN
                                uploadStatus = "Uploading PAN..."
                                panProgress = 0
                                val pan = uploadImageToStorage(
                                    uri = panUri!!,
                                    filePrefix = "pan",
                                    onProgress = { panProgress = it }
                                ) ?: run {
                                    isLoading = false
                                    uploadStatus = ""
                                    return@launch
                                }

                                // Photo
                                uploadStatus = "Uploading Photo..."
                                photoProgress = 0
                                val photo = uploadImageToStorage(
                                    uri = photoUri!!,
                                    filePrefix = "photo",
                                    onProgress = { photoProgress = it }
                                ) ?: run {
                                    isLoading = false
                                    uploadStatus = ""
                                    return@launch
                                }

                                // Signature
                                uploadStatus = "Uploading Signature..."
                                signProgress = 0
                                val sign = uploadImageToStorage(
                                    uri = signUri!!,
                                    filePrefix = "signature",
                                    onProgress = { signProgress = it }
                                ) ?: run {
                                    isLoading = false
                                    uploadStatus = ""
                                    return@launch
                                }

                                uploadStatus = "Saving form data..."

                                val user = auth.currentUser
                                val formData = hashMapOf(
                                    "userId" to user?.uid,
                                    "userEmail" to user?.email,
                                    "username" to userName,
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

                                    // Image info: imageId + url
                                    "aadhaar" to mapOf(
                                        "imageId" to aadhaar.imageId,
                                        "url" to aadhaar.downloadUrl
                                    ),
                                    "pan" to mapOf(
                                        "imageId" to pan.imageId,
                                        "url" to pan.downloadUrl
                                    ),
                                    "photo" to mapOf(
                                        "imageId" to photo.imageId,
                                        "url" to photo.downloadUrl
                                    ),
                                    "signature" to mapOf(
                                        "imageId" to sign.imageId,
                                        "url" to sign.downloadUrl
                                    ),

                                    "timestamp" to FieldValue.serverTimestamp()
                                )

                                val userId = auth.currentUser!!.uid
                                val submissionId = System.currentTimeMillis().toString()
                                db.collection("user_forms")
                                    .document(userId)
                                    .collection("submissions")
                                    .document(submissionId)
                                    .set(formData)

//                                db.collection("user_forms")
//                                    .add(formData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Form Submitted Successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        isLoading = false
                                        uploadStatus = ""

                                        // Reset progress
                                        aadhaarProgress = 0
                                        panProgress = 0
                                        photoProgress = 0
                                        signProgress = 0

                                        navController.navigate("home") {
                                            popUpTo("form") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error saving data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isLoading = false
                                        uploadStatus = ""
                                    }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isLoading = false
                                uploadStatus = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        //containerColor = Color(Red)
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ----------------- REUSABLE COMPONENTS --------------------

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
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
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
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