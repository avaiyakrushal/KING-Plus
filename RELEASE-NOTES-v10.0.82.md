# KING Plus v10.0.82 Chat Block Sync TEST

## Me/Profile → Safety Center → Blocked Users

- Chat's Block/Unblock action now uses the same Firebase account Block List that the Me Privacy screen reads.
- The chat loads the signed-in account's block list from the server, offers explicit Block/Unblock confirmation, and writes or deletes only the account-owned block record.
- New text, photo, and voice-note message writes are stopped when the account has blocked that chat partner. Firestore rules remain the authority and deny new messages if either account blocks the other.
- Local-only chats retain their local test block behavior.

## Verification boundary

The workflow runs existing Firestore privacy and block enforcement emulator tests, source checks, Android compilation, and TEST APK package/signature verification. A signed-in two-phone check is still needed to confirm the UI's live update timing. Block rules must also be deployed before production enforcement; this is a TEST APK.
