# Utopia: Regulatory & Legal Compliance Manual

## 1. Digital Personal Data Protection Act, 2023 (DPDP Act 2023)

Utopia is designed from first principles around the statutory requirements of the DPDP Act 2023:

### 1.1 Data Minimization & Consent Notices
- Consent is captured explicitly through bilingual (English & Hindi) clear modal dialogs prior to processing mobile OTP and identity verification.
- Purpose Limitation: Identity data is used solely for the singular purpose of de-duplicating voter accounts to guarantee "one person, one account".
- Storage Limitation: Raw identification records (Aadhaar number, VID, demographic documents) are never persisted in Utopia databases. Only the irreversible hash `HMAC-SHA256(UID, secret_salt)` is retained.

### 1.2 Protection of Minors (18+ Mandatory)
- In compliance with Section 9 of the DPDP Act regarding processing data of children, Utopia strictly prohibits accounts under 18 years of age.
- Birth year validation occurs during the zero-storage KYC check. Under-age attempts are terminated immediately with no record created.

### 1.3 Data Principal Rights (Access, Correction, Erasure)
- **Right to Erasure (Workflow):** Under Settings > Account > "Delete Account & Data", citizens can trigger an automated deletion sequence.
- All personal handles, device bindings, and notification tokens are permanently deleted from active storage.
- Past cast votes are decoupled from the user handle and irreversibly anonymized into historical aggregate tallies to protect public election records while honoring personal erasure.
- **Right to Grievance Redressal:** An in-app Grievance mechanism routes complaints directly to Utopia's Data Protection Officer (DPO) and Grievance Redressal Officer with statutory 30-day resolution tracking.

---

## 2. Information Technology (Intermediary Guidelines and Digital Media Ethics Code) Rules, 2021

### 2.1 Content Moderation & Due Diligence
- Rule 3(1)(b) Compliance: Community spaces and suggestions prohibit defamation, incitement to violence, obscenity, hate speech, and disruption of public order.
- Automated screening filters content against banned keywords and harassment patterns.
- Citizens can report abusive suggestions or campaigns with specific grievance categories (Hate Speech, Personal Data Leak, Unlawful Assembly, Spam).
- Moderation Queue: Reports are reviewed by human moderators who cannot see the real identity or contact details of the author.

### 2.2 Grievance Officer & Takedown Handling
- Contact details of the appointed Grievance Officer are accessible directly in the app's Help & Grievance section (`grievance@utopia.civic.in`).
- Emergency takedown orders from authorized government agencies or judicial courts are processed within the statutory 36-hour compliance window, with reasons logged in the public append-only audit trail.

---

## 3. Election Commission of India (ECI) Compliance & Silence Periods

### 3.1 Silence Period Enforcement (Representation of the People Act, 1951, Section 126)
- During the statutory 48-hour "silence period" prior to the conclusion of polling in a specific constituency or state, Utopia enters **Constituency Silence Mode**.
- In the affected jurisdiction:
  - New campaign rallies and offline events are automatically frozen.
  - Active policy voting relating to electoral campaigns is paused.
  - Informational civic education (Learn modules) remains active.
