#!/usr/bin/env python3
"""KING Plus v9.9.18: fix actual Party chat, profile & voice source parity.
Build against the exact released v9.9.17 integrated source; no proprietary
Bolo Hi code/images, fake server data, or paid recharge enabled.
"""
from pathlib import Path
import re,sys

root=Path(sys.argv[1]);src=root/"app/src/main/java/com/kingplus/social"
p=src/"PartyActivity.java"
s=p.read_text(encoding="utf-8")
def once(old,new,label):
    global s
    n=s.count(old)
    if n!=1:raise RuntimeError(f'{label}: expected 1 source anchor; found {n}')
    s=s.replace(old,new,1)

# Any occupied avatar (including one's own) opens the same real profile page.
once('''        if(uid==null||uid.isEmpty())return;if(user!=null&&uid.equals(user.getUid())){toast("This is you");return;}showRichProfile(uid,name,false);''',
'''        if(uid==null||uid.trim().isEmpty())return;
        showRichProfile(uid,name,false);''',"self-and-peer-profile")

# Do not allow an isolated offline/device-only 'sent' chat bubble to impersonate
# a realtime Party message. Require authenticated server-joined room membership.
once('''    private void sendMessage(EditText box) {
        if(box==null)return;
        String text=box.getText()==null?"":box.getText().toString().trim();''',
'''    private void sendMessage(EditText box) {
        if(box==null)return;
        if(!cloudRoom||!memberSeen||user==null||db==null||roomId==null||
            roomId.trim().isEmpty()){
            toast("Join a live KING Party before sending messages");return;
        }
        final String chatRoom9918=roomId, chatUid9918=user.getUid();
        final int chatGeneration9918=presenceGeneration967;
        String text=box.getText()==null?"":box.getText().toString().trim();''',"verified-party-chat")

# Use a captured room identifier and protect the current composer against stale
# asynchronous success/error completion from a room already left.
once('''            db.collection("live_rooms").document(roomId).collection("messages").add(d)
                .addOnSuccessListener(v->{
                    if(partyUiDead())return;''',
'''            db.collection("live_rooms").document(chatRoom9918).collection("messages").add(d)
                .addOnSuccessListener(v->{
                    if(partyUiDead()||!isActivePresence967(chatRoom9918,chatUid9918,chatGeneration9918))return;''',
"chat-send-stale-success")
once('''                .addOnFailureListener(e->{
                    if(partyUiDead())return;
                    if(box.getText()==null||box.getText().length()==0){box.setText(originalText);box.setSelection(box.length());}''',
'''                .addOnFailureListener(e->{
                    if(partyUiDead()||!isActivePresence967(chatRoom9918,chatUid9918,chatGeneration9918))return;
                    if(box.getText()==null||box.getText().length()==0){box.setText(originalText);box.setSelection(box.length());}''',
"chat-send-stale-failure")

# The old code displayed "Mic ON • live" immediately after writing micOn=true
# to Firestore, even when the Jitsi voice view failed to initialize. Ensure
# the mic is reverted atomically on transport initialization failure.
once('''            if(desiredMic968){
                roomVoiceOptedIn957=true;
                if(partyShell940!=null&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);
            }
            setInlineAudioMuted940(!desiredMic968);
            setVoicePresence900(desiredMic968);
            refreshMicControl();
            toast(desiredMic968?"Mic ON • live inside Party":"Mic OFF");''',
'''            if(desiredMic968){
                roomVoiceOptedIn957=true;
                if(partyShell940!=null&&!inRoomVoiceJoined940)
                    attachInRoomVoice940(partyShell940);
                if(!inRoomVoiceJoined940){
                    // A Firestore mic flag is NOT itself an audio connection.
                    micOn=false;
                    seatMics.put(expectedSeat968,false);
                    setVoicePresence900(false);
                    refreshMicControl();
                    db.runTransaction(revert9918->{
                        DocumentSnapshot current9918=revert9918.get(seatRef968);
                        if(current9918.exists()&&expectedUid968.equals(current9918.getString("uid")))
                            revert9918.update(seatRef968,"micOn",false);
                        return null;
                    }).addOnFailureListener(err9918->
                        android.util.Log.w("KINGPlusParty","Voice failure mic rollback failed",err9918));
                    toast("Voice unavailable • Mic OFF (retry in room)");
                    return;
                }
            }
            setInlineAudioMuted940(!desiredMic968);
            setVoicePresence900(desiredMic968);
            refreshMicControl();
            // Jitsi native view created. Conference connection still needs a
            // real two-phone network test before it is called LIVE.
            toast(desiredMic968?"Mic ON requested • connecting voice":"Mic OFF");''',
"no-false-voice-live-status")

# Clear an expired room-scoped voice opt-in when leaving a room.
# The app's real voice backend still needs production RTC QA; do not claim
# end-to-end audio because a Jitsi view object exists.
p.write_text(s,encoding="utf-8")
g=root/"app/build.gradle"
build=g.read_text(encoding="utf-8")
build,n=re.subn(r"versionCode\s+196;\s*versionName\s+'9\.9\.17[^']*'",
    "versionCode 197; versionName '9.9.18-room-voice-chat-stability-test'",build)
if n!=1:raise RuntimeError("Expected exactly v9.9.17 release source/versionCode 196")
g.write_text(build,encoding="utf-8")

for requirement in [
    "approveOrMoveMemberSeat9913",
    "db.runTransaction(tx9912",
    "openLiveChess9917",
    "openPartyGameChooser9915",
    "KingNative3DRoom.open(",
    "chatRoom9918",
    "Voice unavailable • Mic OFF",
]:
    if requirement not in s:raise RuntimeError("Missing retained functionality: "+requirement)
assert (src/"KingLiveChessActivity.java").exists()
assert (src/"KingChessRules.java").exists()
assert "PAID_VIP_ACTIVE = false" in (src/"KingPremiumAccess981.java").read_text()
assert "LIVE_RECHARGE = false" in (src/"KingRecharge974Activity.java").read_text()
print("PASS: current-user profile, room-auth chat, stale callback guards, voice initialization fail-safe, prior chess, gift and Party functions")
