package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ReadingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtopiaTopAppBar(
    currentLocation: UserLocation,
    readingMode: ReadingMode,
    isOffline: Boolean,
    onToggleReadingMode: () -> Unit,
    onToggleOffline: () -> Unit,
    onOpenAuthorityPortal: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenProfile: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CivicEmberBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "UTOPIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CivicGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "DEMOCRACY 2.0",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CivicGreen
                            )
                        }
                    }
                    Text(
                        text = "${currentLocation.townWard} • ${currentLocation.state}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            // Mode toggle
            FilledTonalButton(
                onClick = onToggleReadingMode,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(34.dp)
                    .testTag("mode_toggle_button"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (readingMode == ReadingMode.FORMAL) UtopiaSurfaceElevatedDark else UtopiaRed.copy(alpha = 0.25f),
                    contentColor = if (readingMode == ReadingMode.FORMAL) Color.White else UtopiaRedLight
                )
            ) {
                Icon(
                    imageVector = if (readingMode == ReadingMode.FORMAL) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (readingMode == ReadingMode.FORMAL) "Formal" else "Story",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Options menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("menu_more_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(UtopiaSurfaceElevatedDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile & Identity (18+ Verified)") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CivicGreen) },
                        onClick = {
                            menuExpanded = false
                            onOpenProfile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Authority Portal (Officials)") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CivicBlue) },
                        onClick = {
                            menuExpanded = false
                            onOpenAuthorityPortal()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Civic Insights & Analytics") },
                        leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = CivicPurple) },
                        onClick = {
                            menuExpanded = false
                            onOpenInsights()
                        }
                    )
                    HorizontalDivider(color = UtopiaBorderDark)
                    DropdownMenuItem(
                        text = { Text(if (isOffline) "Go Online" else "Simulate Offline Mode") },
                        leadingIcon = {
                            Icon(
                                if (isOffline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOffline) CivicGreen else MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleOffline()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = UtopiaBlackBg
        )
    )
}

