#!/usr/bin/env python3
from pathlib import Path
import re, sys, shutil

root=Path('.')
source=Path(sys.argv[1] if len(sys.argv)>1 else '/tmp/v850-firestore.rules')
old=source.read_text()
start=old.index('    // Online casual Ludo. All moves and server-timestamp dice are checked here.')
end=old.index('    match /users/{uid} {', start)
ludo=old[start:end].rstrip()+"\n\n"

rules=root/'firestore.rules'
s=rules.read_text()
if 'match /ludo_matches/{code}' not in s:
    anchor='    match /users/{uid} {'
    if anchor not in s: raise SystemExit('firestore.rules users anchor not found')
    s=s.replace(anchor,ludo+anchor,1)
    rules.write_text(s)

parts=sorted((root/'ci/v851/parts').glob('OnlineLudoActivity.java.part*'))
if not parts: raise SystemExit('Online Ludo Java parts missing')
dst=root/'app/src/main/java/com/kingplus/social/OnlineLudoActivity.java'
dst.parent.mkdir(parents=True,exist_ok=True)
dst.write_text(''.join(x.read_text() for x in parts))

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
if '.OnlineLudoActivity' not in m:
    anchor='<activity android:name=".GamePlayActivity" android:exported="false" />'
    if anchor not in m: raise SystemExit('Manifest GamePlayActivity anchor not found')
    m=m.replace(anchor,anchor+'\n        <activity android:name=".OnlineLudoActivity" android:exported="false" />',1)
    manifest.write_text(m)

main=root/'app/src/main/java/com/kingplus/social/MainActivity.java'
m=main.read_text()
old_method='private void openPlayableGame(String game){Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}'
new_method='private void openPlayableGame(String game){if("ludo".equalsIgnoreCase(game)){startActivity(new Intent(this,OnlineLudoActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}'
if old_method in m:
    m=m.replace(old_method,new_method,1)
elif 'OnlineLudoActivity.class' not in m:
    raise SystemExit('MainActivity openPlayableGame anchor not found')
main.write_text(m)

room=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
r=room.read_text()
if 'import android.content.Intent;' not in r:
    r=r.replace('import android.app.AlertDialog;','import android.app.AlertDialog;\nimport android.content.Intent;',1)
ready_line='Button ready=button("✅ Ready / Not Ready");ready.setOnClickListener(v->toggleReady750());controls.addView(ready,new LinearLayout.LayoutParams(-1,dp(50)));'
if 'Online Ludo' not in r:
    if ready_line not in r: raise SystemExit('RoomGameActivity ready anchor not found')
    extra=ready_line+'\n        Button ludo=button("🎲 Online Ludo");ludo.setOnClickListener(v->startActivity(new Intent(this,OnlineLudoActivity.class)));LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);'
    r=r.replace(ready_line,extra,1)
room.write_text(r)

gradle=root/'app/build.gradle'
g=gradle.read_text()
g=re.sub(r"versionCode\s+114;\s*versionName\s+'8\.3\.1'","versionCode 115; versionName '8.5.1'",g,count=1)
if "versionName '8.5.1'" not in g: raise SystemExit('Unable to set v8.5.1 version')
gradle.write_text(g)

print('Applied KING Plus v8.5.1 Online Ludo integration')
