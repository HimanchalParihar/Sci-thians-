package com.example

import com.example.data.UtopiaRepository
import com.example.model.CampaignType
import com.example.model.PolicyLevel
import com.example.model.PolicyStatus
import com.example.model.UserRole
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UtopiaCoreFlowsTest {

    private lateinit var repository: UtopiaRepository

    @Before
    fun setUp() {
        repository = UtopiaRepository()
    }

    @Test
    fun testOnePersonOneVoteIdempotency() {
        val policyId = "pol_state_01"
        val initialPolicy = repository.policies.value.find { it.id == policyId }!!
        val initialUp = initialPolicy.upVotes
        val initialDown = initialPolicy.downVotes

        // First vote: UP
        val firstVote = repository.castVote(policyId, true)
        assertTrue("Vote should succeed", firstVote.isSuccess)
        assertNotNull("Cryptographic receipt must be generated", firstVote.getOrNull())
        assertTrue("Receipt has SHA-256 hash", firstVote.getOrNull()!!.cryptographicHash.startsWith("0x"))

        val afterFirst = repository.policies.value.find { it.id == policyId }!!
        assertEquals(initialUp + 1, afterFirst.upVotes)

        // Repeat vote: UP (should not increment again)
        val repeatVote = repository.castVote(policyId, true)
        assertTrue(repeatVote.isSuccess)
        val afterRepeat = repository.policies.value.find { it.id == policyId }!!
        assertEquals(initialUp + 1, afterRepeat.upVotes)

        // Vote change: DOWN (replaces existing vote: up decrements, down increments)
        val changeVote = repository.castVote(policyId, false)
        assertTrue(changeVote.isSuccess)
        val afterChange = repository.policies.value.find { it.id == policyId }!!
        assertEquals(initialUp, afterChange.upVotes)
        assertEquals(initialDown + 1, afterChange.downVotes)
    }

    @Test
    fun testJurisdictionEligibilityCheck() {
        val centralPolicy = repository.policies.value.find { it.level == PolicyLevel.CENTRAL }!!
        assertTrue("Central policies are open to all constituents", repository.isEligibleToVote(centralPolicy))

        val localWardPolicy = repository.policies.value.find { it.id == "pol_local_01" }!!
        assertTrue("User in Ward 47 is eligible for Ward 47 policy", repository.isEligibleToVote(localWardPolicy))
    }

    @Test
    fun testAgeCheckInKYC() {
        val kyc = repository.kycProvider
        val underageResult = kyc.verifyAadhaarOfflineConsent("1234", 2012)
        assertTrue("Under-18 registration must be blocked under DPDP Act 2023", underageResult.isFailure)

        val adultResult = kyc.verifyAadhaarOfflineConsent("1234", 1999)
        assertTrue("Adult 18+ registration must succeed", adultResult.isSuccess)
        assertNotNull("Token hash generated", adultResult.getOrNull()!!.tokenHash)
    }

    @Test
    fun testSuggestionSubmissionAndRanking() {
        val policyId = "pol_local_01"
        val initialSuggestions = repository.suggestions.value[policyId] ?: emptyList()
        val initialCount = initialSuggestions.size

        val submission = repository.submitSuggestion(
            policyId = policyId,
            bodyText = "Construct automated stormwater sensor stations to monitor water depth.",
            supportingLink = "https://bengaluru.gov.in/sensors"
        )
        assertTrue("Suggestion must be accepted", submission.isSuccess)

        val afterSubmission = repository.suggestions.value[policyId]!!
        assertEquals(initialCount + 1, afterSubmission.size)

        val newSug = submission.getOrNull()!!
        assertEquals(1, newSug.netVotes)
    }

    @Test
    fun testCampaignCreationAndDonationWithFeeBreakdown() {
        val campaignRes = repository.createCampaign(
            title = "Repair Outer Ring Road Pedestrian Walkway",
            type = CampaignType.ONLINE,
            goal = "Pedestrians require safe footpaths free of encroachments.",
            demands = listOf("Clear footpath debris", "Install streetlights"),
            targetAuthority = "BBMP Road Infrastructure",
            location = "Indiranagar",
            targetSignatures = 1500,
            targetFunds = 20000.0
        )
        assertTrue(campaignRes.isSuccess)
        val campaign = campaignRes.getOrNull()!!

        // Donate ₹1,000 with transparent 2.5% platform fee
        val donationRes = repository.donateToCampaign(campaign.id, 1000.0)
        assertTrue("Donation must succeed", donationRes.isSuccess)

        val receipt = donationRes.getOrNull()!!
        assertEquals(1000.0, receipt.grossAmount, 0.01)
        assertEquals(25.0, receipt.platformFeeAmount, 0.01)
        assertEquals(975.0, receipt.escrowDepositAmount, 0.01)

        val updatedCamp = repository.campaigns.value.find { it.id == campaign.id }!!
        assertEquals(975.0, updatedCamp.raisedFunds, 0.01)
        assertEquals(1, updatedCamp.ledger.size)
    }

    @Test
    fun testAuthorityResponseUpdatesTimeline() {
        val policyId = "pol_local_01"
        val responseStatement = "BBMP East Zone has mobilized 4 earthmovers to begin Indiranagar canal desilting."
        val statusRes = repository.postAuthorityResponse(policyId, responseStatement, "Work Order Issued")
        assertTrue(statusRes.isSuccess)

        val updatedPolicy = repository.policies.value.find { it.id == policyId }!!
        assertEquals(PolicyStatus.RESPONDED, updatedPolicy.status)
        assertNotNull(updatedPolicy.authorityResponse)
        assertEquals(responseStatement, updatedPolicy.authorityResponse!!.officialStatement)
    }
}
