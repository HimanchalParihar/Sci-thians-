package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ThumbUp
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
import com.example.model.CommunityPost
import com.example.model.PollOption
import com.example.ui.theme.*
import com.example.ui.viewmodel.UtopiaViewModel

@Composable
fun LocalCommunityScreen(
    viewModel: UtopiaViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.communityPosts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var showNewPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = com.example.ui.theme.UtopiaBlackBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPostDialog = true },
                containerColor = UtopiaRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Local Post / Poll", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_post_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(com.example.ui.theme.UtopiaBlackBg)
                .padding(padding)
        ) {
            // Locality Header Banner
            Surface(
                color = com.example.ui.theme.UtopiaSurfaceDark,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = UtopiaRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userProfile.location.townWard,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Verified Resident Space • Pseudonymous Handles Only",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CivicGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIED RESIDENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CivicGreen
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("community_posts_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Neighborhood Hero Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_ill_neighborhood),
                                contentDescription = "Neighborhood Ward",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x440A0F1D),
                                                Color(0xBB0A0F1D),
                                                Color(0xF50A0F1D)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = CivicCyan.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, CivicCyan.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "📍 HYPER-LOCAL WARD SPACE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CivicCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Neighbors Solving Real Issues",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Raise local ward concerns, vote in polls, and escalate to binding campaigns.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UtopiaTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                items(posts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        onVotePoll = { optId -> viewModel.votePoll(post.id, optId) },
                        onEscalateToCampaign = { viewModel.escalateCommunityPost(post.id) }
                    )
                }
            }
        }
    }

    if (showNewPostDialog) {
        CreatePostDialog(
            wardName = userProfile.location.townWard,
            onDismiss = { showNewPostDialog = false },
            onCreate = { title, content, tags, isPoll, pollOptions ->
                viewModel.submitCommunityPost(title, content, tags, isPoll, pollOptions)
                showNewPostDialog = false
            }
        )
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onVotePoll: (String) -> Unit,
    onEscalateToCampaign: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UtopiaSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = CivicCardBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (post.illustrationRes != null && post.illustrationRes != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                ) {
                    Image(
                        painter = painterResource(id = post.illustrationRes),
                        contentDescription = post.title,
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
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
            // Author pseudonym & time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.authorPseudonym,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = post.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Poll Options if present
            if (post.isPoll && post.pollOptions != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val totalPollVotes = post.pollOptions.sumOf { it.votes }
                    post.pollOptions.forEach { opt ->
                        val isSelected = post.userVotedPollOptionId == opt.id
                        val percent = if (totalPollVotes == 0) 0 else ((opt.votes.toFloat() / totalPollVotes) * 100).toInt()

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) UtopiaRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVotePoll(opt.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onVotePoll(opt.id) },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Text(
                                    text = "$percent% (${opt.votes})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) UtopiaRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer controls: Escalate to Campaign + Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ThumbUp,
                        contentDescription = "Upvotes",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.upVotes} votes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentsCount} comments",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!post.isEscalated) {
                    OutlinedButton(
                        onClick = onEscalateToCampaign,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("escalate_post_${post.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = UtopiaRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Escalate to Campaign",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = UtopiaRed
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CivicAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Escalated to Campaign",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CivicAmber
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun CreatePostDialog(
    wardName: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, List<String>, Boolean, List<String>?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("Civic, Ward") }
    var isPoll by remember { mutableStateOf(false) }
    var pollOpt1 by remember { mutableStateOf("Option 1") }
    var pollOpt2 by remember { mutableStateOf("Option 2") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_post_dialog"),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = "Post in $wardName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Discuss local issues, clean-ups, or seek neighbor opinions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Issue Title") },
                        modifier = Modifier.fillMaxWidth().testTag("post_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Description & Details") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                item {
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add a Community Poll", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isPoll, onCheckedChange = { isPoll = it })
                    }
                }

                if (isPoll) {
                    item {
                        OutlinedTextField(
                            value = pollOpt1,
                            onValueChange = { pollOpt1 = it },
                            label = { Text("Poll Option 1") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = pollOpt2,
                            onValueChange = { pollOpt2 = it },
                            label = { Text("Poll Option 2") },
                            modifier = Modifier.fillMaxWidth()
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
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val pollOpts = if (isPoll) listOf(pollOpt1, pollOpt2) else null
                                    onCreate(title, content, tags, isPoll, pollOpts)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("confirm_create_post"),
                            colors = ButtonDefaults.buttonColors(containerColor = UtopiaRed)
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    }
}
