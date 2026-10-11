# KING Plus → Bolo Hi functional parity checklist (v9.9.7 baseline)

**Baseline**: signed Android v9.9.7 APK from GitHub Actions run 38124730942.  
**Target**: original KING Plus implementations of the capabilities visible in Bolo Hi v5.30.5 (not copied artwork, music, recordings, proprietary implementation).
**Not a claim of 1:1 feature parity**. Source code presence, APK compilation, secure backend verification and real two-phone use are different acceptance states.

## Release acceptance rubric
- CODE: implementation exists.
- BUILD: compiles and signed update matches existing package/signature.
- BACKEND: persists securely, authenticated access enforced and correct rules actually deployed.
- DEVICE: verified on **two real phones with different accounts**, with screenshots/logs/video.
- PRODUCTION: performance, abuse tests, error handling, consent/permissions, payments, Play Store policy.

A feature is not complete until its necessary stages pass. Do not mark a placeholder/preview/demo as working.

## Part 1 — Biggest gaps: actual 3D and video
| Item | Baseline | Required acceptance |
|---|---|---|
| Native 3D World/avatar movement | MISSING | Load original 3D scene on supported Android phones; avatars move/sit; 10 simultaneous participants without critical crash; optimized assets |
| Original 3D gifts | PARTIAL (Canvas 3D-look) | Different recognisable gift models, full animation timeline, affordable memory, optional audio and per-participant event sync |
| Live video streaming | MISSING/UNVERIFIED | Camera/microphone consent, publisher/audience roles, actual stream to second device, reconnect and leave, privacy controls |
| Multi-video room | PARTIAL (class present) | Verified multi-user camera/mic, host permissions, joining/leaving and Android backgrounding |

## Part 2 — Party room
| Item | Baseline | Required acceptance |
|---|---|---|
| Room creation, invitation and join | CODE | Different phones join **same** roomId, member list/seat count sync, rejoin after poor network |
| Administrator / cohost | CODE | Host can assign/revoke role, permission checks on server; moderator cannot grant self ownership |
| DP → complete profile | CODE | Tap any occupied seat; actual displayName, avatar, level and private-safe profile; moderators long-press for management |
| Billboard, seat lock, kick/ban, mute | CODE | Only authorized moderator can change; visible on second device in real time; ban cannot be bypassed |
| Group/channel roles | PARTIAL | Real owner/admin/member separation in backend + removal, audit log and user feedback |
| Mic/voice & rejoin | PARTIAL | Both devices hear stable audio, toggling mic never creates duplicate call, running Games does not leak/rejoin channel |

## Part 3 — Games
| Item | Baseline | Required acceptance |
|---|---|---|
| Ludo | CODE | Turn authority, legal move validation, board sync, reconnect, win result for two real users |
| Tic-Tac-Toe, Dice | CODE | Both phones see legal move/result from same room, no local-only fake success |
| Werewolf, Spy, Draw & Guess | PARTIAL | Distinct real rules, synchronized timed rounds, scores, reconnect and result |
| Sheep Fight, Crazy Zoo, Bingo and similar | PARTIAL/Demo | Original complete interactive play, fair rules, shared scores and actual animations |
| Play without leaving Party voice | PARTIAL | In-room overlay for games, mic stays in correct room; no memory leak when closing game |

## Part 4 — Gifts, emoji, VIP, levels
| Item | Baseline | Required acceptance |
|---|---|---|
| Gift catalog | CODE (88) | Each distinct artwork visibly recognizes correct gift and has synchronized animation/sound on sender & receiver |
| Live action emoji | CODE (120) | Individual interactive animations, recipient sees them with supported RAM budgets |
| Original VIP Night visual | PARTIAL preview | Non-proprietary scene/model effects + sound, correct entitlement lock/unlock, per-room sync |
| VIP/Noble/Charisma progression | BLOCKED | Verified server source of truth, abuse-resistant spend/activity accounting, user-facing rule transparency |
| Diamond recharge | OFF | Official billing provider and approved app merchant, purchase verification, ledger, refunds/reversals, anti-double-credit |
| Audio for gifts | CODE | Each gift has distinguishable effect; background stops audio; accessibility mute option |

## Part 5 — Login, follow, messages, profile and video
| Item | Baseline | Required acceptance |
|---|---|---|
| Google login | CODE | New/returning account persists after restart |
| Facebook Login | MISSING | Facebook app and configuration, supported SDK, provider token verification & account linking |
| SMS OTP | CODE / UNVERIFIED | Real SMS arrives on authorized phone, verified credential logs in without app crash |
| WhatsApp OTP | NOT IMPLEMENTED | Approved business provider, template/consent, rate limiting, server verification |
| Search ID, Follow/Follow Back | CODE / UNVERIFIED | Different accounts find same registered user, bidirectional state confirms on both phones |
| Private Chat | CODE / UNVERIFIED | Message ordered and delivered on both phones, report/block protections |
| Profile/DP/Level | CODE / PARTIAL | Actual avatar, user privacy, server-level rank, photo permissions |
| Live / 1:1 Video | PARTIAL/UNVERIFIED | Actual device-to-device session with permissions/leave/mute and abuse prevention |

## Real-device acceptance in a single session
1. Fresh install **same version** on Phone A/B, each logs in with a different auth UID.
2. People → Test Connection shows **same Firebase project** and a distinct UID on each phone; profiles readable on server.
3. A searches B by KING ID, follows; B follows back; state refreshes on both.
4. Exchange messages, reopen app, confirm messages persisted.
5. A creates Party, sends full KINGROOM invite; B joins same persisted room document; both see other participant.
6. A appoints B admin; verify B may moderate but cannot become host. A revokes; verify B loses permission.
7. On A/B tap seat DP; compare user profile, level and disclosed metadata.
8. Play Dice and Ludo together; validate turns/results (not just menu buttons).
9. Send Kiss, Rocket and VIP preview gifts; check recipient animation, **different** sound and lock enforcement.
10. Test moving Party → Games → back and app background → foreground 10 times without crash.
11. Disable data mid-room, restore network, verify retry/rejoin and no duplicate Firestore writes.
12. Check Play Store account/license/billing, Facebook app setup, 3D asset pack, multi-camera permissions before declaring production complete.

## Critical dependencies
- Firebase management connection + deploy access to inspect project/rules, test the current project's real backend.
- Facebook Developer app config and public privacy/deletion URLs.
- Official mobile OTP/WhatsApp provider and quotas/consent where used.
- Play Console setup, verified Android release signing, in-app purchase or approved payment provider and backend purchase validation.
- Original 3D models, rigs, optimized assets/audio, 3D renderer and reliable live RTC/video backend.
- Two or more real Android devices for final validation.

## Guardrails
Do not copy Bolo Hi proprietary textures, sounds, names, logos, artwork, or exact code. Reproduce high-level functionality with original design.
Never allow client-set paid VIP/Noble entitlements or direct diamond minting.
Never broaden Firestore permissions to "allow read, write: if true" to make demo tests pass.
Do not publish a release with "all features completed" unless all necessary device/backend gates are evidenced.
