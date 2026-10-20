# Me/Profile check-in audit — v10.0.94

- The Check-in page now loads the signed-in account's status and streak from Firebase callable endpoints.
- Server transaction records one receipt per Kolkata calendar day. Concurrent/retried claims do not duplicate Activity XP.
- Activity XP is nonredeemable and does not affect Diamonds, Wallet, VIP or money. Anonymous and banned accounts cannot claim.
- Firestore rules allow owner reads only and deny client writes to check-in state and receipts.
- Validation includes service transaction tests and Firestore emulator privacy/write-denial tests.
