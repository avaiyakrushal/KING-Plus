# Bolo Hi Me/Profile → KING Plus route coverage (v10.0.80)

v10.0.80 continues the KING Plus Me/Profile routes from v10.0.79 and closes a reward-integrity gap in legacy screens. The old Daily Check-in and Missions pages displayed client-generated coin/XP claims even though the current Profile Center correctly said task rewards were unavailable.

| Area | v10.0.80 behavior |
| --- | --- |
| Daily Check-in | Explicitly unavailable until a server-side check-in service is deployed; no local balance or XP mutation |
| Missions | No local completion counter or claim button; routes back to the honest Tasks & Check-in page |
| Activity XP | Opening or using device-local profile/room actions no longer awards XP |
| Profile Center | Keeps the v10.0.79 nested Me/Profile destinations and clearly states that task completion is not tracked |

## Verification boundary

`tools/me-profile-parity-v10080/check_source.py` verifies that these screens cannot manufacture local coins, mission claims, or XP. This is a source-level check. It does not establish Firebase deployment, Android compilation, APK signature validity, or on-device behavior. Bolo Hi UI and live backend parity still need app/device review.
