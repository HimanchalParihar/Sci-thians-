package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.Campaign
import com.example.model.CampaignStatus
import com.example.model.CampaignType
import com.example.ui.components.ReceiptDetailRow
import com.example.ui.components.formatNumber
import com.example.ui.theme.*
import com.example.ui.viewmodel.UtopiaViewModel

@Composable
fun CampaignsScreen(
    viewModel: UtopiaViewModel,
    onOpenCampaign: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val campaigns by viewModel.campaigns.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Online Petitions, 2: Offline Assemblies
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredList = campaigns.filter { c ->
        when (selectedTab) {
            1 -> c.type == CampaignType.ONLINE
            2 -> c.type == CampaignType.OFFLINE
            else -> true
        }
    }

    Scaffold(
        containerColor = UtopiaBlackBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = UtopiaRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Start Campaign", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("start_campaign_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(UtopiaBlackBg)
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = UtopiaSurfaceDark,
                contentColor = UtopiaRed
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All Campaigns", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_campaigns_all"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Online Petitions", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_campaigns_online"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Offline Events", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_campaigns_offline"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("campaigns_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList, key = { it.id }) { campaign ->
                    CampaignCard(
                        campaign = campaign,
                        onSign = { viewModel.signCampaign(campaign.id) },
                        onRsvp = { viewModel.rsvpCampaign(campaign.id) },
                        onDonate = { viewModel.openDonateSheet(campaign) },
                        onClick = { onOpenCampaign(campaign.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCampaignDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, type, goal, demands, authority, loc, signatures, funds ->
                viewModel.createNewCampaign(title, type, goal, demands, authority, loc, signatures, funds)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CampaignCard(
    campaign: Campaign,
    onSign: () -> Unit,
    onRsvp: () -> Unit,
    onDonate: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("campaign_card_${campaign.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = com.example.ui.theme.CivicCardBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (campaign.illustrationRes != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = campaign.illustrationRes),
                        contentDescription = campaign.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x660A0F1D),
                                        Color(0x330A0F1D),
                                        UtopiaSurfaceDark
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (campaign.type == CampaignType.ONLINE) CivicCyan.copy(alpha = 0.25f) else UtopiaOrange.copy(alpha = 0.25f))
                                .border(1.dp, if (campaign.type == CampaignType.ONLINE) CivicCyan else UtopiaOrange, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = campaign.type.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (campaign.type == CampaignType.ONLINE) CivicCyan else UtopiaOrange
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UtopiaSurfaceDark.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = campaign.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UtopiaTextWhite
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (campaign.illustrationRes == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (campaign.type == CampaignType.ONLINE) CivicGreen.copy(alpha = 0.15f) else CivicAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = campaign.type.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (campaign.type == CampaignType.ONLINE) CivicGreen else CivicAmber
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = campaign.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

            Text(
                text = campaign.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Target: ${campaign.targetAuthority}",
                style = MaterialTheme.typography.labelSmall,
                color = UtopiaRed,
                fontWeight = FontWeight.Medium
            )

            if (campaign.locationOrVenue != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = campaign.locationOrVenue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = campaign.goalDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Signature Progress
            val sigProgress = (campaign.currentSignatures.toFloat() / maxOf(1, campaign.targetSignatures)).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${campaign.currentSignatures.formatNumber()} signed",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = UtopiaRed
                )
                Text(
                    text = "Goal: ${campaign.targetSignatures.formatNumber()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { sigProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = UtopiaRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Funds Progress if target funds > 0
            if (campaign.targetFunds > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val fundProgress = (campaign.raisedFunds / campaign.targetFunds).toFloat().coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹${"%.0f".format(campaign.raisedFunds)} in Escrow",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CivicGreen
                    )
                    Text(
                        text = "Budget: ₹${"%.0f".format(campaign.targetFunds)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: Sign / RSVP + Donate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (campaign.type == CampaignType.ONLINE) {
                    Button(
                        onClick = onSign,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("sign_campaign_${campaign.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (campaign.isSignedByMe) CivicGreen else UtopiaRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (campaign.isSignedByMe) Icons.Default.Check else Icons.Default.BorderColor,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (campaign.isSignedByMe) "Signed" else "Sign Petition", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onRsvp,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("rsvp_campaign_${campaign.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (campaign.isRsvpdByMe) CivicGreen else CivicAmber
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (campaign.isRsvpdByMe) Icons.Default.Check else Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (campaign.isRsvpdByMe) "Attending (RSVP)" else "RSVP Event", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onDonate,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("donate_button_${campaign.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Support", fontSize = 12.sp)
                }
            }
        }
    }
}
}

@Composable
fun CreateCampaignDialog(
    onDismiss: () -> Unit,
    onCreate: (String, CampaignType, String, List<String>, String, String?, Int, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CampaignType.ONLINE) }
    var goal by remember { mutableStateOf("") }
    var demandsText by remember { mutableStateOf("") }
    var targetAuthority by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var targetSignatures by remember { mutableStateOf("1000") }
    var targetFunds by remember { mutableStateOf("25000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_campaign_dialog"),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = "Create Civic Campaign",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Organise collective citizen action when authorities fail to respond.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == CampaignType.ONLINE,
                            onClick = { type = CampaignType.ONLINE },
                            label = { Text("Online Petition") }
                        )
                        FilterChip(
                            selected = type == CampaignType.OFFLINE,
                            onClick = { type = CampaignType.OFFLINE },
                            label = { Text("Offline Assembly") }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Campaign Title") },
                        modifier = Modifier.fillMaxWidth().testTag("campaign_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetAuthority,
                        onValueChange = { targetAuthority = it },
                        label = { Text("Target Authority (e.g. BBMP, Ministry)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (type == CampaignType.OFFLINE) {
                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Assembly Venue / Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Goal & Background") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = demandsText,
                        onValueChange = { demandsText = it },
                        label = { Text("Demands (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Demand 1, Demand 2") }
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetSignatures,
                            onValueChange = { targetSignatures = it },
                            label = { Text("Signatures") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetFunds,
                            onValueChange = { targetFunds = it },
                            label = { Text("Budget ₹") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Legal note: All campaigns adhere to non-violence standards and Indian assembly guidelines. Organizers undergo identity attestation.",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank() && targetAuthority.isNotBlank()) {
                                    val demandsList = demandsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    onCreate(
                                        title,
                                        type,
                                        goal,
                                        if (demandsList.isEmpty()) listOf("Formal public commitment") else demandsList,
                                        targetAuthority,
                                        location.takeIf { it.isNotBlank() },
                                        targetSignatures.toIntOrNull() ?: 500,
                                        targetFunds.toDoubleOrNull() ?: 10000.0
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("confirm_create_campaign"),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed)
                        ) {
                            Text("Launch")
                        }
                    }
                }
            }
        }
    }
}
