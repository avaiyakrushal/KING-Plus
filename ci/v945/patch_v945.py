from pathlib import Path
import sys
root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'; s=party.read_text()
# Harden room resume/reconnect so realtime membership, seats and voice are rebound.
needle='''            clearListeners();
            renderParty();
            startHeartbeat900();
            setInlineAudioMuted940(!(micOn&&mySeat>0));'''
repl='''            clearListeners();
            renderParty();
            startHeartbeat900();
            try{attachInRoomVoice940();}catch(Throwable ignored){}
            setInlineAudioMuted940(!(micOn&&mySeat>0));'''
if needle not in s: raise SystemExit('party reconnect marker missing')
s=s.replace(needle,repl,1)
party.write_text(s)
# Ensure multiplayer room-game normalization runs after resume where available.
rg=pkg/'RoomGameActivity.java'; q=rg.read_text()
if 'protected void onResume()' not in q:
    marker='''    @Override
    protected void onCreate(Bundle savedInstanceState) {'''
    if marker not in q: raise SystemExit('RoomGameActivity onCreate marker missing')
    q=q.replace(marker,'''    @Override
    protected void onResume(){
        super.onResume();
        try{normalizeRoomGame940();}catch(Throwable ignored){}
    }

'''+marker,1)
    rg.write_text(q)
# Version
g=root/'app/build.gradle'; x=g.read_text()
old="versionCode 143; versionName '9.4.4-navigation-guard'"
if old not in x: raise SystemExit('v944 version missing')
g.write_text(x.replace(old,"versionCode 144; versionName '9.4.5-party-multiplayer'",1))
print('v9.4.5 party multiplayer hardening applied')
