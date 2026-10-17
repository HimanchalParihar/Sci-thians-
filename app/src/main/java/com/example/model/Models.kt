package com.example.model

enum class PolicyLevel(val displayName: String) {
    LOCAL("Local"),
    STATE("State"),
    CENTRAL("Central")
}

enum class PolicyStatus(val displayName: String) {
    DRAFT("Draft"),
    OPEN_FOR_EVALUATION("Open for Evaluation"),
    CLOSED("Closed"),
    DELIVERED("Delivered"),
    RESPONDED("Responded"),
    ENACTED("Enacted"),
    WITHDRAWN("Withdrawn")
}

data class PolicyClause(
    val clauseNumber: String,
    val title: String,
    val officialText: String,
    val plainSummary: String
)

data class PolicyExplainer(
    val headline: String,
    val storyCards: List<String>,
    val keyTakeaways: List<String>,
    val lastReviewedDate: String,
    val editorReviewer: String,
    val sourceCitation: String
)

data class AuthorityResponse(
    val id: String,
    val authorityName: String,
    val department: String,
    val designation: String,
    val officialStatement: String,
    val decisionStatus: String, // e.g. "Accepted for Review", "Implementation Scheduled", "Rejected with Reason"
    val respondedAt: String,
    val officialGazetteRef: String? = null
)

data class Policy(
    val id: String,
    val title: String,
    val level: PolicyLevel,
    val issuingBody: String,
    val jurisdictionName: String,
    val plainSummary: String,
    val clauses: List<PolicyClause>,
    val explainer: PolicyExplainer,
    val status: PolicyStatus,
    val opensAt: String,
    val closesAt: String,
    val responseDueAt: String,
    val sourceUrl: String,
    val tags: List<String>,
    val upVotes: Int,
    val downVotes: Int,
    val authorityResponse: AuthorityResponse? = null,
    val revisionHistory: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val imageUrl: String? = null,
    val illustrationRes: Int = 0
) {
    val totalVotes: Int get() = upVotes + downVotes
    val upPercentage: Int get() = if (totalVotes == 0) 50 else ((upVotes.toFloat() / totalVotes) * 100).toInt()
    val downPercentage: Int get() = if (totalVotes == 0) 50 else 100 - upPercentage
}

data class VoteReceipt(
    val receiptId: String,
    val policyId: String,
    val policyTitle: String,
    val voteType: String, // "UP" or "DOWN"
    val timestamp: String,
    val cryptographicHash: String,
    val merkleRoot: String
)

enum class SuggestionStatus {
    ACTIVE,
    UNDER_REVIEW,
    DELIVERED_TO_AUTHORITY,
    FLAGGED
}

data class Suggestion(
    val id: String,
    val policyId: String,
    val authorPseudonym: String,
    val bodyText: String,
    val supportingLink: String? = null,
    val upVotes: Int,
    val downVotes: Int,
    val status: SuggestionStatus = SuggestionStatus.ACTIVE,
    val createdAt: String,
    val userVote: Int = 0 // 1 = upvoted, -1 = downvoted, 0 = none
) {
    val netVotes: Int get() = upVotes - downVotes
}

enum class CampaignType(val label: String) {
    ONLINE("Online Petition"),
    OFFLINE("Offline Event")
}

enum class CampaignStatus(val label: String) {
    ACTIVE("Active"),
    WON("Won"),
    PARTIAL("Partial Victory"),
    ONGOING("In Progress"),
    CLOSED("Closed")
}

data class CampaignUpdate(
    val id: String,
    val title: String,
    val date: String,
    val content: String
)

data class LedgerEntry(
    val id: String,
    val timestamp: String,
    val type: String, // "Donation Received", "Platform Fee (2.5%)", "Milestone Escrow Release", "Refund"
    val grossAmount: Double,
    val netAmount: Double,
    val feeDeduction: Double,
    val txHash: String,
    val note: String
)

data class Campaign(
    val id: String,
    val linkedPolicyId: String?,
    val title: String,
    val type: CampaignType,
    val goalDescription: String,
    val demands: List<String>,
    val targetAuthority: String,
    val organizerPseudonym: String,
    val locationOrVenue: String?,
    val targetSignatures: Int,
    val currentSignatures: Int,
    val targetFunds: Double,
    val raisedFunds: Double,
    val platformFeePercentage: Double = 2.5,
    val status: CampaignStatus = CampaignStatus.ACTIVE,
    val updates: List<CampaignUpdate> = emptyList(),
    val ledger: List<LedgerEntry> = emptyList(),
    val isSignedByMe: Boolean = false,
    val isRsvpdByMe: Boolean = false,
    val permitApproved: Boolean = true,
    val imageUrl: String? = null,
    val illustrationRes: Int = 0
)

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int
)

data class CommunityPost(
    val id: String,
    val authorPseudonym: String,
    val localityName: String,
    val title: String,
    val body: String,
    val tags: List<String>,
    val upVotes: Int,
    val commentsCount: Int,
    val isPoll: Boolean = false,
    val pollOptions: List<PollOption>? = null,
    val userVotedPollOptionId: String? = null,
    val isEscalated: Boolean = false,
    val escalatedCampaignId: String? = null,
    val createdAt: String,
    val illustrationRes: Int? = null
)

data class LearnStep(
    val stepNumber: Int,
    val title: String,
    val content: String,
    val legalArticle: String? = null
)

data class Quiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class LearnModule(
    val id: String,
    val title: String,
    val institutionType: String, // "Union Parliament", "Urban Local Body", "Judiciary", "State Assembly"
    val readMinutes: Int,
    val summary: String,
    val steps: List<LearnStep>,
    val quiz: Quiz,
    val isCompleted: Boolean = false,
    val linkedPolicyId: String? = null,
    val badgeName: String,
    val imageUrl: String? = null,
    val illustrationRes: Int = 0
)

data class UserLocation(
    val state: String = "Karnataka",
    val district: String = "Bengaluru Urban",
    val townWard: String = "Ward 47 (Indiranagar)",
    val street: String = "100 Feet Road"
)

data class UserProfile(
    val id: String = "user_89a3f2b1",
    val pseudonymousHandle: String = "CivicCitizen_4821",
    val phoneNumberMasked: String = "+91 98*** **321",
    val is18PlusVerified: Boolean = true,
    val kycVerificationDate: String = "12 Oct 2026",
    val location: UserLocation = UserLocation(),
    val locationChangeCooldownUntil: String? = null,
    val karmaScore: Int = 120,
    val role: UserRole = UserRole.CITIZEN
)

enum class UserRole(val label: String) {
    CITIZEN("Verified Citizen"),
    ORGANISER("Campaign Organiser"),
    MODERATOR("Content Moderator"),
    AUTHORITY("Department Official"),
    PARTNER("Institutional Partner"),
    ADMIN("Platform Admin")
}
