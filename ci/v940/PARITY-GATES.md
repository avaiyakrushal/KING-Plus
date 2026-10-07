# KING Plus v9.4.0 — Reference parity release gates

This branch is a development/parity branch. Do not treat it as final and do not publish/share a release APK until every gate below is verified on real devices.

## Gate A — Party room + voice
- [x] Mic ON/OFF stays inside Party room UI.
- [x] Audio-only RTC is embedded in PartyActivity; no Jitsi call screen launch from Mic.
- [x] Host/co-host, kick, ban, seat lock, mute-all and private-room controls exist.
- [ ] Two-device live voice test: listener hears seated speaker; mute/unmute syncs both UI and audio.
- [ ] Reconnect/background/Bluetooth/headset behavior verified.

## Gate B — Games
- [x] Realtime Firebase Online Ludo logic exists (2/4 players, ready/start, dice, moves, capture, winner/rematch).
- [x] Party-room multiplayer game flow exists through RoomGameActivity.
- [ ] Two-device Ludo full match verified from Party room.
- [ ] Each exposed room game verified end-to-end; remove/disable any non-playable tile.

## Gate C — Gifts / emoji / PK / KTV
- [x] Live emoji/reaction panel and room event sync exist.
- [x] Gift shop / gift wall / gift ranking flow exists.
- [x] Audio PK and KTV queue/stage flows exist.
- [ ] Two-device gift animation/event consistency verified.
- [ ] Live emoji animation consistency and replay protection verified.
- [ ] PK/KTV real-device room sync verified.

## Gate D — Home / Discover / Messages / Profile
- [x] v9.4 parity batch contains premium login, Discover/Messages, public profile and wallet visual work.
- [ ] Compare every primary screen against the supplied reference APK and close spacing/navigation/state gaps.
- [ ] Profile photo/name persistence across Google re-login verified.
- [ ] Empty/loading/error/offline states verified.

## Gate E — Final quality
- [ ] Cold start, login, room create/join/leave, mic, games, gifts, chat, profile smoke test on at least two accounts.
- [ ] No crash/ANR in the above flow.
- [ ] Firebase rules match host/co-host/member permissions.
- [ ] Final build is produced only after all unchecked gates are closed.

Reference note: match feature behavior and interaction patterns using KING Plus original code/assets; do not copy proprietary source/assets from the reference APK.
