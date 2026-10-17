package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.Policy
import com.example.model.PolicyLevel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.UtopiaViewModel

@Composable
fun HomeScreen(
    viewModel: UtopiaViewModel,
    onOpenPolicy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val policies by viewModel.filteredPolicies.collectAsState()
    val allPolicies by viewModel.policies.collectAsState()
    val userVotes by viewModel.userVotes.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showSavedOnly by viewModel.showSavedOnly.collectAsState()

    var showDailyVerdictModal by remember { mutableStateOf(false) }

    val topics = listOf("All", "Privacy", "Environment", "Drainage", "Transit", "Civic", "Energy")

    Column(modifier = modifier.fillMaxSize().background(UtopiaBlackBg)) {
        // Live Civic Pulse Ticker
        LiveCivicPulseTicker()

        // Level Switcher Tabs: All, Local, State, Central
        TabRow(
            selectedTabIndex = when (selectedLevel) {
                null -> 0
                PolicyLevel.LOCAL -> 1
                PolicyLevel.STATE -> 2
                PolicyLevel.CENTRAL -> 3
            },
            containerColor = UtopiaSurfaceDark,
            contentColor = UtopiaRed
        ) {
            Tab(
                selected = selectedLevel == null,
                onClick = { viewModel.setLevelFilter(null) },
                text = { Text("All Levels", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_level_all"),
                selectedContentColor = UtopiaRed,
                unselectedContentColor = UtopiaTextMuted
            )
            Tab(
                selected = selectedLevel == PolicyLevel.LOCAL,
                onClick = { viewModel.setLevelFilter(PolicyLevel.LOCAL) },
                text = { Text("Local Ward", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_level_local"),
                selectedContentColor = UtopiaRed,
                unselectedContentColor = UtopiaTextMuted
            )
            Tab(
                selected = selectedLevel == PolicyLevel.STATE,
                onClick = { viewModel.setLevelFilter(PolicyLevel.STATE) },
                text = { Text("State", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_level_state"),
                selectedContentColor = UtopiaRed,
                unselectedContentColor = UtopiaTextMuted
            )
            Tab(
                selected = selectedLevel == PolicyLevel.CENTRAL,
                onClick = { viewModel.setLevelFilter(PolicyLevel.CENTRAL) },
                text = { Text("Central Union", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_level_central"),
                selectedContentColor = UtopiaRed,
                unselectedContentColor = UtopiaTextMuted
            )
        }

        // Search & Filter controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("search_policy_input"),
                placeholder = { Text("Search decisions, gazettes...", fontSize = 13.sp, color = UtopiaTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp),
                        tint = UtopiaTextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UtopiaRed,
                    unfocusedBorderColor = UtopiaBorderDark,
                    unfocusedContainerColor = UtopiaSurfaceDark,
                    focusedContainerColor = UtopiaSurfaceDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconToggleButton(
                checked = showSavedOnly,
                onCheckedChange = { viewModel.toggleSavedFilter() },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("saved_filter_toggle"),
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    checkedContainerColor = UtopiaRed.copy(alpha = 0.25f),
                    checkedContentColor = UtopiaOrange,
                    containerColor = UtopiaSurfaceDark,
                    contentColor = UtopiaTextMuted
                )
            ) {
                Icon(
                    imageVector = if (showSavedOnly) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Show Saved Only",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Horizontal Topic Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                val isSelected = selectedTopic == topic
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setTopicFilter(topic) },
                    label = { Text(topic, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UtopiaRed,
                        selectedLabelColor = Color.White,
                        containerColor = UtopiaSurfaceElevatedDark,
                        labelColor = UtopiaTextMuted
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) UtopiaRed else UtopiaBorderDark
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("policies_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Civic Hero Banner with Illustration & Quick Call to Action
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ill_civic_hero),
                            contentDescription = "Civic Democracy Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x440A0F1D),
                                            Color(0xBB0A0F1D),
                                            Color(0xFA0A0F1D)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = UtopiaRed.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, UtopiaRed.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CivicCyan))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("LIVE CONSTITUENT FORUM", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CivicCyan)
                                    }
                                }

                                Text("⚡ 1-Citizen-1-Vote", fontSize = 10.sp, color = UtopiaGold, fontWeight = FontWeight.Bold)
                            }

                            Column {
                                Text(
                                    text = "Shape India's Next Decision",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Evaluate upcoming policies, submit suggestions, and audit ward funds.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UtopiaTextMuted
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CivicGreen, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DPDP 2023 Verified", fontSize = 11.sp, color = CivicGreen, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showDailyVerdictModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("30s Verdict", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Civic Impact Card
            item {
                CivicVoiceImpactCard(
                    karmaScore = userProfile.karmaScore,
                    townWard = userProfile.location.townWard,
                    onDailyVerdictClick = { showDailyVerdictModal = true }
                )
            }

            // Trending Civic Stories Carousel
            item {
                TrendingStoriesReel(
                    policies = allPolicies,
                    onSelectPolicy = onOpenPolicy
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Constituent Decisions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${policies.size} Open",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (policies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = null,
                                tint = UtopiaTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No decisions match active filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'All Levels' or reset topic filter to see more.",
                                style = MaterialTheme.typography.bodySmall,
                                color = UtopiaTextMuted
                            )
                        }
                    }
                }
            } else {
                items(policies, key = { it.id }) { policy ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PolicyCard(
                            policy = policy,
                            userVote = userVotes[policy.id],
                            onVoteUp = { viewModel.castVote(policy.id, true) },
                            onVoteDown = { viewModel.castVote(policy.id, false) },
                            onToggleSave = { viewModel.toggleSavePolicy(policy.id) },
                            onClick = { onOpenPolicy(policy.id) }
                        )
                    }
                }
            }
        }
    }

    // Daily Quick Ballot / 30-Second Verdict Modal
    if (showDailyVerdictModal) {
        val quickPolicy = allPolicies.firstOrNull { it.id == "pol_local_01" } ?: allPolicies.first()
        Dialog(onDismissRequest = { showDailyVerdictModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Illustration Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ill_quick_ballot),
                            contentDescription = "Daily Quick Ballot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Today's 30-Second Verdict",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "⚡ ${quickPolicy.jurisdictionName} • Instant Impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaOrange,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = UtopiaSurfaceElevatedDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = quickPolicy.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = quickPolicy.plainSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = UtopiaTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.castVote(quickPolicy.id, true)
                                showDailyVerdictModal = false
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CivicGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agree", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.castVote(quickPolicy.id, false)
                                showDailyVerdictModal = false
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Oppose", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
