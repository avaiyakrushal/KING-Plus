"""Stability diagnostic: do not auto-start native Jitsi when a Party room renders.
Voice starts only from an explicit mic action in this diagnostic build.
This isolates silent native process exits from UI/Firestore failures.
"""
from pathlib import Path
import sys

root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()

def once(old,new):
    global s
    if s.count(old)!=1:
        raise SystemExit('Expected one Party marker: '+old[:130]+'; found '+str(s.count(old)))
    s=s.replace(old,new,1)

once('    private FrameLayout partyShell940;',
     '    private FrameLayout partyShell940;\n    private boolean roomVoiceOptedIn957=false;')
once('        partyShell940=shell;\n        attachInRoomVoice940(shell);',
     '        partyShell940=shell;\n        // v9.5.7 diagnostic: native Jitsi must not start just because the room UI renders.\n        if(roomVoiceOptedIn957)attachInRoomVoice940(shell);')
once('if(cloudRoom&&roomId!=null&&partyShell940!=null&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);',
     'if(cloudRoom&&roomId!=null&&partyShell940!=null&&roomVoiceOptedIn957&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);')
once('            setVoicePresence900(micOn);',
     '            if(micOn){roomVoiceOptedIn957=true;if(partyShell940!=null&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);}\n            setVoicePresence900(micOn);')
# Mark a fresh room as not opted-in. Prevent background Jitsi joins across room switches.
once('    private void leaveRoom(){stopInRoomVoice940(true);',
     '    private void leaveRoom(){roomVoiceOptedIn957=false;partyShell940=null;stopInRoomVoice940(true);')
p.write_text(s)

g=root/'app/build.gradle'
t=g.read_text()
old="versionCode 147; versionName '9.5.6-family-live-fix'"
if t.count(old)!=1:raise SystemExit('Unexpected v9.5.6 base version')
g.write_text(t.replace(old,"versionCode 148; versionName '9.5.7-party-voice-isolation'",1))
print('Applied v9.5.7 Party voice native-crash isolation')
