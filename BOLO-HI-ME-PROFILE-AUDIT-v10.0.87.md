# Bolo Hi Me/Profile → KING Plus audit (v10.0.87)

| Me/Profile option | KING Plus route and behavior |
| --- | --- |
| My Videos | Opens account-scoped cloud uploads plus the existing local video archive. Uploads are limited to 10 MB and sync through the signed-in profile. |
| Cloud video actions | Plays the saved account video and removes its owner-scoped Firebase Storage object and Firestore record after confirmation. |
| Upload safety | Picker result must belong to the same signed-in UID; Firebase Storage checks owner and video MIME; Firestore checks UID, MIME, path and size. |
| Existing Me routes | Gift Wall/history, My Gallery, Check-in/Missions, Settings & Privacy, and Account & Sign-in retain their destinations and reward-safety behavior. |

## Still open
- Trusted server verification for reward progression and paid VIP benefits.
- Video playback/privacy acceptance using real Firebase accounts on two devices.
- More Profile options, deployment of updated Firestore rules, and signed-in cross-device UI QA.
