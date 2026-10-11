#!/usr/bin/env python3
"""Read-only, honest five-section parity audit. CODE FOUND != LIVE VERIFIED.
Works against extracted signed source archive (v9.9.7 or later).
"""
from pathlib import Path
import os,re,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
files={f.name:f.read_text(encoding='utf-8',errors='replace') for f in pkg.glob('*.java')}
def code(*terms):return all(any(t in v for v in files.values()) for t in terms)
def file_has(name,*terms):return name in files and all(t in files[name] for t in terms)
def row(section,description,found,verify,blocker=""):
    state="CODE DETECTED; TEST REQUIRED" if found else "NOT YET VERIFIED / MISSING"
    return [section,description,state,verify,blocker]
entries=[
row("1. 3D/Video","True native 3D renderer and avatar simulation",
    code("GLSurfaceView") or code("UnityPlayer") or code("Sceneform"),
    "Two signed-in avatars walk/sit in synchronized 3D on two Android devices",
    "Original optimized 3D assets, renderer, live state sync"),
row("1. 3D/Video","Multi-participant video room",
    "KingMultiVideoActivity.java" in files,
    "Both phones publish/receive video; mute, permissions, exit, reconnect and abuse protections",
    "Video server + camera permissions and 2-device test"),
row("1. 3D/Video","Live broadcast server/publisher",
    code("rtmp://") or code("Rtmp") or code("LiveStream"),
    "One publisher, multiple viewers; stable stream/rejoin",
    "RTC/streaming backend"),
row("2. Party","Create/join live Firestore Party",
    file_has("PartyActivity.java","live_rooms","roomId"),
    "Unique users A+B join same room, see seats, recover after network loss",
    "Deploy and check current Firebase rules"),
row("2. Party","Host/admin role assign and revoke",
    file_has("PartyActivity.java","administratorDialog","cohost"),
    "Host assigns Admin; Admin moderates but cannot change Owner; revoked loses access",
    "Live backend permissions"),
row("2. Party","Seat DP profile / kick / ban / billboard",
    file_has("PartyActivity.java","openSeatProfile","kickUser","roomBillboardDialog"),
    "Each control changes second phone instantly; private data protected",
    "Two devices and Firestore rules"),
row("3. Games","Ludo real multiplayer",
    "OnlineLudoActivity.java" in files,
    "A/B legal turns, sync, reconnect, winner computed once",
    "Two devices running same match"),
row("3. Games","Other room games in Firestore",
    file_has("RoomGameActivity.java","game_moves","game_ready"),
    "Non-demo state validated across devices for every supported game",
    "Rules/timer/result fairness testing"),
row("3. Games","Inline room Dice with listener cleanup",
    file_has("KingRoomDice997.java","ListenerRegistration","dice_room_997"),
    "Both phones see the same die rolls while remaining in voice room",
    "Firestore events write/listen and mobile test"),
row("4. Gifts/VIP","Distinct art and sound",
    file_has("KingGiftArtView.java","drawText","Camera")
    and file_has("KingGiftSound993.java","signature(String gift)"),
    "Each gift has distinct artwork and matching heard audio on both phones",
    "Real device audio/animation review"),
row("4. Gifts/VIP","Animated emoji catalogue and security",
    file_has("KingEmojiAccess993.java","CATALOG_SIZE=120") 
    and file_has("PartyActivity.java","showLiveEmojiPanelV530"),
    "50+ actions have distinct motion and correct synced room effects",
    "Visual and memory testing"),
row("4. Gifts/VIP","Verified paid VIP/Noble and diamonds",
    (file_has("KingPremiumAccess981.java","PAID_VIP_ACTIVE = true") 
      and file_has("KingRecharge974Activity.java","LIVE_RECHARGE = true")),
    "Server validates payment once; ranks accurate; refunds reversed; fraud tests pass",
    "Billing onboarding, verified backend & entitlement ledger"),
row("5. Login/Social","Google auth",
    file_has("MainActivity.java","GoogleAuthProvider"),
    "A/B sign in, persist session and recover after restart",
    "Two accounts"),
row("5. Login/Social","Facebook auth",
    file_has("MainActivity.java","FacebookAuthProvider"),
    "Facebook account login and linking work with real provider configuration",
    "Meta Developer App setup"),
row("5. Login/Social","Phone OTP login",
    file_has("MainActivity.java","PhoneAuthProvider"),
    "Real OTP arrives, validates, and logs in; no crashes or bypasses",
    "Phone Auth project configuration and quota"),
row("5. Login/Social","ID, Follow and Private Chat",
    file_has("SocialActivity.java","Follow Back","public_profiles")
    and file_has("ChatActivity.java","direct_threads"),
    "Different UIDs A/B find each other, follow, follow back, message and persist",
    "Live Firebase rules and two-phone test"),
row("5. Login/Social","True 1-to-1 video calling",
    "KingVideoCallActivity.java" in files or code("PeerConnectionFactory"),
    "Both phones connect video/audio privately, mute and hang up",
    "RTC provider + real device checks"),
]
def esc(v):
    return str(v).replace("|","\\|").replace("\n"," ")
lines=[
"# KING Plus — Five-part verified parity audit",
"",
"**Reference APK:** Bolo Hi v5.30.5; **local signed baseline:** KING Plus v9.9.7.",
"",
"## Important interpretation",
"Any 'CODE DETECTED' means only a Java implementation anchor was found. It is **not** proof of real gameplay, server deployment, successful login, proper VIP access or stable use on different devices.",
"",
"| Section | Feature | Static source audit | Required real-world acceptance | Blocker |",
"|---|---|---|---|---|"
]
for r in entries:lines.append("| "+" | ".join(esc(x) for x in r)+" |")
lines+=["","## Results",
f"- Source Java classes inspected: **{len(files)}**",
f"- Code anchors present: **{sum('CODE DETECTED' in r[2] for r in entries)}/{len(entries)}**",
"- Device, security and backend criteria completed: **not verified** in this offline CI audit.",
"- Play Store production readiness: **not verified**.",
"",
"## Integrity safeguards",
"- Do not classify non-verified code or preview-only buttons as complete.",
"- Do not deploy permissive Firebase rules or accept client-generated paid VIP entitlements.",
"- Do not copy protected Bolo Hi artwork, models, recordings, or proprietary code.",
"- A successful Android build is only a necessary technical step, not acceptance of the five sections.",
"",
"## Next action",
"Check actual Firebase project and deployed rules; then run acceptance A/B on two separate devices. Configure Facebook / official payment provider / original 3D assets and RTC before full release."
]
dest=Path(os.environ.get("KING_PARITY_REPORT","/tmp/king-five-part-parity.md"))
dest.parent.mkdir(parents=True,exist_ok=True)
dest.write_text("\n".join(lines)+"\n",encoding="utf-8")
print("REPORT",dest,"CLASS_COUNT",len(files),"CODE_ANCHORS",sum("CODE DETECTED" in r[2] for r in entries),"TOTAL",len(entries))
print("\n".join(lines[:28]))