@Composable
fun LiveCivicPulseTicker(
    modifier: Modifier = Modifier
) {
    val liveEvents = remember {
        listOf(
            "🟢 Citizen in Ward 47 cast vote on Stormwater Canal Desilting",
            "🔥 34 new verified signatures on Domlur Skywalk Campaign",
            "🏛️ BBMP acknowledged constituent feedback report #BLR-47",
            "✨ 92% approval in Karnataka for Electric Public Transit Scheme"
        )
    }
    var currentIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4500)
            currentIdx = (currentIdx + 1) % liveEvents.size
        }
    }

    Surface(
        color = UtopiaSurfaceElevatedDark,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CivicGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PULSE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = CivicGreen,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = liveEvents[currentIdx],
                style = MaterialTheme.typography.bodySmall,
                color = UtopiaTextWhite,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CivicVoiceImpactCard(
    karmaScore: Int,
    townWard: String,
    onDailyVerdictClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("civic_impact_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(UtopiaRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = UtopiaOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Citizen Impact: Level 3",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔥 3-Day Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = UtopiaGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$karmaScore Civic Karma • Top 5% in $townWard",
                            style = MaterialTheme.typography.labelSmall,
                            color = UtopiaTextMuted
                        )
                    }
                }

                Button(
                    onClick = onDailyVerdictClick,
                    colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daily Pulse", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TrendingStoriesReel(
    policies: List<Policy>,
    onSelectPolicy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Trending Civic Stories",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Live Tally",
                style = MaterialTheme.typography.labelSmall,
                color = UtopiaOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            policies.take(4).forEach { p ->
                Card(
                    onClick = { onSelectPolicy(p.id) },
                    modifier = Modifier
                        .width(240.dp)
                        .height(145.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceElevatedDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (p.illustrationRes != 0) {
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
                                                Color(0x770A0F1D),
                                                Color(0xCC0A0F1D),
                                                Color(0xFA0A0F1D)
                                            )
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LevelBadge(p.level)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CivicGreen.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, CivicGreen.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "🔥 ${p.upPercentage}% Agree",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CivicGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🗳️ ${p.totalVotes.formatNumber()} citizens",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UtopiaTextMuted
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = CivicCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UtopiaBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = UtopiaSurfaceDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainTab.HOME) Icons.Filled.HowToVote else Icons.Outlined.HowToVote,
                    contentDescription = "Evaluate Policies"
                )
            },
            label = { Text("Evaluate", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_evaluate_tab"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = UtopiaRed.copy(alpha = 0.25f),
                selectedIconColor = UtopiaRed,
                selectedTextColor = UtopiaRed,
                unselectedIconColor = UtopiaTextMuted,
                unselectedTextColor = UtopiaTextMuted
            )
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.CAMPAIGNS,
            onClick = { onTabSelected(MainTab.CAMPAIGNS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainTab.CAMPAIGNS) Icons.Filled.Campaign else Icons.Outlined.Campaign,
                    contentDescription = "Organise Campaigns"
                )
            },
            label = { Text("Organise", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_organise_tab"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = UtopiaRed.copy(alpha = 0.25f),
                selectedIconColor = UtopiaRed,
                selectedTextColor = UtopiaRed,
                unselectedIconColor = UtopiaTextMuted,
                unselectedTextColor = UtopiaTextMuted
            )
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.LOCAL,
            onClick = { onTabSelected(MainTab.LOCAL) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainTab.LOCAL) Icons.Filled.LocationCity else Icons.Outlined.LocationCity,
                    contentDescription = "Local Community"
                )
            },
            label = { Text("Local", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_local_tab"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = UtopiaRed.copy(alpha = 0.25f),
                selectedIconColor = UtopiaRed,
                selectedTextColor = UtopiaRed,
                unselectedIconColor = UtopiaTextMuted,
                unselectedTextColor = UtopiaTextMuted
            )
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.LEARN,
            onClick = { onTabSelected(MainTab.LEARN) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainTab.LEARN) Icons.Filled.School else Icons.Outlined.School,
                    contentDescription = "Civic Academy"
                )
            },
            label = { Text("Learn", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_learn_tab"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = UtopiaRed.copy(alpha = 0.25f),
                selectedIconColor = UtopiaRed,
                selectedTextColor = UtopiaRed,
                unselectedIconColor = UtopiaTextMuted,
                unselectedTextColor = UtopiaTextMuted
            )
        )
    }
}

@Composable
fun VoteSplitBar(
    upPercentage: Int,
    downPercentage: Int,
    totalVotes: Int,
    modifier: Modifier = Modifier
) {
    val animatedUpProgress by animateFloatAsState(
        targetValue = upPercentage.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "voteUpAnim"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CivicGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$upPercentage% For",
                    style = MaterialTheme.typography.labelSmall,
                    color = CivicGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "${totalVotes.formatNumber()} citizens verified",
                style = MaterialTheme.typography.labelSmall,
                color = UtopiaTextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$downPercentage% Against",
                    style = MaterialTheme.typography.labelSmall,
                    color = UtopiaRedLight,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(UtopiaRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(UtopiaSurfaceElevatedDark)
        ) {
            Box(
                modifier = Modifier
                    .weight(maxOf(1f, animatedUpProgress))
                    .fillMaxHeight()
                    .background(CivicMintBrush)
            )
            Box(
                modifier = Modifier
                    .weight(maxOf(1f, 100f - animatedUpProgress))
                    .fillMaxHeight()
                    .background(UtopiaRed)
            )
        }
    }
}

