package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CivicGreen
import com.example.ui.theme.UtopiaRed
import com.example.ui.viewmodel.UtopiaViewModel

@Composable
fun OnboardingScreen(
    viewModel: UtopiaViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) } // 0: Neutrality Charter, 1: Mobile OTP, 2: KYC Consent & 18+, 3: Location Setup

    // Mobile & OTP State
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var otpInput by remember { mutableStateOf("123456") }
    var otpSent by remember { mutableStateOf(false) }

    // KYC State
    var aadhaarLast4 by remember { mutableStateOf("8812") }
    var birthYear by remember { mutableStateOf("1998") }
    var consentAgreed by remember { mutableStateOf(true) }

    // Location State
    var stateInput by remember { mutableStateOf("Karnataka") }
    var districtInput by remember { mutableStateOf("Bengaluru Urban") }
    var wardInput by remember { mutableStateOf("Ward 47 (Indiranagar)") }
    var streetInput by remember { mutableStateOf("100 Feet Road") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Utopia Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(UtopiaRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (step) {
                    0 -> {
                        // Step 0: Neutrality Charter
                        Text(
                            text = "Welcome to Utopia",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "A way for absolute democracy in India. Shape every government policy, from your street up to Parliament.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "NON-NEGOTIABLE PRINCIPLES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = UtopiaRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• 1 Verified Person = 1 Account = 1 Vote", style = MaterialTheme.typography.bodySmall)
                                Text("• Zero algorithmic amplification or boosting", style = MaterialTheme.typography.bodySmall)
                                Text("• Pseudonymous handles: real identity never exposed", style = MaterialTheme.typography.bodySmall)
                                Text("• Verifiable signed cryptographic receipts", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 1 },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Get Started", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    1 -> {
                        // Step 1: Mobile & OTP
                        Text(
                            text = "Mobile Verification",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Secured with rate limits & SIM change velocity detection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number") },
                            prefix = { Text("+91 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("mobile_input")
                        )

                        if (otpSent) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("Enter 6-Digit OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("otp_input")
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!otpSent) {
                            Button(
                                onClick = {
                                    val res = viewModel.repository.kycProvider.requestOtp(mobileNumber)
                                    res.onSuccess {
                                        otpSent = true
                                        errorMessage = null
                                    }.onFailure {
                                        errorMessage = it.message
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Request OTP (Sandbox: 123456)", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val res = viewModel.repository.kycProvider.verifyOtp(mobileNumber, otpInput)
                                    res.onSuccess {
                                        step = 2
                                        errorMessage = null
                                    }.onFailure {
                                        errorMessage = it.message
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("verify_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Verify OTP & Proceed", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // Step 2: KYC Consent & 18+ Verification
                        Text(
                            text = "Zero-Storage KYC (18+)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aadhaar Offline e-KYC sandbox adapter. Generates an irreversible salted token without storing raw ID documents.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = aadhaarLast4,
                            onValueChange = { if (it.length <= 4) aadhaarLast4 = it },
                            label = { Text("Aadhaar Last 4 Digits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("aadhaar_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = { birthYear = it },
                            label = { Text("Year of Birth (Mandatory 18+)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("birth_year_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = consentAgreed, onCheckedChange = { consentAgreed = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I consent to cryptographic token generation under Section 6 of DPDP Act 2023.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val yr = birthYear.toIntOrNull() ?: 2000
                                val res = viewModel.repository.kycProvider.verifyAadhaarOfflineConsent(aadhaarLast4, yr)
                                res.onSuccess {
                                    step = 3
                                    errorMessage = null
                                }.onFailure {
                                    errorMessage = it.message
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("verify_kyc_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                            shape = RoundedCornerShape(8.dp),
                            enabled = consentAgreed
                        ) {
                            Text("Attest 18+ & Generate Token", fontWeight = FontWeight.Bold)
                        }
                    }

                    3 -> {
                        // Step 3: Location Setup & Constituent Jurisdictions
                        Text(
                            text = "Jurisdiction Setup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Derives your local municipal ward, state assembly, and parliamentary voting areas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(value = stateInput, onValueChange = { stateInput = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = districtInput, onValueChange = { districtInput = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = wardInput, onValueChange = { wardInput = it }, label = { Text("Ward / Town") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = streetInput, onValueChange = { streetInput = it }, label = { Text("Street") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.updateLocation(stateInput, districtInput, wardInput, streetInput)
                                onComplete()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("complete_onboarding_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enter Utopia Civic Platform", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
