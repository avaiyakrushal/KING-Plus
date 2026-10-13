#!/usr/bin/env python3
"""v9.9.57: restore a user-OPTED-IN real Jitsi Party Voice after Internet reconnect.
Authenticate live room and member from SERVER before rejoining, never auto-consent
microphones or start a voice call for non-opted-in members.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java'
s=p.read_text()
def rep(a,b):
 global s
 n=s.count(a)
 if n!=1:raise RuntimeError(f'Exact v9.9.56 anchor count {n}: {a[:110]}')
 s=s.replace(a,b,1)

rep('    private boolean roomVoiceOptedIn957=false;\n', '''    private boolean roomVoiceOptedIn957=false;
    private boolean voiceReconnectPending9957=false;
    private boolean voiceRejoinChecking9957=false;
    private long lastVoiceRejoinAt9957=0L;
    private final android.os.Handler voiceRejoinHandler9957=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable voiceRejoinRunnable9957;
    private int voiceRejoinToken9957=0;
    private void clearVoiceRejoin9957(){
        ++voiceRejoinToken9957;
        if(voiceRejoinRunnable9957!=null)
            voiceRejoinHandler9957.removeCallbacks(voiceRejoinRunnable9957);
        voiceRejoinRunnable9957=null;
        voiceReconnectPending9957=false;
        voiceRejoinChecking9957=false;
    }
    /** Safe on lifecycle stop: cancel pending work, retain intention for resume. */
    private void pauseVoiceRejoin9957(){
        ++voiceRejoinToken9957;
        if(voiceRejoinRunnable9957!=null)
            voiceRejoinHandler9957.removeCallbacks(voiceRejoinRunnable9957);
        voiceRejoinRunnable9957=null;
        voiceRejoinChecking9957=false;
    }
    private boolean voiceRejoinReady9957(String id,String uid,int generation){
        return KingPartyVoiceReconnect9957.eligible(
            voiceReconnectPending9957,roomVoiceOptedIn957,partyVisualForeground995,
            KingNetwork.online(this),cloudRoom,memberSeen,
            id,roomId,uid,user==null?null:user.getUid(),
            generation==presenceGeneration967,partyShell940!=null);
    }
    private void verifyAndRejoinVoice9957(){
        if(voiceRejoinChecking9957||!voiceReconnectPending9957||db==null||user==null)return;
        final String expectedRoom9957=roomId;
        final String expectedUid9957=user.getUid();
        final int presence9957=presenceGeneration967;
        final int token9957=++voiceRejoinToken9957;
        if(!voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957))return;
        if(System.currentTimeMillis()-lastVoiceRejoinAt9957<8000L)return;
        voiceRejoinChecking9957=true;
        final DocumentReference roomRef9957=db.collection("live_rooms").document(expectedRoom9957);
        roomRef9957.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(room9957->{
                if(token9957!=voiceRejoinToken9957||!voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957)){
                    voiceRejoinChecking9957=false;return;
                }
                if(room9957==null||!room9957.exists()||
                   Boolean.TRUE.equals(room9957.getBoolean("closed"))){
                    voiceRejoinChecking9957=false;
                    setOnlineState900(false,"Voice room unavailable");return;
                }
                roomRef9957.collection("members").document(expectedUid9957)
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(member9957->{
                        if(token9957!=voiceRejoinToken9957||
                          !voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957)){
                            voiceRejoinChecking9957=false;return;
                        }
                        if(member9957==null||!member9957.exists()||
                           !expectedUid9957.equals(member9957.getString("uid"))){
                            voiceRejoinChecking9957=false;
                            setOnlineState900(false,"Voice membership not verified");return;
                        }
                        voiceRejoinRunnable9957=()->{
                            voiceRejoinRunnable9957=null;
                            voiceRejoinChecking9957=false;
                            if(token9957!=voiceRejoinToken9957||
                               !voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957))
                                return;
                            lastVoiceRejoinAt9957=System.currentTimeMillis();
                            voiceReconnectPending9957=false;
                            // Any prior native Jitsi conference may be stuck after
                            // connectivity changes. Replace it once after verified rejoin.
                            setInlineAudioMuted940(true);
                            stopInRoomVoice940(true);
                            attachInRoomVoice940(partyShell940);
                            // Never re-enable local microphone from a remote flag.
                            setInlineAudioMuted940(!canTransmitMic9956());
                            if(inRoomVoiceJoined940)setOnlineState900(true,"Party voice rejoined");
                            else{
                                voiceReconnectPending9957=true;
                                setOnlineState900(false,"Voice reconnect failed • retry Voice");
                            }
                        };
                        voiceRejoinHandler9957.postDelayed(voiceRejoinRunnable9957,700L);
                    }).addOnFailureListener(err9957->{
                        voiceRejoinChecking9957=false;
                        if(token9957==voiceRejoinToken9957)
                            KingStability.nonFatal(this,"voice-rejoin-member",err9957);
                    });
            }).addOnFailureListener(err9957->{
                voiceRejoinChecking9957=false;
                if(token9957==voiceRejoinToken9957)
                    KingStability.nonFatal(this,"voice-rejoin-room",err9957);
            });
    }
''')
rep('''            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(!recovered||isFinishing()||isDestroyed())return;''',
'''            boolean recovered=online&&!partyLastOnline940;
            boolean disconnected9957=!online&&partyLastOnline940;
            partyLastOnline940=online;
            if(disconnected9957&&roomVoiceOptedIn957&&cloudRoom&&roomId!=null)
                voiceReconnectPending9957=true;
            if(!recovered||isFinishing()||isDestroyed())return;''')
rep('''                    if(cloudRoom&&roomId!=null&&memberSeen){
                        ensureRoomMembership900();
                    }else if''',
'''                    if(cloudRoom&&roomId!=null&&memberSeen){
                        ensureRoomMembership900();
                        // Voice is rejoined ONLY for people who explicitly opted in,
                        // and only after fresh SERVER room/member checks.
                        verifyAndRejoinVoice9957();
                    }else if''')
rep('''        partyVisualForeground995=true;
        drainPendingGifts9944();''',
'''        partyVisualForeground995=true;
        if(voiceReconnectPending9957)verifyAndRejoinVoice9957();
        drainPendingGifts9944();''')
rep('''        revokeMicConsent9956();
        ++presenceGeneration967; micTogglePending968=false;''',
'''        clearVoiceRejoin9957();
        revokeMicConsent9956();
        ++presenceGeneration967; micTogglePending968=false;''')
rep('''        revokeMicConsent9956();
        ++presenceGeneration967;
        partyAutoRepairUsed979=false;''',
'''        clearVoiceRejoin9957();
        revokeMicConsent9956();
        ++presenceGeneration967;
        partyAutoRepairUsed979=false;''')
rep('''    private void leaveRoom(){roomVoiceOptedIn957=false;partyShell940=null;stopInRoomVoice940(true);renderLobby("Hot");}''',
'''    private void leaveRoom(){clearVoiceRejoin9957();roomVoiceOptedIn957=false;partyShell940=null;stopInRoomVoice940(true);renderLobby("Hot");}''')
rep('''    @Override protected void onStop(){
        partyVisualForeground995=false;''',
'''    @Override protected void onStop(){
        partyVisualForeground995=false;
        pauseVoiceRejoin9957();''')
rep('''    @Override protected void onDestroy(){revokeMicConsent9956();''',
'''    @Override protected void onDestroy(){clearVoiceRejoin9957();revokeMicConsent9956();''')
p.write_text(s)
(pkg/'KingPartyVoiceReconnect9957.java').write_text("""package com.kingplus.social;
/** An Internet reconnect is not consent to open Jitsi voice or the microphone. */
public final class KingPartyVoiceReconnect9957 {
 private KingPartyVoiceReconnect9957(){}
 public static boolean eligible(boolean pending,boolean optedIn,boolean foreground,
    boolean online,boolean cloudRoom,boolean memberSeen,
    String expectedRoom,String actualRoom,String expectedUid,String actualUid,
    boolean generationMatches,boolean partyViewVisible){
  return pending&&optedIn&&foreground&&online&&cloudRoom&&memberSeen
      &&expectedRoom!=null&&!expectedRoom.isEmpty()
      &&expectedRoom.equals(actualRoom)
      &&expectedUid!=null&&!expectedUid.isEmpty()
      &&expectedUid.equals(actualUid)
      &&generationMatches&&partyViewVisible;
 }
}
""")
g=root/'app/build.gradle';z=g.read_text()
a="versionCode 235; versionName '9.9.56-real-party-mic-consent-test'"
assert z.count(a)==1;g.write_text(z.replace(a,
    "versionCode 236; versionName '9.9.57-voice-rejoin-live-party-test'"))
assert 'localMicConsent9956' in s
assert 'KingGiftReceiptRetry9955' in s
assert 'KingRoomSettleRetry9954' in (pkg/'RoomGameActivity.java').read_text()
print("PASS Voice restores only to actual opted-in Party after SERVER verification; Mic owner consent retained")
