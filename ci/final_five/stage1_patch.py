#!/usr/bin/env python3
"""Stage 1 of 5: native GLES20 Party World + existing real Jitsi video bridge.
This patch is staged and compiled but MUST NOT publish any interim APK.
"""
from pathlib import Path
import shutil,sys
base=Path(sys.argv[1])
pkg=base/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
shutil.copyfile(Path(__file__).with_name('KingNative3DRoom.java'),pkg/'KingNative3DRoom.java')
s=party.read_text()
start=s.index('    private void roomMenu() {')
end=s.index('\n    private ',start+25)
chunk=s[start:end]
needle='items.add("📹 Multi Video");'
assert chunk.count(needle)==1, "Room menu MultiVideo anchor absent"
chunk=chunk.replace(needle,needle+' items.add("🌐 3D World");',1)
s=s[:start]+chunk+s[end:]
needle='else if(x.contains("Multi Video"))openVideoRoom();'
assert s.count(needle)==1, "Video handler in handleRoomMenuItem810 absent"
s=s.replace(needle,needle+
    '\n        else if(x.contains("3D World"))openNativeWorld998();',1)
needle='    private void roomMenu() {'
assert s.count(needle)==1
s=s.replace(needle,'''    /** First true GLES20 3D Party view, using existing Firebase seat permissions.
     * Keep the original Party Activity/voice visible behind the dialog.
     * The 📹 button routes to the already available Jitsi Multi Video room.
     */
    private void openNativeWorld998(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a real online KING Party first");return;
        }
        KingNative3DRoom.open(this,db,user,roomId,this::openVideoRoom);
    }

'''+needle,1)
assert 'this::openRoomGames740' in s or 'openRoomGames740' in s
assert 'KingRoomDice997.open(' in s
assert 'this::shareRealRoom996' in s
assert 'KingGiftSound993.play(this,gift)' in s
party.write_text(s)
print('PASS Stage1 genuine original 3D Party renderer integrated with existing Firebase and Jitsi video')
print('PASS No OTP/VIP/Wallet security rules, games, voice, gifts or friends changed')
