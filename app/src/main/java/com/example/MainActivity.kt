package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.UtopiaTheme
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.UtopiaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UtopiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UtopiaTheme {
                UtopiaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UtopiaApp(viewModel: UtopiaViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeScreen by viewModel.activeScreen.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()

    val toastMessage by viewModel.toastMessage.collectAsState()
    val receiptModal by viewModel.currentReceiptModal.collectAsState()
    val donateSheetTarget by viewModel.donateCampaignTarget.collectAsState()
    val reportTarget by viewModel.reportTarget.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.UtopiaBlackBg,
        topBar = {
            if (activeScreen == ActiveScreen.MAIN_FEED) {
                UtopiaTopAppBar(
                    currentLocation = userProfile.location,
                    readingMode = readingMode,
                    isOffline = isOffline,
                    onToggleReadingMode = { viewModel.toggleReadingMode() },
                    onToggleOffline = { viewModel.toggleOffline() },
                    onOpenAuthorityPortal = { viewModel.navigateTo(ActiveScreen.AUTHORITY_PORTAL) },
                    onOpenInsights = { viewModel.navigateTo(ActiveScreen.PARTNER_INSIGHTS) },
                    onOpenProfile = { viewModel.navigateTo(ActiveScreen.PROFILE_SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (activeScreen == ActiveScreen.MAIN_FEED) {
                UtopiaBottomNavigation(
                    selectedTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                ActiveScreen.MAIN_FEED -> {
                    when (activeTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onOpenPolicy = { policyId -> viewModel.openPolicyDetail(policyId) }
                            )
                        }
                        MainTab.CAMPAIGNS -> {
                            CampaignsScreen(
                                viewModel = viewModel,
                                onOpenCampaign = { campaignId -> viewModel.openCampaignDetail(campaignId) }
                            )
                        }
                        MainTab.LOCAL -> {
                            LocalCommunityScreen(viewModel = viewModel)
                        }
                        MainTab.LEARN -> {
                            LearnScreen(
                                viewModel = viewModel,
                                onOpenModule = { moduleId -> viewModel.openLearnReader(moduleId) }
                            )
                        }
                    }
                }
                ActiveScreen.POLICY_DETAIL -> {
                    PolicyDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.CAMPAIGN_DETAIL -> {
                    CampaignDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.LEARN_READER -> {
                    LearnReaderScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.AUTHORITY_PORTAL -> {
                    AuthorityPortalScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.PARTNER_INSIGHTS -> {
                    PartnerInsightsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.PROFILE_SETTINGS -> {
                    ProfileSettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                ActiveScreen.ONBOARDING -> {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onComplete = { viewModel.navigateBack() }
                    )
                }
            }
        }

        // Modals & Dialogs
        receiptModal?.let { receipt ->
            CryptographicReceiptDialog(
                receipt = receipt,
                onDismiss = { viewModel.closeReceiptModal() }
            )
        }

        donateSheetTarget?.let { campaign ->
            DonateSheetContent(
                campaign = campaign,
                onAuthorizePayment = { amount -> viewModel.donateCampaign(campaign.id, amount) },
                onDismiss = { viewModel.closeDonateSheet() }
            )
        }

        reportTarget?.let {
            ReportModal(
                onDismiss = { viewModel.closeReportDialog() },
                onSubmit = { reason -> viewModel.submitReport(reason) }
            )
        }
    }
}
