# Utopia: Architectural & Operational Assumptions

This document outlines all technical, legal, product, and operational assumptions made in designing and implementing Utopia — India's neutral civic-participation platform.

## 1. Identity & Verification Assumptions
- **No Raw ID Storage:** In compliance with the Aadhaar Act (Section 29) and DPDP Act 2023, Utopia never receives, stores, or caches raw Aadhaar numbers, PAN cards, or Voter ID numbers.
- **Provider Interface & Tokenization:** The identity verification subsystem uses an abstraction (`KycProvider`) with an Aadhaar Offline XML / Digilocker sandbox adapter. Upon citizen consent, the provider generates a SHA-256 salted hash of the unique identifier combined with Utopia's salt (`HMAC-SHA256(UniqueUID, PlatformSalt)`). This ensures strict "One Person, One Account, One Vote" deduplication while guaranteeing zero raw PII persistence.
- **Age Gate:** 18+ age verification is validated cryptographically during the KYC exchange by asserting `birth_year <= (current_year - 18)`. Citizens under 18 cannot complete onboarding.
- **Pseudonymity:** Citizens interact exclusively through generated or chosen pseudonymous handles (e.g., `CivicCitizen_4821`). At no point are real names or phone numbers exposed to other citizens, government officials, or analytical partners.

## 2. Jurisdictions & Location Hierarchy
- **Hierarchy:** 
  1. Central (All of India - Union Parliament & Ministries)
  2. State / Union Territory (28 States & 8 UTs)
  3. Local Body (Municipal Corporation, Municipality, Town Panchayat, or Gram Panchayat)
  4. Ward / Street (Neighborhood level)
- **Eligibility:** A citizen has full read access to all policies across India, but can cast votes and submit suggestions only on policies whose jurisdiction covers their verified residential location.
- **Vote Shopping Protection:** A citizen is allowed a maximum of 2 location updates per year, enforced with a 60-day cooldown period and automated audit logging.

## 3. Policy & Voting Integrity Assumptions
- **Non-Partisan Ranking:** Policy results and suggestions are sorted strictly by verifiable public votes and chronological tie-breakers. No algorithmic amplification, shadow-banning, or paid boosting exists.
- **Vote Receipts & Merkle Trees:** Every vote cast produces an idempotent signed cryptographic receipt containing `{vote_id, policy_id, timestamp, receipt_hash}`. The system periodically rolls these into Merkle tree roots published for transparent independent auditing.
- **Single Vote Idempotency:** Subsequent votes by the same citizen on the same policy update their vote position (up to down, or down to up) rather than creating duplicate tally rows.

## 4. Authority Engagement & Escalation
- **Response Windows:** Each delivered policy has a statutory response window (e.g., 14 to 30 days). Official statuses transition from `Delivered` to `Acknowledged`, `Responded`, or `No Response`.
- **Automatic Campaign Escalation:** If the responsible government department or local body fails to acknowledge or respond before the window expires, Utopia automatically prompts verified constituents in that jurisdiction to launch or join an organized citizen campaign.

## 5. Campaigns & Financial Integrity
- **Sandbox Payments & Escrow:** Campaign contributions (via UPI and Cards) utilize an RBI-compliant sandbox payment gateway adapter (`PaymentGatewayProvider`).
- **Transparent Platform Fee:** Utopia publishes an upfront platform fee (e.g., 2.5% strictly to cover banking infrastructure and audit costs). The exact fee is calculated and displayed transparently to the donor before authorization, and every transaction is recorded on an immutable public ledger.
- **Milestone Payouts & Refunds:** Funds are held in escrow and released only upon validated milestone evidence submitted by verified organizers. In the event of campaign abandonment or violation, unspent funds trigger an automated refund protocol.

## 6. Language & Vocabulary Standards
- **Formal Civic Lexicon:** Strict adherence to formal vocabulary across English, Hindi, and regional interfaces. The term "suggestion" is strictly utilized rather than informal terms like "advice" or "tips". Active primary verbs are standardized: `Vote`, `Suggest`, `Organise`, `Learn`.
