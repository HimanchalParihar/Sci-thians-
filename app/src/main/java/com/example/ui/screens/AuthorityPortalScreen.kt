package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Policy
import com.example.model.PolicyStatus
import com.example.ui.components.ReceiptDetailRow
import com.example.ui.components.VoteSplitBar
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.CivicGreen
import com.example.ui.theme.UtopiaRed
import com.example.ui.viewmodel.UtopiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorityPortalScreen(
    viewModel: UtopiaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val policies by viewModel.policies.collectAsState()
    val suggestionsMap by viewModel.suggestions.collectAsState()

    var selectedPolicyForResponse by remember { mutableStateOf<Policy?>(null) }
    var officialStatement by remember { mutableStateOf("") }
    var decisionStatus by remember { mutableStateOf("Accepted & Scheduled for Deployment") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Government Authority Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("authority_portal_back")) {
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
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CivicBlue.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CivicBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VERIFIED OFFICIAL DESK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CivicBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Review citizen votes, read constituent-ranked suggestions, and publish legally traceable official statements.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Delivered Constituent Evaluations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(policies) { policy ->
                val suggestions = suggestionsMap[policy.id] ?: emptyList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = policy.level.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UtopiaRed
                            )
                            Text(
                                text = "Statutory Due: ${policy.responseDueAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = policy.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        VoteSplitBar(
                            upPercentage = policy.upPercentage,
                            downPercentage = policy.downPercentage,
                            totalVotes = policy.totalVotes
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Top ranked suggestion preview
                        if (suggestions.isNotEmpty()) {
                            val topSug = suggestions.first()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Top Ranked Constituent Suggestion (+${topSug.netVotes} votes):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UtopiaRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = topSug.bodyText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (policy.authorityResponse != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CivicGreen.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CivicGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Official Response Published on ${policy.authorityResponse.respondedAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CivicGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    selectedPolicyForResponse = policy
                                    officialStatement = ""
                                },
                                modifier = Modifier.fillMaxWidth().testTag("respond_as_authority_${policy.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish Formal Authority Response", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to post official response
    if (selectedPolicyForResponse != null) {
        val targetPol = selectedPolicyForResponse!!
        AlertDialog(
            onDismissRequest = { selectedPolicyForResponse = null },
            title = { Text("Publish Official Authority Statement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Policy: ${targetPol.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = officialStatement,
                        onValueChange = { officialStatement = it },
                        label = { Text("Official Statement / Action Plan") },
                        modifier = Modifier.fillMaxWidth().testTag("official_statement_input"),
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = decisionStatus,
                        onValueChange = { decisionStatus = it },
                        label = { Text("Decision Status") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (officialStatement.isNotBlank()) {
                            viewModel.postAuthorityResponse(targetPol.id, officialStatement, decisionStatus)
                            selectedPolicyForResponse = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                    modifier = Modifier.testTag("confirm_authority_response")
                ) {
                    Text("Publish to Public Timeline")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPolicyForResponse = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
