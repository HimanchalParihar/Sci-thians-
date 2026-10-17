package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadingMode
import com.example.ui.viewmodel.UtopiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyDetailScreen(
    viewModel: UtopiaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val policy by viewModel.selectedPolicy.collectAsState()
    val userVotes by viewModel.userVotes.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val suggestionsMap by viewModel.suggestions.collectAsState()

    var detailTab by remember { mutableIntStateOf(0) } // 0: Evaluation & Story, 1: Suggestions, 2: Authority Clock
    var suggestionInput by remember { mutableStateOf("") }
    var suggestionLinkInput by remember { mutableStateOf("") }
    var expandedClauseIndex by remember { mutableIntStateOf(0) }

    if (policy == null) {
        Box(modifier = modifier.fillMaxSize().background(UtopiaBlackBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UtopiaRed)
        }
        return
    }

    val p = policy!!
    val userVote = userVotes[p.id]
    val suggestions = suggestionsMap[p.id] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = p.level.displayName + " Decision",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = p.jurisdictionName,
                            style = MaterialTheme.typography.labelSmall,
                            color = UtopiaTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { viewModel.toggleReadingMode() },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = UtopiaSurfaceElevatedDark,
                            contentColor = UtopiaOrange
                        )
                    ) {
                        Icon(
                            imageVector = if (readingMode == ReadingMode.FORMAL) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (readingMode == ReadingMode.FORMAL) "Formal" else "Story", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UtopiaBlackBg
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(UtopiaBlackBg)
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = detailTab,
                containerColor = UtopiaSurfaceDark,
                contentColor = UtopiaRed
            ) {
                Tab(
                    selected = detailTab == 0,
                    onClick = { detailTab = 0 },
                    text = { Text("Evaluation", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_policy_eval"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
                Tab(
                    selected = detailTab == 1,
                    onClick = { detailTab = 1 },
                    text = { Text("Suggestions (${suggestions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_policy_suggestions"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
                Tab(
                    selected = detailTab == 2,
                    onClick = { detailTab = 2 },
                    text = { Text("Authority Clock", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_policy_timeline"),
                    selectedContentColor = UtopiaRed,
                    unselectedContentColor = UtopiaTextMuted
                )
            }

            when (detailTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Image Illustration
                        if (p.illustrationRes != 0) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Image(
                                            painter = painterResource(id = p.illustrationRes),
                                            contentDescription = p.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0x330A0F1D),
                                                            Color(0x880A0F1D),
                                                            Color(0xEE0A0F1D)
                                                        )
                                                    )
                                                )
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                                .align(Alignment.BottomCenter),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = CivicGreen.copy(alpha = 0.25f),
                                                border = BorderStroke(1.dp, CivicGreen.copy(alpha = 0.6f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CivicGreen))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${p.upPercentage}% Agree",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = CivicGreen
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "🗳️ ${p.totalVotes.formatNumber()} Verified Ballots",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = UtopiaTextWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Title & Badges
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LevelBadge(p.level)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(p.status)
                                }
                                Text(
                                    text = "Closing: ${p.closesAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UtopiaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Issuing Body: ${p.issuingBody}",
                                style = MaterialTheme.typography.labelMedium,
                                color = UtopiaOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // High-Energy Voting Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CONSTITUENT VERDICT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = UtopiaRed,
                                            letterSpacing = 1.sp
                                        )

                                        Text(
                                            text = "1 Person = 1 Vote",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CivicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    VoteSplitBar(
                                        upPercentage = p.upPercentage,
                                        downPercentage = p.downPercentage,
                                        totalVotes = p.totalVotes
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.castVote(p.id, true) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("detail_vote_up"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (userVote == true) CivicGreen else UtopiaSurfaceElevatedDark,
                                                contentColor = if (userVote == true) Color.Black else Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (userVote == true) "Voted Agree" else "Agree (Up)", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.castVote(p.id, false) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("detail_vote_down"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (userVote == false) UtopiaRed else UtopiaSurfaceElevatedDark,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.ThumbDown, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (userVote == false) "Voted Oppose" else "Oppose (Down)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Reading Mode: Story Mode vs Formal Mode
                        if (readingMode == ReadingMode.INTERACTIVE) {
                            item {
                                Text(
                                    text = "Story Highlights & Visual Explainer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reviewed on ${p.explainer.lastReviewedDate} by ${p.explainer.editorReviewer}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UtopiaTextMuted
                                )
                            }

                            // Interactive Story Cards
                            items(p.explainer.storyCards) { card ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0x33FF2A4B), Color(0x22FFFFFF))))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(UtopiaRed.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Bolt, contentDescription = null, tint = UtopiaOrange, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = card,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceElevatedDark)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "🎯 What This Changes For You",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = UtopiaGold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        p.explainer.keyTakeaways.forEach { takeaway ->
                                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CivicGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = takeaway, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // FORMAL MODE: Interactive Accordion / Expandable Clauses
                            item {
                                Text(
                                    text = "Official Gazette Clauses (Tap to Expand)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            items(p.clauses.mapIndexed { i, c -> Pair(i, c) }) { (idx, clause) ->
                                val isExpanded = expandedClauseIndex == idx
                                Card(
                                    onClick = { expandedClauseIndex = if (isExpanded) -1 else idx },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                    border = BorderStroke(1.dp, if (isExpanded) UtopiaRed else UtopiaBorderDark)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = clause.clauseNumber,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = UtopiaOrange
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = clause.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = UtopiaTextMuted
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = clause.plainSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UtopiaTextMuted
                                        )

                                        AnimatedVisibility(visible = isExpanded) {
                                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                                HorizontalDivider(color = UtopiaBorderDark)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Statutory Gazette Text:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = UtopiaOrange,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = clause.officialText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Ranked Suggestions Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Submit a Citizen Suggestion",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Top-voted suggestions are packaged and delivered to ${p.issuingBody}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UtopiaTextMuted
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = suggestionInput,
                                        onValueChange = { if (it.length <= 350) suggestionInput = it },
                                        modifier = Modifier.fillMaxWidth().testTag("suggestion_input_field"),
                                        placeholder = { Text("What specific amendment or improvement do you suggest?", fontSize = 13.sp, color = UtopiaTextMuted) },
                                        maxLines = 4,
                                        supportingText = {
                                            Text(
                                                text = "${suggestionInput.length}/350 chars",
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth(),
                                                color = UtopiaTextMuted
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtopiaRed,
                                            unfocusedBorderColor = UtopiaBorderDark,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = suggestionLinkInput,
                                        onValueChange = { suggestionLinkInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Optional supporting study or gazette link", fontSize = 12.sp, color = UtopiaTextMuted) },
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = UtopiaTextMuted) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtopiaRed,
                                            unfocusedBorderColor = UtopiaBorderDark,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (suggestionInput.isNotBlank()) {
                                                viewModel.submitSuggestion(p.id, suggestionInput, suggestionLinkInput)
                                                suggestionInput = ""
                                                suggestionLinkInput = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("submit_suggestion_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                                        shape = RoundedCornerShape(10.dp),
                                        enabled = suggestionInput.trim().length >= 10
                                    ) {
                                        Text("Publish Suggestion for Public Ranking", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(suggestions, key = { it.id }) { sug ->
                            SuggestionItemCard(
                                suggestion = sug,
                                onVoteUp = { viewModel.voteSuggestion(p.id, sug.id, true) },
                                onVoteDown = { viewModel.voteSuggestion(p.id, sug.id, false) },
                                onReport = { viewModel.openReportDialog(p.id, sug.id) }
                            )
                        }
                    }
                }

                2 -> {
                    // Authority Response & Clock
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                                border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "STATUTORY ACCOUNTABILITY WINDOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UtopiaOrange
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = p.issuingBody,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Statutory Response Due: ${p.responseDueAt}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = UtopiaTextMuted
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    TimelineStep(
                                        isCompleted = true,
                                        title = "Policy Ingested & Verified",
                                        subtitle = "Published from Gazette on ${p.opensAt}"
                                    )
                                    TimelineStep(
                                        isCompleted = true,
                                        title = "Constituent Public Evaluation",
                                        subtitle = "${p.totalVotes.formatNumber()} citizens voted • Evaluation active"
                                    )
                                    TimelineStep(
                                        isCompleted = p.status == PolicyStatus.DELIVERED || p.status == PolicyStatus.RESPONDED,
                                        title = "Automated Official Delivery",
                                        subtitle = "Delivered to Department Portal with Merkle root receipt"
                                    )
                                    TimelineStep(
                                        isCompleted = p.status == PolicyStatus.RESPONDED,
                                        title = "Authority Statement",
                                        subtitle = if (p.authorityResponse != null) "Responded on ${p.authorityResponse.respondedAt}" else "Pending official action"
                                    )
                                }
                            }
                        }

                        if (p.authorityResponse != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = CivicGreen.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, CivicGreen)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = CivicGreen)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "OFFICIAL AUTHORITY STATEMENT",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CivicGreen
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = p.authorityResponse.officialStatement,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Status: ${p.authorityResponse.decisionStatus} • Date: ${p.authorityResponse.respondedAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CivicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceElevatedDark),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, tint = UtopiaOrange)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "NO RESPONSE? ESCALATE TO CAMPAIGN",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = UtopiaOrange
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "If ${p.issuingBody} fails to respond by ${p.responseDueAt}, constituents can launch an organized collective campaign with online petitions or offline rallies.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UtopiaTextMuted
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = { viewModel.createCampaignFromPolicy(p) },
                                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("launch_campaign_escalation_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Launch Citizen Campaign for this Policy", fontWeight = FontWeight.Bold)
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

@Composable
fun SuggestionItemCard(
    suggestion: Suggestion,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = BorderStroke(1.dp, UtopiaBorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(UtopiaSurfaceElevatedDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp), tint = UtopiaOrange)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion.authorPseudonym,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = suggestion.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = UtopiaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = suggestion.bodyText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            if (!suggestion.supportingLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = UtopiaOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = suggestion.supportingLink,
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaOrange,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onVoteUp,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (suggestion.userVote == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Support",
                            tint = if (suggestion.userVote == 1) CivicGreen else UtopiaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${suggestion.netVotes}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (suggestion.netVotes > 0) CivicGreen else UtopiaTextMuted
                    )
                    IconButton(
                        onClick = onVoteDown,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (suggestion.userVote == -1) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Oppose",
                            tint = if (suggestion.userVote == -1) UtopiaRed else UtopiaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onReport,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Outlined.Flag,
                        contentDescription = "Report abuse",
                        tint = UtopiaTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineStep(
    isCompleted: Boolean,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isCompleted) CivicGreen else UtopiaBorderDark),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = UtopiaTextMuted)
        }
    }
}