@Composable
fun PolicyCard(
    policy: Policy,
    userVote: Boolean?,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reactionBurst by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reactionBurst) {
        if (reactionBurst != null) {
            kotlinx.coroutines.delay(1800)
            reactionBurst = null
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("policy_card_${policy.id}"),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Visual Image Illustration Banner at Top
            if (policy.illustrationRes != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                ) {
                    Image(
                        painter = painterResource(id = policy.illustrationRes),
                        contentDescription = policy.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient scrim to ensure clean contrast with badge elements
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x770A0F1D),
                                        Color(0x330A0F1D),
                                        UtopiaSurfaceDark
                                    )
                                )
                            )
                    )

                    // Floating Badges on top of illustration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LevelBadge(policy.level)
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(policy.status)
                        }

                        Surface(
                            shape = CircleShape,
                            color = UtopiaSurfaceDark.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, UtopiaBorderDark),
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = onToggleSave,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (policy.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save Policy",
                                    tint = if (policy.isSaved) UtopiaOrange else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Fallback top row if no illustration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LevelBadge(policy.level)
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(policy.status)
                    }

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (policy.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save Policy",
                            tint = if (policy.isSaved) UtopiaOrange else UtopiaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = policy.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = UtopiaOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${policy.issuingBody} • ${policy.jurisdictionName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtopiaOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = policy.plainSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UtopiaTextMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (policy.authorityResponse != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CivicGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CivicGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CivicGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Authority Response: ${policy.authorityResponse.authorityName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CivicGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Quick Reaction Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { reactionBurst = "🔥 Marked as High Priority Concern!" },
                        shape = RoundedCornerShape(12.dp),
                        color = UtopiaSurfaceElevatedDark,
                        border = BorderStroke(1.dp, UtopiaBorderDark)
                    ) {
                        Text(
                            text = "🔥 Urgent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = UtopiaOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = { reactionBurst = "💡 Endorsed as Pragmatic Solution!" },
                        shape = RoundedCornerShape(12.dp),
                        color = UtopiaSurfaceElevatedDark,
                        border = BorderStroke(1.dp, UtopiaBorderDark)
                    ) {
                        Text(
                            text = "💡 Smart",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CivicCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = { reactionBurst = "⚠️ Flagged: Suggesting Amendments!" },
                        shape = RoundedCornerShape(12.dp),
                        color = UtopiaSurfaceElevatedDark,
                        border = BorderStroke(1.dp, UtopiaBorderDark)
                    ) {
                        Text(
                            text = "⚠️ Needs Work",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = UtopiaGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                VoteSplitBar(
                    upPercentage = policy.upPercentage,
                    downPercentage = policy.downPercentage,
                    totalVotes = policy.totalVotes
                )

                // Animated Reaction Burst Toast
                AnimatedVisibility(visible = reactionBurst != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UtopiaOrange.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, UtopiaOrange.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = reactionBurst ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = UtopiaOrange,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-energy tactile voting action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            reactionBurst = "⚡ Vote Recorded! SHA-256 Receipt Generated"
                            onVoteUp()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("vote_up_button_${policy.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == true) CivicGreen else UtopiaSurfaceElevatedDark,
                            contentColor = if (userVote == true) Color.Black else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (userVote == true) CivicGreen else CivicGreen.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (userVote == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Vote Up",
                            tint = if (userVote == true) Color.Black else CivicGreen,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userVote == true) "Voted Agree" else "Agree (Up)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            reactionBurst = "⚡ Vote Recorded! Merkle Root Updated"
                            onVoteDown()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("vote_down_button_${policy.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == false) UtopiaRed else UtopiaSurfaceElevatedDark,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (userVote == false) UtopiaRed else UtopiaRed.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (userVote == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Vote Down",
                            tint = if (userVote == false) Color.White else UtopiaRedLight,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userVote == false) "Voted Oppose" else "Oppose (Down)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelBadge(level: PolicyLevel) {
    val (bgColor, textColor, label) = when (level) {
        PolicyLevel.LOCAL -> Triple(CivicAmber.copy(alpha = 0.2f), CivicAmber, "Local Ward")
        PolicyLevel.STATE -> Triple(CivicBlue.copy(alpha = 0.2f), CivicBlue, "State Law")
        PolicyLevel.CENTRAL -> Triple(UtopiaRed.copy(alpha = 0.2f), UtopiaRedLight, "Central Union")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusBadge(status: PolicyStatus) {
    val (bgColor, textColor) = when (status) {
        PolicyStatus.OPEN_FOR_EVALUATION -> Pair(CivicGreen.copy(alpha = 0.2f), CivicGreen)
        PolicyStatus.DELIVERED -> Pair(CivicBlue.copy(alpha = 0.2f), CivicBlue)
        PolicyStatus.RESPONDED -> Pair(CivicPurple.copy(alpha = 0.25f), CivicPurple)
        PolicyStatus.ENACTED -> Pair(CivicGreen.copy(alpha = 0.25f), CivicGreen)
        else -> Pair(UtopiaSurfaceElevatedDark, UtopiaTextMuted)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun CryptographicReceiptDialog(
    receipt: VoteReceipt,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("receipt_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CivicGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = CivicGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Signed Cryptographic Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Immutable proof published on Merkle Tree audit trail",
                    style = MaterialTheme.typography.bodySmall,
                    color = UtopiaTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReceiptDetailRow("Receipt Identifier", receipt.receiptId)
                ReceiptDetailRow("Decision Position", receipt.voteType)
                ReceiptDetailRow("Verification Time", receipt.timestamp)

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(UtopiaSurfaceElevatedDark)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SHA-256 DIGITAL FINGERPRINT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UtopiaOrange
                    )
                    Text(
                        text = receipt.cryptographicHash,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "MERKLE TREE ROOT ANCHOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CivicGreen
                    )
                    Text(
                        text = receipt.merkleRoot,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CivicGreen
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed)
                ) {
                    Text("Close Verification Receipt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = UtopiaTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun DonateSheetContent(
    campaign: Campaign,
    onAuthorizePayment: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountSlider by remember { mutableFloatStateOf(500f) }
    val amount = Math.round(amountSlider).toDouble()
    val feePercent = campaign.platformFeePercentage
    val feeAmount = Math.round(amount * (feePercent / 100.0) * 100.0) / 100.0
    val escrowDeposit = maxOf(0.0, amount - feeAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("donation_sheet"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fund Campaign Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Text(
                text = campaign.title,
                style = MaterialTheme.typography.bodySmall,
                color = UtopiaTextMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Contribution Dial / Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Slide Contribution:",
                    style = MaterialTheme.typography.labelMedium,
                    color = UtopiaTextMuted
                )
                Text(
                    text = "₹${"%.0f".format(amountSlider)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = UtopiaOrange
                )
            }

            Slider(
                value = amountSlider,
                onValueChange = { amountSlider = it },
                valueRange = 50f..5000f,
                steps = 98,
                colors = SliderDefaults.colors(
                    thumbColor = UtopiaRed,
                    activeTrackColor = UtopiaOrange,
                    inactiveTrackColor = UtopiaSurfaceElevatedDark
                )
            )

            // Preset Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100f, 500f, 1000f, 2500f).forEach { preset ->
                    OutlinedButton(
                        onClick = { amountSlider = preset },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (amountSlider == preset) UtopiaRed.copy(alpha = 0.2f) else UtopiaSurfaceElevatedDark
                        )
                    ) {
                        Text("₹${preset.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Transparent Escrow Visualizer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = UtopiaSurfaceElevatedDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TRANSPARENT ESCROW LEDGER (RBI Compliant)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UtopiaOrange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptDetailRow("Gross Citizen Debit", "₹${"%.2f".format(amount)}")
                    ReceiptDetailRow("Published Platform Fee (2.5%)", "₹${"%.2f".format(feeAmount)}")
                    HorizontalDivider(color = UtopiaBorderDark, modifier = Modifier.padding(vertical = 4.dp))
                    ReceiptDetailRow("Net Campaign Escrow Deposit", "₹${"%.2f".format(escrowDeposit)}")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onAuthorizePayment(amount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("authorize_payment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authorize UPI Payment of ₹${amount.toInt()}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReportModal(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val reportReasons = listOf(
        "Hate speech or incitement to violence",
        "Personal data or privacy leak (DPDP Act)",
        "Defamation or false claims",
        "Commercial spam or advertising",
        "Unlawful assembly or violent threats"
    )
    var selectedReason by remember { mutableStateOf(reportReasons[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("report_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Report Content to Moderation Queue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Reviewed anonymously under Information Technology Rules 2021.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UtopiaTextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(selectedReason) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed)
                    ) {
                        Text("Submit Report", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun Int.formatNumber(): String {
    return when {
        this >= 10000000 -> String.format(Locale.US, "%.1f Cr", this / 10000000f)
        this >= 100000 -> String.format(Locale.US, "%.1f L", this / 100000f)
        this >= 1000 -> String.format(Locale.US, "%.1f k", this / 1000f)
        else -> this.toString()
    }
}
