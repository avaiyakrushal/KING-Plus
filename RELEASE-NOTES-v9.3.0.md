# KING Plus v9.3.0 — Crowd Room + Multi-user Multiplayer

Main goal: many users can gather in one Party Room, talk/chat and play synchronized room games.

Implemented:
- Party Room member capacity is now modeled separately from stage/mic seats.
- New rooms advertise up to 100 joined members while retaining 8/10/12 stage/mic seats.
- Soft room-full admission check for new members (default 100, room metadata can specify 10–200).
- All joined members can open the same shared Jitsi room voice conference; mic seats remain stage/control indicators.
- Host Mute All still blocks new non-moderator voice entry at the KING Plus layer.
- Duplicate display names are disambiguated in member/admin/gift lists.
- Member strip shows live member count and overflow.
- Room state shows both crowd count and occupied mic seats.
- Group room games now support up to 50 ready players where game logic is group-capable. Pair/four-player games still use their own player counts.
- Production Test Center now reports crowd capacity, voice participants and large-room game readiness.
- v9.2.1 room-create/open compatibility fallback remains.

Important external dependency:
Firestore production Security Rules/IAM must allow member/message/event/game writes. A 403/permission-denied from Firebase cannot be bypassed safely inside the APK.
Public meet.jit.si is used for shared room audio; very large production voice rooms should ultimately use a dedicated managed/self-hosted real-time voice service for predictable scale and moderation.
