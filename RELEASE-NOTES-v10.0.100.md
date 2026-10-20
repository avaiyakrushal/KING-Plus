# KING Plus v10.0.100 — Party Treasure Hunt (TEST)

- Treasure Hunt opens the live Party game room and uses shared room round state.
- Only currently Ready Party members can pick one of three chests once per round; everyone must submit before the result appears.
- Firestore rules constrain valid picks and prevent editing the selected chest while a round is active.
- This is a free casual room game. It grants no diamonds, coins, VIP, or paid rewards.
- Automated source, rule, Android build, APK signature and package checks run in CI. Physical-device and two-phone acceptance remain unverified.
