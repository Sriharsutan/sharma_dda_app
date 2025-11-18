package com.example.dda_v1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SignupScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var mobileno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sign up to get started",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Signup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE)
                    )

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            focusedLabelColor = Color(0xFF6200EE)
                        )
                    )

                    // Phone number Field
                    OutlinedTextField(
                        value = mobileno,
                        onValueChange = {
                            mobileno = it
                            errorMessage = ""
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            focusedLabelColor = Color(0xFF6200EE)
                        )
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            focusedLabelColor = Color(0xFF6200EE)
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            focusedLabelColor = Color(0xFF6200EE)
                        )
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (confirmPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            focusedLabelColor = Color(0xFF6200EE)
                        )
                    )

                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // OK Button
                    Button(
                        onClick = {
                            when {
                                username.isEmpty() || email.isEmpty() ||
                                        password.isEmpty() || confirmPassword.isEmpty() -> {
                                    errorMessage = "Please fill all fields"
                                }
                                !email.contains("@") -> {
                                    errorMessage = "Please enter a valid email"
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                }
                                else -> {
                                    // TODO: Replace with Firebase Authentication
                                    // Temporary: Show success dialog for testing
                                    println("Signup - Username: $username, Email: $email")
                                    showSuccessDialog = true

                                    //Firebase code - uncomment when Firebase is set up:
                                    isLoading = true
                                    val auth = FirebaseAuth.getInstance()
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
//                                            if (task.isSuccessful) {
//                                                val user = auth.currentUser
//                                                val profileUpdates = UserProfileChangeRequest.Builder()
//                                                    .setDisplayName(username)
//                                                    .build()
//
//                                                user?.updateProfile(profileUpdates)
//                                                    ?.addOnCompleteListener { profileTask ->
//                                                        isLoading = false
//                                                        if (profileTask.isSuccessful) {
//                                                            showSuccessDialog = true
//                                                        } else {
//                                                            showSuccessDialog = true
//                                                        }
//                                                    }
//                                            } else {
//                                                isLoading = false
//                                                errorMessage = task.exception?.message
//                                                    ?: "Signup failed. Please try again."
//                                            }
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(username)
                                                    .build()

                                                user?.updateProfile(profileUpdates)
                                                    ?.addOnCompleteListener { profileTask ->
                                                        isLoading = false

                                                        if (profileTask.isSuccessful) {
                                                            // Store user details in Firestore
                                                            val db = Firebase.firestore
                                                            val userData = hashMapOf(
                                                                "username" to username,
                                                                "phone_number" to mobileno,
                                                                "email" to email
                                                            )

                                                            // Use user UID as document ID
                                                            db.collection("user_details")
                                                                .document(user!!.uid)
                                                                .set(userData)
                                                                .addOnSuccessListener {
                                                                    showSuccessDialog = true
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    errorMessage = "Signup succeeded but failed to save details: ${e.message}"
                                                                }

                                                        } else {
                                                            errorMessage = "Profile update failed."
                                                        }
                                                    }
                                            } else {
                                                isLoading = false
                                                errorMessage = task.exception?.message
                                                    ?: "Signup failed. Please try again."
                                            }

                                        }

                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "OK",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Back to Login Button
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account? Login",
                            color = Color(0xFF6200EE),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Success!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE)
                )
            },
            text = {
                Text("Account created successfully! Please login to continue.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Go to Login")
                }
            },
            containerColor = Color.White
        )
    }
}