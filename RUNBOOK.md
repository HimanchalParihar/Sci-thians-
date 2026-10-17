# Utopia: Operations & Deployment Runbook

## 1. Quick Start & Staging Environment Setup
```bash
# Verify Gradle dependencies and compilation
gradle assembleDebug

# Run unit and architectural tests
gradle :app:testDebugUnitTest
```

## 2. Sandbox Provider Adapters Configuration
The application is pre-architected with pluggable provider adapters:
- **KYC Sandbox Adapter:** `SandboxKycProvider` enables test identity verification with mock OTPs (`123456`) and demographic hashing.
- **Payment Sandbox Adapter:** `SandboxPaymentProvider` enables UPI and Card payment simulation with realistic fee deduction, escrow deposit, and ledger reconciliation.
- **Notification Provider:** `SandboxNotificationProvider` queues push notifications and simulates delivery for closing policies and authority responses.

## 3. Policy Ingestion & Lifecycle Workflow
1. **Draft:** Policy ingested from official gazette or municipal order.
2. **Open for Evaluation:** Published with Formal and Interactive explainers; voting and suggestions open to constituents in jurisdiction.
3. **Closed:** Evaluation window concludes. Cryptographic vote receipts sealed into a Merkle Root. Final tally report generated.
4. **Delivered:** Automatically delivered to verified department / municipal portal with 14-day statutory response clock.
5. **Responded / Escalated:**
   - If authority responds with action plan -> Public timeline updated.
   - If authority fails to respond -> Campaign trigger prompt activated for residents.

## 4. Disaster Recovery & Audit Procedures
- **Append-only Audit Log:** Every administrative change, moderation takedown, and payment ledger entry is written to an immutable audit record.
- **Data Erasure Invocations:** Processed daily at 02:00 UTC through scheduled batch jobs.
