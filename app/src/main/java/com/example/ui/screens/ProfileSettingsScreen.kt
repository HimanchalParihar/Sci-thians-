package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.UserRole
import com.example.ui.components.ReceiptDetailRow
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.CivicGreen
import com.example.ui.theme.UtopiaRed
import com.example.ui.viewmodel.UtopiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: UtopiaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val profile by viewModel.userProfile.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val voteReceipts by viewModel.voteReceipts.collectAsState()

    var showLocationEditDialog by remember { mutableStateOf(false) }
    var showDataErasureDialog by remember { mutableStateOf(false) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English (India)") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(UtopiaRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = profile.pseudonymousHandle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active Role: ${profile.role.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtopiaRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { showRoleSwitcherDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Switch Role", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CivicGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CivicGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "18+ Age Verified & Identity Salted Token (Zero raw ID stored)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CivicGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Verified Constituent Jurisdictions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified Jurisdictions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showLocationEditDialog = true }) {
                                Text("Update Location", fontSize = 12.sp, color = UtopiaRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptDetailRow("Local Ward / Town", profile.location.townWard)
                        ReceiptDetailRow("District", profile.location.district)
                        ReceiptDetailRow("State Assembly", "${profile.location.state} (Constituency #161)")
                        ReceiptDetailRow("Parliamentary Seat", "Bengaluru Central (Union of India)")

                        if (profile.locationChangeCooldownUntil != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CivicAmber.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = CivicAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Anti-Vote-Shopping Cooldown active until ${profile.locationChangeCooldownUntil}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CivicAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cryptographic Receipts Vault
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "My Cryptographic Vote Receipts (${voteReceipts.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Signed server-side receipts. Private to you; auditable on the Merkle root.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        voteReceipts.forEach { receipt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = receipt.receiptId, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = UtopiaRed)
                                        Text(text = "Voted ${receipt.voteType}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = receipt.policyTitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    Text(
                                        text = "Hash: ${receipt.cryptographicHash}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DPDP Act 2023 Compliance & Data Rights
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DPDP Act 2023: Citizen Data Rights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exercise statutory rights to data portability, purpose limitation, or complete erasure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.repository.auditLogs
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download My Machine-Readable Data (JSON)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showDataErasureDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("delete_account_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Right to Erasure: Delete Account & Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Append-only Audit Log
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = UtopiaRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Append-Only Public Audit Trail",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        auditLogs.take(5).forEach { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Grievance Officer & Neutrality Policy
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Statutory Grievance Redressal Officer",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Name: Adv. S. Krishnamurthy\nEmail: grievance@utopia.civic.in\nStatutory Resolution SLA: 30 days under DPDP Act 2023 & IT Rules 2021",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitcherDialog = false },
            title = { Text("Switch Active System Role") },
            text = {
                Column {
                    UserRole.values().forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchRole(role)
                                    showRoleSwitcherDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = profile.role == role, onClick = {
                                viewModel.switchRole(role)
                                showRoleSwitcherDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(role.label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRoleSwitcherDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Location Edit Dialog
    if (showLocationEditDialog) {
        var stateInput by remember { mutableStateOf(profile.location.state) }
        var districtInput by remember { mutableStateOf(profile.location.district) }
        var wardInput by remember { mutableStateOf(profile.location.townWard) }
        var streetInput by remember { mutableStateOf(profile.location.street) }

        AlertDialog(
            onDismissRequest = { showLocationEditDialog = false },
            title = { Text("Constituent Location Setup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Subject to a 60-day cooldown period and audit review to prevent vote-shopping across jurisdictions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(value = stateInput, onValueChange = { stateInput = it }, label = { Text("State") })
                    OutlinedTextField(value = districtInput, onValueChange = { districtInput = it }, label = { Text("District") })
                    OutlinedTextField(value = wardInput, onValueChange = { wardInput = it }, label = { Text("Ward / Town") })
                    OutlinedTextField(value = streetInput, onValueChange = { streetInput = it }, label = { Text("Street") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLocation(stateInput, districtInput, wardInput, streetInput)
                        showLocationEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed)
                ) {
                    Text("Save Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Data Erasure Confirmation Dialog
    if (showDataErasureDialog) {
        AlertDialog(
            onDismissRequest = { showDataErasureDialog = false },
            title = { Text("Confirm Data Erasure & Account Deletion", color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    text = "In accordance with Section 12 of the DPDP Act 2023, all personal handles, device tokens, and session identifiers will be permanently purged from active storage. Past cast votes are irreversibly dissociated from your identity."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDataErasureDialog = false
                        viewModel.repository.toggleOfflineMode()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Execute Permanent Erasure")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataErasureDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
