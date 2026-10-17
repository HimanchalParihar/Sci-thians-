package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.example.model.Campaign
import com.example.model.CampaignType
import com.example.ui.components.ReceiptDetailRow
import com.example.ui.components.formatNumber
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.CivicGreen
import com.example.ui.theme.UtopiaRed
import com.example.ui.viewmodel.UtopiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    viewModel: UtopiaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val campaign by viewModel.selectedCampaign.collectAsState()

    if (campaign == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UtopiaRed)
        }
        return
    }

    val c = campaign!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(c.type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("campaign_detail_back")) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (c.type == CampaignType.ONLINE) CivicGreen.copy(alpha = 0.15f) else CivicAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = c.type.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (c.type == CampaignType.ONLINE) CivicGreen else CivicAmber
                        )
                    }

                    Text(
                        text = "Status: ${c.status.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = c.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Target Authority: ${c.targetAuthority}",
                    style = MaterialTheme.typography.labelMedium,
                    color = UtopiaRed,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Organiser: ${c.organizerPseudonym} (Verified Identity Token)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress & Action Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val progress = (c.currentSignatures.toFloat() / maxOf(1, c.targetSignatures)).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${c.currentSignatures.formatNumber()} Constituent Signatures",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = UtopiaRed
                            )
                            Text(
                                text = "Goal: ${c.targetSignatures.formatNumber()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = UtopiaRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (c.type == CampaignType.ONLINE) {
                                Button(
                                    onClick = { viewModel.signCampaign(c.id) },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("sign_petition_action"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (c.isSignedByMe) CivicGreen else UtopiaRed
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(if (c.isSignedByMe) Icons.Default.Check else Icons.Default.BorderColor, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (c.isSignedByMe) "Signed" else "Sign Petition", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.rsvpCampaign(c.id) },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("rsvp_action"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (c.isRsvpdByMe) CivicGreen else CivicAmber
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(if (c.isRsvpdByMe) Icons.Default.Check else Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (c.isRsvpdByMe) "Attending (RSVP)" else "RSVP for Assembly", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.openDonateSheet(c) },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("donate_campaign_action"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fund Campaign", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Demands
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Core Demands Submitted to Authority",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        c.demands.forEachIndexed { idx, demand ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "${idx + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UtopiaRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = demand, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Offline Assembly Permit Guidance
            if (c.type == CampaignType.OFFLINE) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = CivicGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LEGAL ASSEMBLY & PERMIT NOTICE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CivicGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Venue: ${c.locationOrVenue ?: "Public Ground"}\nPermit: Police No-Objection Certificate granted. Strictly peaceful democratic assembly under Article 19(1)(b) of the Constitution of India.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Transparent Public Ledger of Income & Disbursements
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
                            Column {
                                Text(
                                    text = "Transparent Public Ledger",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RBI-compliant escrow • Platform fee: ${c.platformFeePercentage}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "₹${"%.0f".format(c.raisedFunds)} Total",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CivicGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (c.ledger.isEmpty()) {
                            Text(
                                text = "No disbursements yet. Funds held in audited escrow account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            c.ledger.forEach { entry ->
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
                                            Text(text = entry.type, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = UtopiaRed)
                                            Text(text = "₹${"%.2f".format(entry.grossAmount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        Text(text = entry.note, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Tx: ${entry.txHash.take(18)}...",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(text = entry.timestamp, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
