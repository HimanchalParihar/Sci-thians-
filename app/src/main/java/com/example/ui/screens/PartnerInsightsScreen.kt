package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.ReceiptDetailRow
import com.example.ui.components.formatNumber
import com.example.ui.theme.CivicGreen
import com.example.ui.theme.CivicPurple
import com.example.ui.theme.UtopiaRed
import com.example.ui.viewmodel.UtopiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerInsightsScreen(
    viewModel: UtopiaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val policies by viewModel.policies.collectAsState()
    var selectedInsightPolicy by remember { mutableStateOf(policies.firstOrNull()) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Civic Insights & Analytics (Partners)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("insights_back")) {
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
            // Privacy Shield Notice (k-anonymity)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CivicPurple.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CivicPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRIVACY-BY-DESIGN: k-ANONYMITY (k ≥ 10)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CivicPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Individual opinions are never accessible. Cells with fewer than 10 constituents are cryptographically suppressed to ensure absolute voter anonymity.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Ward Comparison Matrix
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bengaluru Urban: Ward Sentiment Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Comparing constituent approval on Clean Air & EV Public Transit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        WardSentimentRow("Ward 47 (Indiranagar)", 88, 2480)
                        WardSentimentRow("Ward 112 (Domlur)", 91, 1920)
                        WardSentimentRow("Ward 84 (Koramangala)", 82, 3140)
                        WardSentimentRow("Ward 174 (HSR Layout)", 86, 2810)
                        WardSentimentRow("Ward 19 (Malleswaram)", 79, 1650)
                    }
                }
            }

            // Anonymised Demographic Split
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Aggregated Demographic Cohorts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ReceiptDetailRow("18-29 Age Cohort", "62% of participants (89% Up)")
                        ReceiptDetailRow("30-49 Age Cohort", "28% of participants (84% Up)")
                        ReceiptDetailRow("50+ Age Cohort", "10% of participants (76% Up)")
                        ReceiptDetailRow("Differential Privacy Noise", "Laplace (ε = 0.5)")
                    }
                }
            }

            // Export Simulation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Institutional Anonymised Report Export",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exports contain only aggregated distribution percentiles and cryptographic Merkle roots.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                exportStatusText = "Exported: UTOPIA_AGGREGATE_REPORT_2026.json (Logged in Audit Trail #AUD-9912)"
                            },
                            modifier = Modifier.fillMaxWidth().testTag("export_insights_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Anonymised Dataset & Log Audit Entry")
                        }

                        if (exportStatusText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exportStatusText!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = CivicGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardSentimentRow(wardName: String, approvalPct: Int, sampleCount: Int) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = wardName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$approvalPct% Approval (${sampleCount.formatNumber()} citizens)",
                style = MaterialTheme.typography.labelSmall,
                color = if (approvalPct >= 80) CivicGreen else UtopiaRed,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { approvalPct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (approvalPct >= 80) CivicGreen else UtopiaRed,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
