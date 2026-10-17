package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UtopiaRepository
import com.example.model.*
import com.example.provider.PaymentReceipt
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    CAMPAIGNS,
    LOCAL,
    LEARN
}

enum class ReadingMode {
    FORMAL,
    INTERACTIVE
}

enum class ActiveScreen {
    MAIN_FEED,
    POLICY_DETAIL,
    CAMPAIGN_DETAIL,
    LEARN_READER,
    AUTHORITY_PORTAL,
    PARTNER_INSIGHTS,
    PROFILE_SETTINGS,
    ONBOARDING
}

class UtopiaViewModel(
    val repository: UtopiaRepository = UtopiaRepository()
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val userVotes: StateFlow<Map<String, Boolean>> = repository.userVotes
    val voteReceipts: StateFlow<List<VoteReceipt>> = repository.voteReceipts
    val policies: StateFlow<List<Policy>> = repository.policies
    val suggestions: StateFlow<Map<String, List<Suggestion>>> = repository.suggestions
    val campaigns: StateFlow<List<Campaign>> = repository.campaigns
    val communityPosts: StateFlow<List<CommunityPost>> = repository.communityPosts
    val learnModules: StateFlow<List<LearnModule>> = repository.learnModules
    val auditLogs: StateFlow<List<String>> = repository.auditLogs
    val isOffline: StateFlow<Boolean> = repository.isOffline

    private val _activeTab = MutableStateFlow(MainTab.HOME)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.MAIN_FEED)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    private val _readingMode = MutableStateFlow(ReadingMode.INTERACTIVE)
    val readingMode: StateFlow<ReadingMode> = _readingMode.asStateFlow()

    private val _selectedLevel = MutableStateFlow<PolicyLevel?>(null)
    val selectedLevel: StateFlow<PolicyLevel?> = _selectedLevel.asStateFlow()

    private val _selectedTopic = MutableStateFlow<String?>("All")
    val selectedTopic: StateFlow<String?> = _selectedTopic.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showSavedOnly = MutableStateFlow(false)
    val showSavedOnly: StateFlow<Boolean> = _showSavedOnly.asStateFlow()

    private val _selectedPolicyId = MutableStateFlow<String?>("pol_local_01")
    val selectedPolicyId: StateFlow<String?> = _selectedPolicyId.asStateFlow()

    private val _selectedCampaignId = MutableStateFlow<String?>("cmp_01")
    val selectedCampaignId: StateFlow<String?> = _selectedCampaignId.asStateFlow()

    private val _selectedLearnModuleId = MutableStateFlow<String?>("mod_bill_to_law")
    val selectedLearnModuleId: StateFlow<String?> = _selectedLearnModuleId.asStateFlow()

    // Interactive Modals & Drawers
    private val _currentReceiptModal = MutableStateFlow<VoteReceipt?>(null)
    val currentReceiptModal: StateFlow<VoteReceipt?> = _currentReceiptModal.asStateFlow()

    private val _donateCampaignTarget = MutableStateFlow<Campaign?>(null)
    val donateCampaignTarget: StateFlow<Campaign?> = _donateCampaignTarget.asStateFlow()

    private val _reportTarget = MutableStateFlow<Pair<String, String>?>(null) // policyId to suggestionId
    val reportTarget: StateFlow<Pair<String, String>?> = _reportTarget.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val filteredPolicies: StateFlow<List<Policy>> = combine(
        policies,
        _selectedLevel,
        _selectedTopic,
        _searchQuery,
        _showSavedOnly
    ) { list, level, topic, query, savedOnly ->
        list.filter { p ->
            val matchLevel = level == null || p.level == level
            val matchTopic = topic == null || topic == "All" || p.tags.any { it.contains(topic, ignoreCase = true) }
            val matchQuery = query.isBlank() || p.title.contains(query, ignoreCase = true) || p.plainSummary.contains(query, ignoreCase = true)
            val matchSaved = !savedOnly || p.isSaved
            matchLevel && matchTopic && matchQuery && matchSaved
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPolicy: StateFlow<Policy?> = combine(policies, _selectedPolicyId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCampaign: StateFlow<Campaign?> = combine(campaigns, _selectedCampaignId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedLearnModule: StateFlow<LearnModule?> = combine(learnModules, _selectedLearnModuleId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
        _activeScreen.value = ActiveScreen.MAIN_FEED
    }

    fun openPolicyDetail(policyId: String) {
        _selectedPolicyId.value = policyId
        _activeScreen.value = ActiveScreen.POLICY_DETAIL
    }

    fun openCampaignDetail(campaignId: String) {
        _selectedCampaignId.value = campaignId
        _activeScreen.value = ActiveScreen.CAMPAIGN_DETAIL
    }

    fun openLearnReader(moduleId: String) {
        _selectedLearnModuleId.value = moduleId
        _activeScreen.value = ActiveScreen.LEARN_READER
    }

    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun navigateBack() {
        if (_activeScreen.value != ActiveScreen.MAIN_FEED) {
            _activeScreen.value = ActiveScreen.MAIN_FEED
        }
    }

    fun toggleReadingMode() {
        _readingMode.value = if (_readingMode.value == ReadingMode.FORMAL) ReadingMode.INTERACTIVE else ReadingMode.FORMAL
    }

    fun setReadingMode(mode: ReadingMode) {
        _readingMode.value = mode
    }

    fun setLevelFilter(level: PolicyLevel?) {
        _selectedLevel.value = level
    }

    fun setTopicFilter(topic: String?) {
        _selectedTopic.value = topic
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSavedFilter() {
        _showSavedOnly.value = !_showSavedOnly.value
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun closeReceiptModal() {
        _currentReceiptModal.value = null
    }

    fun openDonateSheet(campaign: Campaign) {
        _donateCampaignTarget.value = campaign
    }

    fun closeDonateSheet() {
        _donateCampaignTarget.value = null
    }

    fun openReportDialog(policyId: String, suggestionId: String) {
        _reportTarget.value = Pair(policyId, suggestionId)
    }

    fun closeReportDialog() {
        _reportTarget.value = null
    }

    // Actions
    fun castVote(policyId: String, isUpvote: Boolean) {
        val result = repository.castVote(policyId, isUpvote)
        result.onSuccess { receipt ->
            _currentReceiptModal.value = receipt
            _toastMessage.value = "Vote recorded with cryptographic receipt!"
        }.onFailure { err ->
            _toastMessage.value = err.message ?: "Unable to vote"
        }
    }

    fun toggleSavePolicy(policyId: String) {
        repository.toggleSavePolicy(policyId)
    }

    fun submitSuggestion(policyId: String, text: String, link: String?) {
        val res = repository.submitSuggestion(policyId, text, link)
        res.onSuccess {
            _toastMessage.value = "Formal suggestion submitted for public tally."
        }.onFailure {
            _toastMessage.value = it.message ?: "Failed to submit suggestion"
        }
    }

    fun voteSuggestion(policyId: String, suggestionId: String, isUp: Boolean) {
        repository.voteSuggestion(policyId, suggestionId, isUp)
    }

    fun submitReport(reason: String) {
        val target = _reportTarget.value ?: return
        repository.reportSuggestion(target.first, target.second, reason)
        closeReportDialog()
        _toastMessage.value = "Report logged in append-only moderation queue."
    }

    fun signCampaign(campaignId: String) {
        repository.signCampaign(campaignId)
        _toastMessage.value = "Petition signed with verified constituent token!"
    }

    fun rsvpCampaign(campaignId: String) {
        repository.rsvpCampaign(campaignId)
        _toastMessage.value = "Assembly RSVP updated. Safety guidance dispatched."
    }

    fun donateCampaign(campaignId: String, amount: Double) {
        val res = repository.donateToCampaign(campaignId, amount)
        res.onSuccess { receipt ->
            closeDonateSheet()
            _toastMessage.value = "Contribution of ₹${receipt.grossAmount} added to public escrow ledger!"
        }.onFailure {
            _toastMessage.value = it.message ?: "Payment failed"
        }
    }

    fun createCampaignFromPolicy(policy: Policy) {
        val res = repository.createCampaign(
            title = "Demand Action on: ${policy.title}",
            type = CampaignType.ONLINE,
            goal = "Hold authorities accountable as the response window expired without statutory resolution.",
            demands = listOf(
                "Immediate written commitment from ${policy.issuingBody}",
                "Implementation of constituent-ranked suggestions within 30 days"
            ),
            targetAuthority = policy.issuingBody,
            location = policy.jurisdictionName,
            targetSignatures = 1000,
            targetFunds = 25000.0,
            linkedPolicyId = policy.id
        )
        res.onSuccess {
            _selectedCampaignId.value = it.id
            _activeTab.value = MainTab.CAMPAIGNS
            _activeScreen.value = ActiveScreen.CAMPAIGN_DETAIL
            _toastMessage.value = "Civic Campaign launched! Verified residents alerted."
        }
    }

    fun createNewCampaign(
        title: String,
        type: CampaignType,
        goal: String,
        demands: List<String>,
        targetAuthority: String,
        location: String?,
        targetSignatures: Int,
        targetFunds: Double
    ) {
        val res = repository.createCampaign(
            title, type, goal, demands, targetAuthority, location, targetSignatures, targetFunds
        )
        res.onSuccess {
            _selectedCampaignId.value = it.id
            _activeTab.value = MainTab.CAMPAIGNS
            _activeScreen.value = ActiveScreen.CAMPAIGN_DETAIL
            _toastMessage.value = "New campaign published with transparent public ledger."
        }
    }

    fun submitCommunityPost(title: String, content: String, tags: List<String>, isPoll: Boolean, pollOptions: List<String>?) {
        repository.submitCommunityPost(title, content, tags, isPoll, pollOptions)
        _toastMessage.value = "Post shared in ${userProfile.value.location.townWard} board."
    }

    fun votePoll(postId: String, optionId: String) {
        repository.voteCommunityPoll(postId, optionId)
    }

    fun escalateCommunityPost(postId: String) {
        val campId = repository.escalatePostToCampaign(postId)
        if (campId.isNotBlank()) {
            _selectedCampaignId.value = campId
            _activeTab.value = MainTab.CAMPAIGNS
            _activeScreen.value = ActiveScreen.CAMPAIGN_DETAIL
            _toastMessage.value = "Local issue escalated into an active civic campaign!"
        }
    }

    fun answerQuiz(moduleId: String, selectedIndex: Int) {
        val correct = repository.completeQuiz(moduleId, selectedIndex)
        if (correct) {
            _toastMessage.value = "Correct answer! +25 Civic Karma awarded."
        } else {
            _toastMessage.value = "Incorrect. Review the constitutional steps and retry!"
        }
    }

    fun postAuthorityResponse(policyId: String, statement: String, decisionStatus: String) {
        repository.postAuthorityResponse(policyId, statement, decisionStatus)
        _toastMessage.value = "Official response published to public timeline."
    }

    fun updateLocation(state: String, district: String, townWard: String, street: String) {
        val res = repository.updateUserLocation(state, district, townWard, street)
        res.onSuccess {
            _toastMessage.value = "Location updated. 60-day anti-vote-shopping cooldown active."
        }.onFailure {
            _toastMessage.value = it.message ?: "Update failed"
        }
    }

    fun switchRole(role: UserRole) {
        repository.switchUserRole(role)
        _toastMessage.value = "Switched active view role to ${role.label}"
    }

    fun toggleOffline() {
        repository.toggleOfflineMode()
        val status = if (repository.isOffline.value) "Offline Mode (Actions queued safely)" else "Online Mode (Sync complete)"
        _toastMessage.value = status
    }
}
