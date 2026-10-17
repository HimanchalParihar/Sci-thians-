package com.example.provider

import com.example.model.LedgerEntry
import com.example.model.VoteReceipt
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface KycProvider {
    fun requestOtp(mobileNumber: String): Result<String>
    fun verifyOtp(mobileNumber: String, enteredOtp: String): Result<Boolean>
    fun verifyAadhaarOfflineConsent(
        mockAadhaarNumberLast4: String,
        birthYear: Int
    ): Result<KycVerificationResult>
}

data class KycVerificationResult(
    val tokenHash: String,
    val is18Plus: Boolean,
    val pseudonymousHandle: String,
    val maskedIdentity: String
)

interface PaymentGatewayProvider {
    fun initiateCampaignContribution(
        campaignId: String,
        donorHandle: String,
        amount: Double,
        feePercent: Double
    ): Result<PaymentReceipt>
}

data class PaymentReceipt(
    val transactionId: String,
    val grossAmount: Double,
    val platformFeeAmount: Double,
    val escrowDepositAmount: Double,
    val timestamp: String,
    val publicTxHash: String,
    val ledgerEntry: LedgerEntry
)

object CryptoReceiptEngine {
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateVoteReceipt(policyId: String, policyTitle: String, isUpvote: Boolean, userId: String): VoteReceipt {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
        val receiptId = "RCP-" + UUID.randomUUID().toString().take(12).uppercase()
        val voteStr = if (isUpvote) "UP" else "DOWN"
        val rawInput = "$receiptId:$policyId:$voteStr:$userId:$now"
        val receiptHash = sha256(rawInput)
        val merkleRoot = sha256("MERKLE_ROOT_$policyId:${receiptHash.take(16)}")

        return VoteReceipt(
            receiptId = receiptId,
            policyId = policyId,
            policyTitle = policyTitle,
            voteType = voteStr,
            timestamp = now,
            cryptographicHash = "0x" + receiptHash.take(40),
            merkleRoot = "0x" + merkleRoot.take(40)
        )
    }
}

class SandboxKycProvider : KycProvider {
    private var otpRequestCount = 0
    private var lastOtpTime = 0L

    override fun requestOtp(mobileNumber: String): Result<String> {
        val now = System.currentTimeMillis()
        if (now - lastOtpTime < 3000) {
            return Result.failure(Exception("Rate limit reached: Please wait a moment before requesting another OTP."))
        }
        otpRequestCount++
        lastOtpTime = now
        // Sandbox OTP is 123456
        return Result.success("123456")
    }

    override fun verifyOtp(mobileNumber: String, enteredOtp: String): Result<Boolean> {
        return if (enteredOtp == "123456" || enteredOtp.length == 6) {
            Result.success(true)
        } else {
            Result.failure(Exception("Invalid OTP entered. (Sandbox valid OTP is 123456)"))
        }
    }

    override fun verifyAadhaarOfflineConsent(
        mockAadhaarNumberLast4: String,
        birthYear: Int
    ): Result<KycVerificationResult> {
        val currentYear = 2026
        val age = currentYear - birthYear
        if (age < 18) {
            return Result.failure(Exception("Registration blocked: DPDP Act 2023 requires citizens to be 18+ to register."))
        }

        // Salted hash without storing raw ID
        val platformSalt = "UTOPIA_CIVIC_GOV_SALT_2026"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$mockAadhaarNumberLast4:$birthYear:$platformSalt".toByteArray())
        val tokenHash = digest.joinToString("") { "%02x".format(it) }

        val pseudoId = tokenHash.take(4).uppercase()
        val pseudonymousHandle = "Citizen_$pseudoId"
        val maskedIdentity = "Aadhaar ending in •••• $mockAadhaarNumberLast4"

        return Result.success(
            KycVerificationResult(
                tokenHash = tokenHash,
                is18Plus = true,
                pseudonymousHandle = pseudonymousHandle,
                maskedIdentity = maskedIdentity
            )
        )
    }
}

class SandboxPaymentProvider : PaymentGatewayProvider {
    override fun initiateCampaignContribution(
        campaignId: String,
        donorHandle: String,
        amount: Double,
        feePercent: Double
    ): Result<PaymentReceipt> {
        if (amount < 10.0) {
            return Result.failure(Exception("Minimum contribution is ₹10.00"))
        }

        val feeAmount = Math.round(amount * (feePercent / 100.0) * 100.0) / 100.0
        val escrowAmount = Math.round((amount - feeAmount) * 100.0) / 100.0
        val txId = "UPI-" + UUID.randomUUID().toString().take(10).uppercase()
        val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())
        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(32)

        val entry = LedgerEntry(
            id = "LEDGER-" + UUID.randomUUID().toString().take(8),
            timestamp = now,
            type = "Donation Received (Escrow Deposit)",
            grossAmount = amount,
            netAmount = escrowAmount,
            feeDeduction = feeAmount,
            txHash = txHash,
            note = "Contribution from $donorHandle to Campaign $campaignId"
        )

        return Result.success(
            PaymentReceipt(
                transactionId = txId,
                grossAmount = amount,
                platformFeeAmount = feeAmount,
                escrowDepositAmount = escrowAmount,
                timestamp = now,
                publicTxHash = txHash,
                ledgerEntry = entry
            )
        )
    }
}
