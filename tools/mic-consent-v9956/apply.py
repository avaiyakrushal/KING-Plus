#!/usr/bin/env python3
"""v9.9.56 real Party mic privacy: host may mute; only seat owner can unmute.
Native Jitsi voice only activates after explicit local tap and same room+seat.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);base=root/'app/src/main/java/com/kingplus/social'
p=base/'PartyActivity.java';s=p.read_text()
def put(a,b):
 global s
 assert s.count(a)==1,(s.count(a),a[:80])
 s=s.replace(a,b,1)
put('    private boolean micTogglePending968;',
'''    private boolean micTogglePending968;
    private boolean localMicConsent9956=false;
    private String consentRoom9956="";
    private int consentSeat9956=-1;
    private void revokeMicConsent9956(){
        localMicConsent9956=false;consentRoom9956="";consentSeat9956=-1;
        setInlineAudioMuted940(true);
    }
    private boolean canTransmitMic9956(){
        return KingPartyMicConsent9956.canTransmit(localMicConsent9956,cloudRoom,
            roomId,consentRoom9956,mySeat,consentSeat9956,
            user==null?null:user.getUid(),seatUids.get(mySeat),
            micOn,muteAll,isModerator());
    }
''')
put('        ++presenceGeneration967; micTogglePending968=false; giftSendPending971=false; giftFxActive971=0; // Clear previous room visuals.',
    '        revokeMicConsent9956();\n        ++presenceGeneration967; micTogglePending968=false; giftSendPending971=false; giftFxActive971=0; // Clear previous room visuals.')
put('        ++presenceGeneration967;\n        partyAutoRepairUsed979=false;',
    '        revokeMicConsent9956();\n        ++presenceGeneration967;\n        partyAutoRepairUsed979=false;')
assert s.count('micV530?"🔇 Mute seat":"🎤 Unmute seat"')==2
s=s.replace('micV530?"🔇 Mute seat":"🎤 Unmute seat"','micV530?"🔇 Mute seat":"🎤 Only seat owner can enable Mic"')
put('            if(w==1){setSeatMicV530(seatNoV530,!micV530);return;}',
'''            if(w==1){
                if(micV530)setSeatMicV530(seatNoV530,false);
                else if(user!=null&&uidV530.equals(user.getUid()))toggleMic();
                else toast("Only that member can turn ON their own Mic");
                return;
            }''')
put('''        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).update("micOn",onV530)''',
'''        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        if(onV530){toast("Only member may enable own Mic");return;}
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).update("micOn",false)''')
put('        final boolean desiredMic968=!micOn;\n',
    '        final boolean desiredMic968=!micOn;\n        if(!desiredMic968)revokeMicConsent9956();\n')
put('''            micOn=desiredMic968;
            seatMics.put(expectedSeat968,desiredMic968);
            if(desiredMic968){''',
'''            if(desiredMic968){
                localMicConsent9956=true;
                consentRoom9956=expectedRoom968;
                consentSeat9956=expectedSeat968;
            }else revokeMicConsent9956();
            micOn=desiredMic968;
            seatMics.put(expectedSeat968,desiredMic968);
            if(desiredMic968){''')
put('''                    micOn=false;
                    seatMics.put(expectedSeat968,false);
                    setVoicePresence900(false);''',
'''                    micOn=false;
                    revokeMicConsent9956();
                    seatMics.put(expectedSeat968,false);
                    setVoicePresence900(false);''')
put('''            if(!desiredMic968)setInlineAudioMuted940(true);
            refreshMicControl();
            toast("Mic sync failed:''',
'''            if(!desiredMic968)revokeMicConsent9956();
            refreshMicControl();
            toast("Mic sync failed:''')
put('''if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}if(mySeat<1)micOn=false;setInlineAudioMuted940(!(micOn&&mySeat>0));''',
'''if(user.getUid().equals(d.getString("uid"))){
                    mySeat=no;
                    boolean serverMic9956=Boolean.TRUE.equals(d.getBoolean("micOn"));
                    if(!serverMic9956)revokeMicConsent9956();
                    micOn=KingPartyMicConsent9956.canTransmit(localMicConsent9956,cloudRoom,
                        roomId,consentRoom9956,mySeat,consentSeat9956,
                        user.getUid(),d.getString("uid"),serverMic9956,muteAll,isModerator());
                }}
            if(mySeat<1){micOn=false;revokeMicConsent9956();}
            setInlineAudioMuted940(!canTransmitMic9956());''')
put('.setAudioMuted(!micOn)\n','.setAudioMuted(!canTransmitMic9956())\n')
put('''        if(!inRoomVoiceJoined940)return;
        try{
            Intent voiceIntent940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(muted);''',
'''        if(!inRoomVoiceJoined940)return;
        // Server micOn alone is NEVER microphone consent.
        boolean actualMuted9956=muted||!canTransmitMic9956();
        try{
            Intent voiceIntent940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(actualMuted9956);''')
put('if(muteAll&&!isModerator()&&micOn){micOn=false;setInlineAudioMuted940(true);',
    'if(muteAll&&!isModerator()&&micOn){micOn=false;revokeMicConsent9956();')
put('@Override protected void onDestroy(){stopPartyJoinTimeout977();',
    '@Override protected void onDestroy(){revokeMicConsent9956();stopPartyJoinTimeout977();')
p.write_text(s)
(base/'KingPartyMicConsent9956.java').write_text("""package com.kingplus.social;
public final class KingPartyMicConsent9956 {
 private KingPartyMicConsent9956(){}
 public static boolean canTransmit(boolean approved,boolean cloud,
      String room,String approvalRoom,int seat,int approvalSeat,
      String user,String seatOwner,boolean backendMic,boolean muteAll,boolean moderator){
    return approved&&cloud&&room!=null&&!room.isEmpty()
       &&room.equals(approvalRoom)&&seat>0&&seat==approvalSeat
       &&user!=null&&!user.isEmpty()&&user.equals(seatOwner)
       &&backendMic&&(!muteAll||moderator);
 }
}
""")
rules=root/'firestore.rules';q=rules.read_text()
a='        allow create: if roomAccess(roomId) && request.resource.data.name is string && ('
b="""        allow create: if roomAccess(roomId) && request.resource.data.name is string
          && request.resource.data.get('micOn',false) != true && ("""
assert q.count(a)==1;q=q.replace(a,b,1)
a="""          || (roomModerator(roomId)
            && request.resource.data.uid == resource.data.uid
            && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['micOn']))"""
b="""          || (roomModerator(roomId)
            && request.resource.data.uid == resource.data.uid
            && request.resource.data.get('micOn',false) == false
            && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['micOn']))"""
assert q.count(a)==1;q=q.replace(a,b,1);rules.write_text(q)
g=root/'app/build.gradle';v=g.read_text()
a="versionCode 234; versionName '9.9.55-real-gift-receipt-retry-test'"
assert v.count(a)==1;g.write_text(v.replace(a,"versionCode 235; versionName '9.9.56-real-party-mic-consent-test'"))
assert 'KingGiftReceiptRetry9955' in s and 'KingRoomSettleRetry9954' in (base/'RoomGameActivity.java').read_text()
print("PASS actual Party Mic consent: owner tap only, moderator Mute only; Games/Gifts preserved")
