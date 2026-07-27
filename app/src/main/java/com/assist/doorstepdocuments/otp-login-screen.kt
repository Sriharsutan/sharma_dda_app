package com.assist.doorstepdocuments

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit

@Composable
fun PhoneOtpLoginScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as Activity

    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.dda_final_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Doorstep Documents Assist",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your documents, simplified.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

//            Text(
//                text = "Login with OTP",
//                color = Color.White,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold
//            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Login to Continue",
                        color = Color(0xFF0A2C78),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (!isOtpSent) {

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = ""
                            },
                            label = { Text("Mobile Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {

                                if (phoneNumber.length != 10) {
                                    errorMessage = "Enter valid 10-digit number"
                                    return@Button
                                }

                                isLoading = true

                                db.collection("user_details")
                                    .whereEqualTo("phone_number", phoneNumber)
                                    .get()
                                    .addOnSuccessListener { result ->

                                        if (result.isEmpty) {

                                            isLoading = false
                                            errorMessage = "Phone number not registered. Please signup."

                                        } else {

                                            val options = PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber("+91$phoneNumber")
                                                .setTimeout(60L, TimeUnit.SECONDS)
                                                .setActivity(activity)
                                                .setCallbacks(object :
                                                    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                        auth.signInWithCredential(credential)
                                                            .addOnCompleteListener { task ->
                                                                isLoading = false
                                                                if (task.isSuccessful) {
                                                                    navController.navigate("home") {
                                                                        popUpTo("otp_login") { inclusive = true }
                                                                    }
                                                                }
                                                            }
                                                    }

                                                    override fun onVerificationFailed(e: FirebaseException) {
                                                        isLoading = false
                                                        errorMessage = e.message ?: "OTP verification failed"
                                                    }

                                                    override fun onCodeSent(
                                                        verId: String,
                                                        token: PhoneAuthProvider.ForceResendingToken
                                                    ) {
                                                        isLoading = false
                                                        verificationId = verId
                                                        isOtpSent = true
                                                    }
                                                })
                                                .build()

                                            PhoneAuthProvider.verifyPhoneNumber(options)
                                        }

                                    }
                                    .addOnFailureListener { e ->

                                        isLoading = false
                                        errorMessage = "Database error: ${e.message}"

                                    }

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF0A2C78),
                                                Color(0xFF3EB8C4)
                                            )
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Send OTP",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = { navController.navigate("signup") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "New user? Signup!",
                                color = Color(0xFF0A2C78)
                            )
                        }

                        TextButton(
                            onClick = { navController.navigate("old_login") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Having problem with OTP? Login with username/password",
                                color = Color(0xFF0A2C78),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isOtpSent) {

                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                otp = it
                                errorMessage = ""
                            },
                            label = { Text("Enter OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {

                                if (otp.length != 6) {
                                    errorMessage = "Enter valid OTP"
                                    return@Button
                                }

                                isLoading = true

                                val credential = PhoneAuthProvider.getCredential(
                                    verificationId!!,
                                    otp
                                )

                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { task ->
                                        isLoading = false

                                        if (task.isSuccessful) {
                                            navController.navigate("home") {
                                                popUpTo("otp_login") { inclusive = true }
                                            }
                                        } else {
                                            errorMessage = "Invalid OTP"
                                        }
                                    }

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF0A2C78),
                                                Color(0xFF3EB8C4)
                                            )
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Verify OTP",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "© 2026 SAN Developers",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}