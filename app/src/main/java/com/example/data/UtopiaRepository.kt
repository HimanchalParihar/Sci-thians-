package com.example.data

import com.example.R
import com.example.model.*
import com.example.provider.CryptoReceiptEngine
import com.example.provider.PaymentReceipt
import com.example.provider.SandboxKycProvider
import com.example.provider.SandboxPaymentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UtopiaRepository {

    val kycProvider = SandboxKycProvider()
    val paymentProvider = SandboxPaymentProvider()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "usr_india_9918",
            pseudonymousHandle = "Citizen_K842",
            phoneNumberMasked = "+91 98*** **491",
            is18PlusVerified = true,
            kycVerificationDate = "10 Oct 2026",
            location = UserLocation(
                state = "Karnataka",
                district = "Bengaluru Urban",
                townWard = "Ward 47 (Indiranagar)",
                street = "12th Main Road"
            ),
            karmaScore = 145,
            role = UserRole.CITIZEN
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _userVotes = MutableStateFlow<Map<String, Boolean>>(mapOf(
        "pol_central_01" to true,
        "pol_local_01" to true
    ))
    val userVotes: StateFlow<Map<String, Boolean>> = _userVotes.asStateFlow()

    private val _voteReceipts = MutableStateFlow<List<VoteReceipt>>(listOf(
        VoteReceipt(
            receiptId = "RCP-774A9B12",
            policyId = "pol_central_01",
            policyTitle = "Digital Personal Data Protection Rules & Consent Framework",
            voteType = "UP",
            timestamp = "2026-10-15 09:30:12 UTC",
            cryptographicHash = "0x89ef412a9c40b8932df4a10492cb4108849201df",
            merkleRoot = "0xfa4891bca49018320492abcf71900149021893bb"
        )
    ))
    val voteReceipts: StateFlow<List<VoteReceipt>> = _voteReceipts.asStateFlow()

    private val _policies = MutableStateFlow<List<Policy>>(createInitialPolicies())
    val policies: StateFlow<List<Policy>> = _policies.asStateFlow()

    private val _suggestions = MutableStateFlow<Map<String, List<Suggestion>>>(createInitialSuggestions())
    val suggestions: StateFlow<Map<String, List<Suggestion>>> = _suggestions.asStateFlow()

    private val _campaigns = MutableStateFlow<List<Campaign>>(createInitialCampaigns())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(createInitialCommunityPosts())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _learnModules = MutableStateFlow<List<LearnModule>>(createInitialLearnModules())
    val learnModules: StateFlow<List<LearnModule>> = _learnModules.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<String>>(listOf(
        "[2026-10-14 08:00 UTC] System: Merkle Tree published for closed Central Gazette Pol#2026-09",
        "[2026-10-15 11:20 UTC] Authority BBMP: Acknowledged receipt of Ward 47 Citizen Evaluation",
        "[2026-10-16 14:05 UTC] Compliance: Automated DPDP consent verification log verified zero raw PII"
    ))
    val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun toggleOfflineMode() {
        _isOffline.value = !_isOffline.value
    }

    // Eligibility check based on verified constituent jurisdiction
    fun isEligibleToVote(policy: Policy): Boolean {
        val loc = _userProfile.value.location
        return when (policy.level) {
            PolicyLevel.CENTRAL -> true
            PolicyLevel.STATE -> policy.jurisdictionName.contains(loc.state, ignoreCase = true)
            PolicyLevel.LOCAL -> policy.jurisdictionName.contains(loc.townWard.take(7), ignoreCase = true) ||
                    policy.jurisdictionName.contains("Bengaluru", ignoreCase = true)
        }
    }

    fun castVote(policyId: String, isUpvote: Boolean): Result<VoteReceipt> {
        val policy = _policies.value.find { it.id == policyId }
            ?: return Result.failure(Exception("Policy not found"))

        if (!isEligibleToVote(policy)) {
            return Result.failure(Exception("Ineligible: You can only vote on policies in your verified jurisdiction (${_userProfile.value.location.townWard}, ${_userProfile.value.location.state})."))
        }

        if (policy.status == PolicyStatus.CLOSED || policy.status == PolicyStatus.ENACTED) {
            return Result.failure(Exception("Voting closed: This policy evaluation period has concluded."))
        }

        val currentVotes = _userVotes.value.toMutableMap()
        val previousVote = currentVotes[policyId]

        var newUp = policy.upVotes
        var newDown = policy.downVotes

        if (previousVote == null) {
            // First time voting
            if (isUpvote) newUp++ else newDown++
            currentVotes[policyId] = isUpvote
        } else if (previousVote == isUpvote) {
            // Same vote pressed again - remains as chosen
        } else {
            // Changed vote
            if (isUpvote) {
                newUp++
                newDown = maxOf(0, newDown - 1)
            } else {
                newDown++
                newUp = maxOf(0, newUp - 1)
            }
            currentVotes[policyId] = isUpvote
        }

        _userVotes.value = currentVotes

        _policies.value = _policies.value.map {
            if (it.id == policyId) it.copy(upVotes = newUp, downVotes = newDown) else it
        }

        val receipt = CryptoReceiptEngine.generateVoteReceipt(
            policyId = policy.id,
            policyTitle = policy.title,
            isUpvote = isUpvote,
            userId = _userProfile.value.pseudonymousHandle
        )

        _voteReceipts.value = listOf(receipt) + _voteReceipts.value
        return Result.success(receipt)
    }

    fun toggleSavePolicy(policyId: String) {
        _policies.value = _policies.value.map {
            if (it.id == policyId) it.copy(isSaved = !it.isSaved) else it
        }
    }

    fun submitSuggestion(policyId: String, bodyText: String, supportingLink: String?): Result<Suggestion> {
        if (bodyText.length < 10) {
            return Result.failure(Exception("Suggestion must be at least 10 characters."))
        }
        val newSuggestion = Suggestion(
            id = "sug_" + UUID.randomUUID().toString().take(8),
            policyId = policyId,
            authorPseudonym = _userProfile.value.pseudonymousHandle,
            bodyText = bodyText.trim(),
            supportingLink = supportingLink?.takeIf { it.isNotBlank() },
            upVotes = 1,
            downVotes = 0,
            status = SuggestionStatus.ACTIVE,
            createdAt = "Just now",
            userVote = 1
        )
        val currentMap = _suggestions.value.toMutableMap()
        val list = currentMap[policyId]?.toMutableList() ?: mutableListOf()
        list.add(0, newSuggestion)
        currentMap[policyId] = list
        _suggestions.value = currentMap
        return Result.success(newSuggestion)
    }

    fun voteSuggestion(policyId: String, suggestionId: String, isUp: Boolean) {
        val currentMap = _suggestions.value.toMutableMap()
        val list = currentMap[policyId]?.map { sug ->
            if (sug.id == suggestionId) {
                val voteVal = if (isUp) 1 else -1
                if (sug.userVote == voteVal) {
                    sug
                } else {
                    val deltaUp = if (isUp) 1 else if (sug.userVote == 1) -1 else 0
                    val deltaDown = if (!isUp) 1 else if (sug.userVote == -1) -1 else 0
                    sug.copy(
                        upVotes = maxOf(0, sug.upVotes + deltaUp),
                        downVotes = maxOf(0, sug.downVotes + deltaDown),
                        userVote = voteVal
                    )
                }
            } else sug
        } ?: emptyList()
        // Sort by net votes, then submission time
        currentMap[policyId] = list.sortedByDescending { it.netVotes }
        _suggestions.value = currentMap
    }

    fun reportSuggestion(policyId: String, suggestionId: String, reason: String): Result<Boolean> {
        val currentMap = _suggestions.value.toMutableMap()
        val list = currentMap[policyId]?.map { sug ->
            if (sug.id == suggestionId) sug.copy(status = SuggestionStatus.UNDER_REVIEW) else sug
        } ?: emptyList()
        currentMap[policyId] = list
        _suggestions.value = currentMap
        val log = "[${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}] Moderator queue: Suggestion $suggestionId flagged for '$reason'"
        _auditLogs.value = listOf(log) + _auditLogs.value
        return Result.success(true)
    }

    fun signCampaign(campaignId: String): Result<Boolean> {
        _campaigns.value = _campaigns.value.map { c ->
            if (c.id == campaignId) {
                if (c.isSignedByMe) c else c.copy(
                    currentSignatures = c.currentSignatures + 1,
                    isSignedByMe = true
                )
            } else c
        }
        return Result.success(true)
    }

    fun rsvpCampaign(campaignId: String): Result<Boolean> {
        _campaigns.value = _campaigns.value.map { c ->
            if (c.id == campaignId) {
                c.copy(isRsvpdByMe = !c.isRsvpdByMe)
            } else c
        }
        return Result.success(true)
    }

    fun donateToCampaign(campaignId: String, amount: Double): Result<PaymentReceipt> {
        val campaign = _campaigns.value.find { it.id == campaignId }
            ?: return Result.failure(Exception("Campaign not found"))

        val paymentRes = paymentProvider.initiateCampaignContribution(
            campaignId = campaignId,
            donorHandle = _userProfile.value.pseudonymousHandle,
            amount = amount,
            feePercent = campaign.platformFeePercentage
        )

        return paymentRes.map { receipt ->
            _campaigns.value = _campaigns.value.map { c ->
                if (c.id == campaignId) {
                    c.copy(
                        raisedFunds = c.raisedFunds + receipt.escrowDepositAmount,
                        ledger = listOf(receipt.ledgerEntry) + c.ledger
                    )
                } else c
            }
            receipt
        }
    }

    fun createCampaign(
        title: String,
        type: CampaignType,
        goal: String,
        demands: List<String>,
        targetAuthority: String,
        location: String?,
        targetSignatures: Int,
        targetFunds: Double,
        linkedPolicyId: String? = null
    ): Result<Campaign> {
        val newCamp = Campaign(
            id = "cmp_" + UUID.randomUUID().toString().take(8),
            linkedPolicyId = linkedPolicyId,
            title = title,
            type = type,
            goalDescription = goal,
            demands = demands,
            targetAuthority = targetAuthority,
            organizerPseudonym = _userProfile.value.pseudonymousHandle,
            locationOrVenue = location,
            targetSignatures = targetSignatures,
            currentSignatures = 1,
            targetFunds = targetFunds,
            raisedFunds = 0.0,
            status = CampaignStatus.ACTIVE,
            updates = listOf(
                CampaignUpdate(
                    id = "up_1",
                    title = "Campaign Launched",
                    date = "Today",
                    content = "Constituents initiated this civic campaign to demand accountability."
                )
            ),
            ledger = emptyList(),
            isSignedByMe = true
        )
        _campaigns.value = listOf(newCamp) + _campaigns.value
        return Result.success(newCamp)
    }

    fun submitCommunityPost(title: String, body: String, tags: List<String>, isPoll: Boolean, pollOptions: List<String>?): CommunityPost {
        val newPost = CommunityPost(
            id = "post_" + UUID.randomUUID().toString().take(8),
            authorPseudonym = _userProfile.value.pseudonymousHandle,
            localityName = _userProfile.value.location.townWard,
            title = title,
            body = body,
            tags = tags,
            upVotes = 1,
            commentsCount = 0,
            pollOptions = if (isPoll && pollOptions != null) {
                pollOptions.mapIndexed { idx, opt -> PollOption("opt_$idx", opt, 0) }
            } else null,
            createdAt = "Just now"
        )
        _communityPosts.value = listOf(newPost) + _communityPosts.value
        return newPost
    }

    fun voteCommunityPoll(postId: String, optionId: String) {
        _communityPosts.value = _communityPosts.value.map { p ->
            if (p.id == postId && p.userVotedPollOptionId == null) {
                val updatedOptions = p.pollOptions?.map { opt ->
                    if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                }
                p.copy(pollOptions = updatedOptions, userVotedPollOptionId = optionId)
            } else p
        }
    }

    fun escalatePostToCampaign(postId: String): String {
        val post = _communityPosts.value.find { it.id == postId } ?: return ""
        val campaignRes = createCampaign(
            title = "Action Demand: " + post.title,
            type = CampaignType.ONLINE,
            goal = post.body,
            demands = listOf("Immediate municipal inspection and remediation", "Official public report within 14 days"),
            targetAuthority = "Ward 47 Zonal Commissioner & Municipal Council",
            location = post.localityName,
            targetSignatures = 500,
            targetFunds = 15000.0
        )
        val campId = campaignRes.getOrNull()?.id ?: ""
        _communityPosts.value = _communityPosts.value.map {
            if (it.id == postId) it.copy(isEscalated = true, escalatedCampaignId = campId) else it
        }
        return campId
    }

    fun completeQuiz(moduleId: String, selectedOptionIndex: Int): Boolean {
        val module = _learnModules.value.find { it.id == moduleId } ?: return false
        val isCorrect = module.quiz.correctIndex == selectedOptionIndex
        if (isCorrect) {
            _learnModules.value = _learnModules.value.map {
                if (it.id == moduleId) it.copy(isCompleted = true) else it
            }
            _userProfile.value = _userProfile.value.copy(
                karmaScore = _userProfile.value.karmaScore + 25
            )
        }
        return isCorrect
    }

    fun postAuthorityResponse(policyId: String, statement: String, decisionStatus: String): Result<Boolean> {
        val response = AuthorityResponse(
            id = "resp_" + UUID.randomUUID().toString().take(6),
            authorityName = "Bruhat Bengaluru Mahanagara Palike (BBMP)",
            department = "Solid Waste & Urban Drainage Directorate",
            designation = "Superintending Engineer",
            officialStatement = statement,
            decisionStatus = decisionStatus,
            respondedAt = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
            officialGazetteRef = "BBMP/ENG/2026/SWD-411"
        )
        _policies.value = _policies.value.map {
            if (it.id == policyId) it.copy(
                status = PolicyStatus.RESPONDED,
                authorityResponse = response
            ) else it
        }
        return Result.success(true)
    }

    fun updateUserLocation(newState: String, newDistrict: String, newTownWard: String, newStreet: String): Result<Boolean> {
        val user = _userProfile.value
        if (user.locationChangeCooldownUntil != null) {
            return Result.failure(Exception("Location change restricted: Cooldown active until ${user.locationChangeCooldownUntil} to prevent vote-shopping."))
        }
        _userProfile.value = user.copy(
            location = UserLocation(newState, newDistrict, newTownWard, newStreet),
            locationChangeCooldownUntil = "16 Dec 2026"
        )
        return Result.success(true)
    }

    fun switchUserRole(newRole: UserRole) {
        _userProfile.value = _userProfile.value.copy(role = newRole)
    }

    private fun createInitialPolicies(): List<Policy> {
        return listOf(
            Policy(
                id = "pol_central_01",
                title = "Digital Personal Data Protection Rules & Consent Guidelines",
                level = PolicyLevel.CENTRAL,
                issuingBody = "Ministry of Electronics and Information Technology (MeitY)",
                jurisdictionName = "All India (Parliament of India)",
                plainSummary = "Establishes transparent consent frameworks, data minimization limits, and statutory protections for minors while mandating local grievance officers.",
                clauses = listOf(
                    PolicyClause(
                        clauseNumber = "Clause 4(1)",
                        title = "Notice & Consent Standards",
                        officialText = "Every Data Fiduciary shall give to the Data Principal an itemised notice in clear and plain language detailing the personal data to be processed and purpose.",
                        plainSummary = "Apps must explain plainly what personal data they need and why, without confusing legal jargon."
                    ),
                    PolicyClause(
                        clauseNumber = "Clause 9(2)",
                        title = "Processing of Children's Data",
                        officialText = "A Data Fiduciary shall not undertake tracking or behavioural monitoring of children or targeted advertising directed at children.",
                        plainSummary = "Targeted ads and tracking for users under 18 years of age are strictly prohibited."
                    ),
                    PolicyClause(
                        clauseNumber = "Clause 13(1)",
                        title = "Grievance Redressal Mechanism",
                        officialText = "A Data Principal shall have the right to readily available means of grievance redressal provided by a Data Fiduciary within thirty days.",
                        plainSummary = "Citizens must be provided a direct Grievance Officer with replies mandatory within 30 days."
                    )
                ),
                explainer = PolicyExplainer(
                    headline = "How the DPDP Rules protect citizen privacy online",
                    storyCards = listOf(
                        "No hidden tracking: Companies cannot monitor online habits without explicit, revocable consent.",
                        "Strict 18+ safeguards: Children's privacy is protected from behavioral profiling.",
                        "Direct accountability: In-app Grievance Officer reachable for any data erasure request."
                    ),
                    keyTakeaways = listOf(
                        "Bilingual consent notice requirement",
                        "Zero tracking of minors under 18",
                        "Statutory 30-day complaint resolution"
                    ),
                    lastReviewedDate = "14 Oct 2026",
                    editorReviewer = "Adv. Ramesh Varma (Constitutional Law)",
                    sourceCitation = "Gazette of India, Extra., Part II, Sec. 3(i), No. 418"
                ),
                status = PolicyStatus.OPEN_FOR_EVALUATION,
                opensAt = "01 Oct 2026",
                closesAt = "31 Oct 2026",
                responseDueAt = "15 Nov 2026",
                sourceUrl = "https://www.meity.gov.in/dpdp-rules-2026",
                tags = listOf("Privacy", "Digital Rights", "Central Law"),
                upVotes = 14280,
                downVotes = 1890,
                isSaved = true,
                illustrationRes = R.drawable.ic_ill_parliament
            ),
            Policy(
                id = "pol_state_01",
                title = "Clean Air & Electric Public Transit Incentive Scheme",
                level = PolicyLevel.STATE,
                issuingBody = "Department of Transport & Environment",
                jurisdictionName = "Karnataka State",
                plainSummary = "Provides state subsidies for electric bus fleets and mandates rooftop solar charging infrastructure across major bus depots.",
                clauses = listOf(
                    PolicyClause(
                        clauseNumber = "Section 3(A)",
                        title = "Fleet Electrification Targets",
                        officialText = "The State Transport Undertakings shall achieve 50% zero-emission bus fleet penetration across municipal agglomerations by December 2028.",
                        plainSummary = "Half of all state and city buses will be 100% electric within two years."
                    ),
                    PolicyClause(
                        clauseNumber = "Section 7(B)",
                        title = "Public Charging Grid Access",
                        officialText = "Commercial parking lots with over 50 bays must allocate at least 15% spaces for public Level-2 EV fast chargers.",
                        plainSummary = "Large malls and parking lots must install public electric car and scooter chargers."
                    )
                ),
                explainer = PolicyExplainer(
                    headline = "State-wide transition to electric buses and cleaner air",
                    storyCards = listOf(
                        "Cuts diesel emissions in dense metropolitan corridors by over 40%.",
                        "Guarantees affordable fares while modernizing public bus comfort.",
                        "Fast charging at public transport hubs and major parking zones."
                    ),
                    keyTakeaways = listOf(
                        "50% electric bus fleet by 2028",
                        "Mandatory chargers in commercial parking",
                        "Subsidies funded through green mobility cess"
                    ),
                    lastReviewedDate = "08 Oct 2026",
                    editorReviewer = "Priya Sundaram (Urban Mobility Fellow)",
                    sourceCitation = "Karnataka State Gazette Notification No. TR-2026/78"
                ),
                status = PolicyStatus.OPEN_FOR_EVALUATION,
                opensAt = "05 Oct 2026",
                closesAt = "25 Oct 2026",
                responseDueAt = "10 Nov 2026",
                sourceUrl = "https://transport.karnataka.gov.in/ev-scheme-2026",
                tags = listOf("Environment", "Public Transport", "Energy"),
                upVotes = 8940,
                downVotes = 620,
                illustrationRes = R.drawable.ic_ill_ev_bus
            ),
            Policy(
                id = "pol_local_01",
                title = "Ward 47 Stormwater Drainage Desilting & Public Waste Segregation",
                level = PolicyLevel.LOCAL,
                issuingBody = "Bruhat Bengaluru Mahanagara Palike (BBMP) - East Zone",
                jurisdictionName = "Ward 47 (Indiranagar), Bengaluru",
                plainSummary = "Allocates municipal funds to clear primary drain silt before monsoon and enforces 3-way doorstep waste segregation with spot penalties for non-compliance.",
                clauses = listOf(
                    PolicyClause(
                        clauseNumber = "Order 12(i)",
                        title = "Stormwater Drain Pre-Monsoon Desilting",
                        officialText = "Contractors shall desilt the Indiranagar valley primary channel to original concrete bed depth with geotagged photographic proof before May 15.",
                        plainSummary = "Drains must be cleaned to the bottom and verified with GPS photos to prevent monsoon road flooding."
                    ),
                    PolicyClause(
                        clauseNumber = "Order 15(iv)",
                        title = "3-Way Household Segregation",
                        officialText = "Citizens must segregate waste into wet, dry, and sanitary domestic hazardous fractions prior to morning collection.",
                        plainSummary = "Waste must be separated into wet, dry, and sanitary bins at doorstep."
                    )
                ),
                explainer = PolicyExplainer(
                    headline = "Fixing neighborhood waterlogging and street waste collection",
                    storyCards = listOf(
                        "Directly addresses recurrent flooding at 100 Feet Road and 12th Main junction.",
                        "Door-to-door verified daily collection with strict segregation rules.",
                        "Ward committee citizen supervision and contractor penalty clauses."
                    ),
                    keyTakeaways = listOf(
                        "Desilting Indiranagar primary canal",
                        "Mandatory GPS photos for completed drains",
                        "Community oversight via Ward Committee"
                    ),
                    lastReviewedDate = "11 Oct 2026",
                    editorReviewer = "Suresh N. (Civic Reporter)",
                    sourceCitation = "BBMP Circular ENG/SWD/2026/W47"
                ),
                status = PolicyStatus.OPEN_FOR_EVALUATION,
                opensAt = "10 Oct 2026",
                closesAt = "28 Oct 2026",
                responseDueAt = "05 Nov 2026",
                sourceUrl = "https://bbmp.gov.in/ward47-drainage",
                tags = listOf("Drainage", "Waste Management", "Civic Ward"),
                upVotes = 3410,
                downVotes = 180,
                isSaved = true,
                illustrationRes = R.drawable.ic_ill_drainage
            ),
            Policy(
                id = "pol_local_closed_01",
                title = "Indiranagar Metro Station Feeder Bus Frequency Enhancement",
                level = PolicyLevel.LOCAL,
                issuingBody = "Bangalore Metropolitan Transport Corporation (BMTC)",
                jurisdictionName = "Ward 47 (Indiranagar), Bengaluru",
                plainSummary = "Constituent evaluation on increasing morning and evening peak feeder buses between Indiranagar metro station and surrounding residential layouts.",
                clauses = listOf(
                    PolicyClause(
                        clauseNumber = "Schedule 1",
                        title = "Peak Hour 8-Minute Headway",
                        officialText = "Feeder routes MF-1 and MF-2 shall operate at intervals not exceeding 8 minutes during 08:00-11:00 and 17:30-20:30 on weekdays.",
                        plainSummary = "Feeder buses will arrive every 8 minutes during morning and evening rush hours."
                    )
                ),
                explainer = PolicyExplainer(
                    headline = "Evaluation delivered to BMTC Authority with 92% citizen approval",
                    storyCards = listOf(
                        "Constituents voted overwhelmingly in favor of 8-minute peak intervals.",
                        "Top citizen suggestion demanded digital pass integration and real-time GPS tracking.",
                        "Delivered to BMTC Chief Traffic Manager with official acknowledgement."
                    ),
                    keyTakeaways = listOf(
                        "Evaluation closed on 05 Oct 2026",
                        "2,480 verified constituent votes",
                        "Official authority response received"
                    ),
                    lastReviewedDate = "06 Oct 2026",
                    editorReviewer = "K. Ramanathan",
                    sourceCitation = "Utopia Constituent Report UR-2026-BLR-041"
                ),
                status = PolicyStatus.RESPONDED,
                opensAt = "15 Sep 2026",
                closesAt = "05 Oct 2026",
                responseDueAt = "20 Oct 2026",
                sourceUrl = "https://mybmtc.karnataka.gov.in/feeder-enhancement",
                tags = listOf("Metro Feeder", "Transit", "Delivered"),
                upVotes = 2280,
                downVotes = 200,
                illustrationRes = R.drawable.ic_ill_skywalk,
                authorityResponse = AuthorityResponse(
                    id = "resp_bmtc_901",
                    authorityName = "Bangalore Metropolitan Transport Corporation",
                    department = "Operations & Route Planning Division",
                    designation = "Chief Traffic Manager (Operations)",
                    officialStatement = "BMTC acknowledges constituent evaluation report UR-2026-BLR-041. We have scheduled 6 additional Midi-electric buses on routes MF-1 and MF-2 starting 01 November 2026, achieving a 7-8 minute headway. GPS live tracking will be exposed via the open transit API.",
                    decisionStatus = "Accepted & Scheduled for Deployment",
                    respondedAt = "12 Oct 2026",
                    officialGazetteRef = "BMTC/OPR/RO-47/2026/89"
                )
            )
        )
    }

    private fun createInitialSuggestions(): Map<String, List<Suggestion>> {
        return mapOf(
            "pol_local_01" to listOf(
                Suggestion(
                    id = "sug_01",
                    policyId = "pol_local_01",
                    authorPseudonym = "CivicCitizen_3310",
                    bodyText = "Include an open online dashboard with live photographic updates from the contractor for each cleared drain stretch so residents can inspect before monsoon.",
                    supportingLink = "https://bengaluru.citizenmatters.in/drain-monitoring",
                    upVotes = 284,
                    downVotes = 6,
                    status = SuggestionStatus.ACTIVE,
                    createdAt = "2 days ago",
                    userVote = 1
                ),
                Suggestion(
                    id = "sug_02",
                    policyId = "pol_local_01",
                    authorPseudonym = "Resident_Indira9",
                    bodyText = "Ensure wet waste collection happens before 8:30 AM rather than mid-day when working residents have already left.",
                    supportingLink = null,
                    upVotes = 142,
                    downVotes = 4,
                    status = SuggestionStatus.ACTIVE,
                    createdAt = "3 days ago"
                )
            ),
            "pol_central_01" to listOf(
                Suggestion(
                    id = "sug_c01",
                    policyId = "pol_central_01",
                    authorPseudonym = "TechRights_81",
                    bodyText = "Mandate a machine-readable data export format (JSON/CSV) under the Right to Data Portability so citizens can switch providers effortlessly.",
                    supportingLink = "https://internetfreedom.in/data-portability",
                    upVotes = 512,
                    downVotes = 11,
                    status = SuggestionStatus.ACTIVE,
                    createdAt = "1 day ago",
                    userVote = 1
                )
            )
        )
    }

    private fun createInitialCampaigns(): List<Campaign> {
        return listOf(
            Campaign(
                id = "cmp_01",
                linkedPolicyId = null,
                title = "Install Pedestrian Foot Overbridge with Escalator at Domlur-100ft Junction",
                type = CampaignType.OFFLINE,
                goalDescription = "Over 12,000 pedestrians cross this high-speed 6-lane junction daily without a safe signalized crossing. Over 8 severe pedestrian accidents occurred in the past 12 months.",
                demands = listOf(
                    "BBMP and Traffic Police must sanction a skywalk with elevators for seniors and wheelchair users.",
                    "Install temporary pedestrian-controlled Pelican light until construction begins."
                ),
                targetAuthority = "BBMP Road Infrastructure Directorate & Traffic Police Commissioner",
                organizerPseudonym = "Citizen_Activist_54",
                locationOrVenue = "Indiranagar 100ft & Old Airport Road Junction (Assembly at BDA Complex)",
                targetSignatures = 5000,
                currentSignatures = 3840,
                targetFunds = 45000.0,
                raisedFunds = 38200.0,
                platformFeePercentage = 2.5,
                status = CampaignStatus.ACTIVE,
                updates = listOf(
                    CampaignUpdate(
                        id = "up_c1",
                        title = "Permission Granted for Peaceful Assembly",
                        date = "14 Oct 2026",
                        content = "Local police station has granted assembly clearance. Legal observers and marshals will guide attendees."
                    )
                ),
                ledger = listOf(
                    LedgerEntry(
                        id = "LED-8910",
                        timestamp = "15 Oct 2026, 14:20",
                        type = "Milestone Escrow Release",
                        grossAmount = 15000.0,
                        netAmount = 15000.0,
                        feeDeduction = 0.0,
                        txHash = "0x8fa901bc09148bca1",
                        note = "Released for architectural safety drawings & sound permit clearance."
                    )
                ),
                isSignedByMe = true,
                isRsvpdByMe = true,
                illustrationRes = R.drawable.ic_ill_skywalk
            ),
            Campaign(
                id = "cmp_02",
                linkedPolicyId = "pol_local_01",
                title = "Citizens Demand Open Audit of Stormwater Desilting Contracts",
                type = CampaignType.ONLINE,
                goalDescription = "Demand municipal ward engineers release the physical measurement sheets and contractor names for Indiranagar canals before contract disbursements.",
                demands = listOf(
                    "Publish contract scope and GPS photos on BBMP public portal.",
                    "Ward Committee joint inspection with constituent representatives."
                ),
                targetAuthority = "BBMP Chief Commissioner",
                organizerPseudonym = "Citizen_K842",
                locationOrVenue = null,
                targetSignatures = 2000,
                currentSignatures = 1420,
                targetFunds = 12000.0,
                raisedFunds = 8900.0,
                platformFeePercentage = 2.5,
                status = CampaignStatus.ACTIVE,
                isSignedByMe = true,
                illustrationRes = R.drawable.ic_ill_drainage
            ),
            Campaign(
                id = "cmp_03",
                linkedPolicyId = null,
                title = "Save Ulsoor Lake: Block Raw Sewage Inflow & Restore Bio-Diversity",
                type = CampaignType.OFFLINE,
                goalDescription = "Over 15 MLD of untreated domestic effluent enters the lake daily. Join our weekend citizen water-testing assembly and lake cleanup drive.",
                demands = listOf(
                    "BWSSB must install decentralized aerators and stop direct stormwater inlets.",
                    "Citizen-led bio-monitoring committee with weekly public water quality tests."
                ),
                targetAuthority = "Karnataka State Pollution Control Board & BWSSB",
                organizerPseudonym = "EcoCitizen_Ulsoor",
                locationOrVenue = "Ulsoor Lake Promenade, Gate 2 (Near Boating Club)",
                targetSignatures = 3500,
                currentSignatures = 2890,
                targetFunds = 25000.0,
                raisedFunds = 21400.0,
                platformFeePercentage = 2.5,
                status = CampaignStatus.ACTIVE,
                isSignedByMe = false,
                isRsvpdByMe = false,
                illustrationRes = R.drawable.ic_ill_lake_revival
            ),
            Campaign(
                id = "cmp_04",
                linkedPolicyId = null,
                title = "Pedestrians First: Reclaim Footpaths & Install Safe Street Bollards",
                type = CampaignType.ONLINE,
                goalDescription = "Pedestrians along 100ft road are forced into traffic due to illegal vehicle parking on walkways. Demand immediate bollard installations.",
                demands = listOf(
                    "Install physical steel bollards every 1.2m along Indiranagar main corridors.",
                    "Zero-tolerance automated camera enforcement against sidewalk parking."
                ),
                targetAuthority = "Bengaluru City Traffic Police & BBMP Ward 47",
                organizerPseudonym = "WalkableBengaluru_Org",
                locationOrVenue = null,
                targetSignatures = 1500,
                currentSignatures = 1140,
                targetFunds = 10000.0,
                raisedFunds = 7800.0,
                platformFeePercentage = 2.5,
                status = CampaignStatus.ACTIVE,
                isSignedByMe = false,
                illustrationRes = R.drawable.ic_ill_footpath
            )
        )
    }

    private fun createInitialCommunityPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "cp_01",
                authorPseudonym = "Resident_12thMain",
                localityName = "Ward 47 (Indiranagar)",
                title = "Broken streetlights along 12th Main between 4th and 8th Cross",
                body = "Four streetlights have been completely non-functional for the past 10 days, posing severe safety risks for pedestrians and cyclists returning after 8 PM.",
                tags = listOf("Safety", "Streetlights", "Urgent"),
                upVotes = 42,
                commentsCount = 8,
                isPoll = true,
                pollOptions = listOf(
                    PollOption("po_1", "Escalate directly as a constituent campaign", 31),
                    PollOption("po_2", "Raise at tomorrow's Ward Committee meet", 11)
                ),
                userVotedPollOptionId = "po_1",
                createdAt = "5 hours ago",
                illustrationRes = R.drawable.ic_ill_footpath
            ),
            CommunityPost(
                id = "cp_02",
                authorPseudonym = "CivicCitizen_910",
                localityName = "Ward 47 (Indiranagar)",
                title = "Community clean-up & composting drive this Saturday at Defence Colony Park",
                body = "Join us this Saturday from 7:00 AM to 9:30 AM for a neighborhood dry waste segregation and garden composting workshop. Bring gloves!",
                tags = listOf("Environment", "CleanUp", "Volunteer"),
                upVotes = 68,
                commentsCount = 14,
                createdAt = "Yesterday",
                illustrationRes = R.drawable.ic_ill_neighborhood
            )
        )
    }

    private fun createInitialLearnModules(): List<LearnModule> {
        return listOf(
            LearnModule(
                id = "mod_bill_to_law",
                title = "How a Bill Becomes Law in Parliament",
                institutionType = "Union Parliament (Lok Sabha & Rajya Sabha)",
                readMinutes = 4,
                summary = "Understand the journey of Indian legislation from initial draft and standing committee scrutiny to Presidential assent.",
                steps = listOf(
                    LearnStep(
                        stepNumber = 1,
                        title = "Drafting & Introduction (First Reading)",
                        content = "The responsible Ministry drafts the bill. It is introduced in either Lok Sabha or Rajya Sabha with copies circulated to all MPs.",
                        legalArticle = "Article 107 of Constitution of India"
                    ),
                    LearnStep(
                        stepNumber = 2,
                        title = "Departmental Standing Committee Review",
                        content = "Bills are referred to cross-party committees for deep clause-by-clause analysis, expert testimony, and public suggestions.",
                        legalArticle = "Rules of Procedure and Conduct of Business"
                    ),
                    LearnStep(
                        stepNumber = 3,
                        title = "Second & Third Reading (Debate & Voting)",
                        content = "Clauses and proposed amendments are voted upon individually. If passed by a majority in both Houses, it moves to the President.",
                        legalArticle = "Article 108 (Joint Sitting provisions)"
                    ),
                    LearnStep(
                        stepNumber = 4,
                        title = "Presidential Assent & Gazette Publication",
                        content = "The President signs the bill (or returns for reconsideration once). Upon signature, it is published in The Gazette of India and becomes enforceable Act.",
                        legalArticle = "Article 111 of Constitution of India"
                    )
                ),
                quiz = Quiz(
                    question = "Which body conducts clause-by-clause scrutiny and takes expert evidence on bills before debate in the full House?",
                    options = listOf(
                        "The Election Commission",
                        "Departmentally Related Parliamentary Standing Committee",
                        "The Supreme Court Registry",
                        "The Cabinet Secretariat"
                    ),
                    correctIndex = 1,
                    explanation = "Parliamentary Standing Committees examine the fine print of legislation and hear testimonies from experts, civil society, and stakeholders."
                ),
                isCompleted = true,
                linkedPolicyId = "pol_central_01",
                badgeName = "Legislative Scholar",
                illustrationRes = R.drawable.ic_ill_parliament
            ),
            LearnModule(
                id = "mod_panchayats_municipalities",
                title = "73rd & 74th Amendments: Local Self-Government",
                institutionType = "Panchayats & Municipal Corporations",
                readMinutes = 3,
                summary = "Discover how constitutional status was given to Gram Panchayats and Urban Local Bodies, empowering citizens at the grassroots.",
                steps = listOf(
                    LearnStep(
                        stepNumber = 1,
                        title = "Constitutional Mandate for Decentralisation",
                        content = "Passed in 1992, these landmark amendments mandated democratic elections every 5 years for local bodies across rural and urban India.",
                        legalArticle = "Parts IX and IX-A of the Constitution"
                    ),
                    LearnStep(
                        stepNumber = 2,
                        title = "Ward Committees & Citizen Participation",
                        content = "Urban municipal wards must institute Ward Committees where verified neighborhood residents participate in budgeting and civic works.",
                        legalArticle = "Article 243S of the Constitution"
                    ),
                    LearnStep(
                        stepNumber = 3,
                        title = "Devolution of 18 Municipal Functions",
                        content = "The 12th Schedule lists urban planning, water supply, public health, sanitation, roads, and bridges as direct urban body powers.",
                        legalArticle = "Twelfth Schedule of the Constitution"
                    )
                ),
                quiz = Quiz(
                    question = "Under Article 243S, which forum is constitutionally mandated for citizen participation at the neighborhood level in cities?",
                    options = listOf(
                        "Ward Committee",
                        "State Legislative Council",
                        "District Planning Court",
                        "Zonal Lokayukta"
                    ),
                    correctIndex = 0,
                    explanation = "Ward Committees are the primary constitutional vehicle for direct citizen oversight over municipal works and local budgeting."
                ),
                isCompleted = false,
                linkedPolicyId = "pol_local_01",
                badgeName = "Grassroots Guardian",
                illustrationRes = R.drawable.ic_ill_civic_academy
            ),
            LearnModule(
                id = "mod_clean_energy",
                title = "Clean Air, Solar Grids & Public Transport Rights",
                institutionType = "State Regulatory & Environmental Authorities",
                readMinutes = 3,
                summary = "Explore legal rights to clean air and how municipal energy transitions are financed through green bonds and citizen tariffs.",
                steps = listOf(
                    LearnStep(
                        stepNumber = 1,
                        title = "Right to Clean Environment",
                        content = "The Supreme Court recognized clean air and water as fundamental rights under Article 21 of the Indian Constitution.",
                        legalArticle = "Article 21 (Right to Life & Dignity)"
                    ),
                    LearnStep(
                        stepNumber = 2,
                        title = "State Energy Regulatory Commissions (SERC)",
                        content = "Citizens can submit objections to annual electricity and public transport tariff revisions during open public hearings.",
                        legalArticle = "Electricity Act, 2003"
                    )
                ),
                quiz = Quiz(
                    question = "Which constitutional article has been interpreted by the Supreme Court to encompass the citizen's right to a clean, unpolluted environment?",
                    options = listOf(
                        "Article 21",
                        "Article 14",
                        "Article 32",
                        "Article 51A"
                    ),
                    correctIndex = 0,
                    explanation = "The Supreme Court has consistently held in landmark rulings that the Right to Life under Article 21 includes the right to wholesome air, water, and environment."
                ),
                isCompleted = false,
                linkedPolicyId = "pol_state_01",
                badgeName = "Eco-Rights Advocate",
                illustrationRes = R.drawable.ic_ill_clean_energy
            )
        )
    }
}
