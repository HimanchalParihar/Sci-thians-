# Utopia: Security Architecture & Cryptographic Integrity

## 1. Zero-Trust Identity & Anti-Sybil Architecture
- **HMAC De-duplication:** To ensure 1-person-1-account without retaining citizen PII:
  $$\text{VoterToken} = \text{HMAC-SHA256}(\text{NationalID}, \text{SecretSalt})$$
- Only the voter token is indexed uniquely in the identity database. Even in the event of a database compromise, no raw identity documents or identity numbers can be extracted.
- **Device Binding & Velocity Protection:**
  - Dynamic device fingerprinting and hardware attestation.
  - Rate limiting on OTP generation (maximum 3 requests per 10 minutes per mobile number/IP).
  - Velocity anomaly detection: rapid bursts of votes from identical IP subnets are quarantined for manual audit inspection.

## 2. Cryptographic Vote Receipts & Merkle Trees
- **Signed Vote Receipt:** Every cast or modified vote generates a receipt containing:
  - `receipt_id`: UUIDv4
  - `policy_id`: Identifier of the policy
  - `vote_value`: Up / Down (+1 / -1)
  - `timestamp_iso`: UTC timestamp
  - `signature`: Ed25519 / HMAC cryptographic signature verifiable by the citizen.
- **Merkle Tree Tally Publication:**
  - Upon conclusion of a policy evaluation window, all anonymized vote receipts for that policy are sorted chronologically and placed into a Merkle tree structure.
  - The calculated Merkle Root is recorded in an immutable append-only ledger and published to the public authority report.
  - Any citizen can verify their inclusion in the final tally using their private receipt without exposing their vote choice to third parties.

## 3. Role-Based Access Control (RBAC) & Data Segregation
| Role | Policy Voting | Suggestion Posting | Authority Response | Moderation Review | Insights Dashboard |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Citizen** | Yes (in jurisdiction) | Yes (in jurisdiction) | No | Report only | Public stats only |
| **Organiser** | Yes | Yes | No | Report only | Campaign Ledger |
| **Moderator** | Private citizen only | Private citizen only | No | Yes (Anonymized) | No |
| **Authority** | No (Cannot vote) | No | Yes (Official statement) | No | Ward/City Aggregates |
| **Partner** | No | No | No | No | k-Anonymity (k >= 10) |
| **Admin** | No | No | No | System Audit Logs | Platform metrics |

## 4. Differential Privacy & k-Anonymity in Insights
- Analytical queries require a strict minimum threshold of $k \ge 10$ distinct citizens. Any cell reporting fewer than 10 responses is suppressed or blended with Laplace noise to prevent re-identification.
