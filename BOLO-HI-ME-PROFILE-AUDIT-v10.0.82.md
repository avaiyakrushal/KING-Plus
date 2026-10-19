# Bolo Hi Me/Profile → KING Plus route coverage (v10.0.82)

This update fixes a split between Me's cloud Block List and the Block/Unblock buttons shown inside Chat.

| Flow | v10.0.82 behavior |
| --- | --- |
| Me → Privacy → Blocked | Lists account-owned cloud blocks and lets the owner remove them |
| Chat → Block user | Confirms, then creates the signed-in account's Firestore block record |
| Chat → Unblock user | Confirms, then deletes the owner's Firestore block record |
| Send text/photo/voice note | Stops locally when the chat partner appears in the signed-in account's live block list; server rules deny new DMs when either side blocks |
| Local/test chat | Keeps its device-local test block behavior without claiming cloud enforcement |

The Firestore emulator regression test checks cross-device same-account reads, other-account denial, block ownership, and DM denial in both directions. This source-level and emulator work does not replace production rule deployment or signed-in device testing. Full Bolo Hi parity remains incomplete.
