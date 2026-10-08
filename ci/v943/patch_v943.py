from pathlib import Path
import sys,re

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).parent

for name in ['KingPartyAmbientView.java','KingVipVisualActivity.java','KingLudoLobbyActivity.java','KingLudoPracticeActivity.java']:
    src=here/name
    if not src.exists(): raise SystemExit(f'missing v943 source file {name}')
    (pkg/name).write_text(src.read_text())

# Register the real offline Ludo practice screen.
manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
if '.KingLudoPracticeActivity' not in m:
    marker='''        <activity android:name=".KingLudoLobbyActivity" android:exported="false" />'''
    if marker not in m: raise SystemExit('manifest KingLudoLobbyActivity marker missing')
    m=m.replace(marker,marker+'\n        <activity android:name=".KingLudoPracticeActivity" android:exported="false" />',1)
manifest.write_text(m)

# Harden the last auth dereference candidate by capturing the current user.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''        final String uid=firebaseAuth.getCurrentUser().getUid();'''
new='''        final com.google.firebase.auth.FirebaseUser profileUser943=(firebaseAuth==null?null:firebaseAuth.getCurrentUser());
        if(profileUser943==null)return;
        final String uid=profileUser943.getUid();'''
if old not in q: raise SystemExit('Main auth dereference marker missing')
q=q.replace(old,new,1)
main.write_text(q)

# Party: permission result must return to the mic action instead of only going to Jitsi.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}'''
new='''    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        if(requestCode==942){
            boolean granted=grantResults!=null&&grantResults.length>0&&grantResults[0]==android.content.pm.PackageManager.PERMISSION_GRANTED;
            if(granted){
                toast("Microphone allowed");
                if(page!=null)page.postDelayed(()->{if(!micOn)toggleMic();},120);
            }else toast("Microphone permission is required to turn the Party mic on");
            return;
        }
        org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }'''
if old not in q: raise SystemExit('Party Jitsi permission marker missing')
q=q.replace(old,new,1)

# Add original lightweight animated atmosphere behind the Party UI.
marker='''        setSafeContentView(shell);'''
insert='''        try{
            KingPartyAmbientView ambient943=new KingPartyAmbientView(this);
            ambient943.setTag("king_party_ambient_943");
            shell.addView(ambient943,0,new FrameLayout.LayoutParams(-1,-1));
        }catch(Throwable ignored){}
        setSafeContentView(shell);'''
if 'king_party_ambient_943' not in q:
    if marker not in q: raise SystemExit('Party shell content marker missing')
    q=q.replace(marker,insert,1)

# Requested panels can now open gift/emoji tools after a direct room entry too.
old='''        else if("games".equals(panel))openRoomGames740();'''
new='''        else if("games".equals(panel))openRoomGames740();
        else if("gift".equals(panel))giftShopPanel();
        else if("emoji".equals(panel))showLiveEmojiPanelV530();'''
if old in q and '"gift".equals(panel)' not in q:q=q.replace(old,new,1)
party.write_text(q)

# Version bump.
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 141; versionName '9.4.2-parity-next'"
if old not in g: raise SystemExit('v9.4.2 version marker missing')
gradle.write_text(g.replace(old,"versionCode 142; versionName '9.4.3-final-parity'",1))

(root/'V9.4.3-WORKLOG.md').write_text('''# KING Plus v9.4.3 final-parity code pass

This stage continues only the Bolo-reference parity work requested by the user.

Completed in code:
- last static Firebase-current-user crash candidate hardened
- Party microphone permission grant now resumes the mic action
- original lightweight animated Party atmosphere added behind the room UI
- direct Party panel routing now includes Gift and Live Emoji
- VIP screen reward/perk/tier/info controls are interactive
- Ludo lobby dead controls replaced with working routes
- real offline two-player Ludo practice mode added
- existing realtime Online Ludo / rematch / reconnect work retained

No proprietary Bolo Hi code or assets are copied.
''')
print('KING Plus v9.4.3 final-parity code patch applied')
