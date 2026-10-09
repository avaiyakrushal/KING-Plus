#!/usr/bin/env python3
"""Built-source whole-app crash risk audit. Static checks only; no device or user data."""
from pathlib import Path
import re
root=Path('/tmp/src/app/src/main')
pkg=root/'java/com/kingplus/social'
paths=sorted(pkg.glob('*.java'))
manifest=(root/'AndroidManifest.xml').read_text(errors='replace')
print('=== WHOLE APP CRASH AUDIT ===',flush=True)
print('JAVA_FILES',len(paths),'TOTAL_JAVA_LINES',sum(len(p.read_text(errors='replace').splitlines()) for p in paths))
print('MANIFEST_ACTIVITIES',len(re.findall(r'<activity\b',manifest)))
needles=[
 ('FORCED_EXIT',['System.exit(', 'Process.killProcess(']),
 ('AUTH_NULL_RISK',['getCurrentUser().getUid()']),
 ('NATIVE_INIT',['JitsiMeet.instantiateReactNative','new org.jitsi.meet.sdk.JitsiMeetView','.join(options']),
 ('GLOBAL_CRASH',['UncaughtExceptionHandler','setDefaultUncaughtExceptionHandler']),
 ('DELAYED_UI',['postDelayed(']),
 ('BITMAP_DECODE',['BitmapFactory.decode','Bitmap.createBitmap']),
 ('SNAPSHOT_LISTENER',['addSnapshotListener(']),
 ('ON_DESTROY',['onDestroy(']),
 ('NATIVE_LOAD',['System.loadLibrary(']),
 ('TIMERS',['scheduleAtFixedRate(', 'new Timer(', 'postDelayed(']),
 ('WEBVIEW',['new WebView(', 'new JitsiMeetView('])
]
for p in paths:
 rows=p.read_text(errors='replace').splitlines()
 hits=[]
 for tag,terms in needles:
  ln=[i+1 for i,line in enumerate(rows) if any(q in line for q in terms)]
  if ln:hits.append(tag+'='+str(len(ln))+'@'+','.join(map(str,ln[:10])))
 if hits:print('FILE',p.name,'LINES',len(rows),' '.join(hits))
targets={
 'PartyActivity.java':['void onCreate(', 'void renderParty(', 'void attachInRoomVoice940(', 'void stopInRoomVoice940(', 'void leaveRoom(', 'void onResume(', 'void onDestroy(', 'void onStop(', 'void clearListeners(', 'void renderLobby(', 'roomVoiceOptedIn957','partyShell940='],
 'MainActivity.java':['void onCreate(', 'void showGlobalCrashDiagnostic940(', 'void installCrashReport(', 'void onResume(', 'void onDestroy(', 'getCurrentUser().getUid()'],
 'KingApplication.java':['void onCreate(', 'setDefaultUncaughtExceptionHandler','void onTrimMemory('],
 'KingStability.java':['class KingStability','void install(', 'setDefaultUncaughtExceptionHandler','lastCrash','void nonFatal('],
 'KingMultiVideoActivity.java':['void onCreate(','JitsiMeet.instantiateReactNative','void onDestroy(', 'void onResume(']
}
for name,keys in targets.items():
 p=pkg/name
 if not p.exists():
  print('MISSING',name);continue
 rows=p.read_text(errors='replace').splitlines()
 print('=== TARGET',name,'LINE_COUNT',len(rows),'===')
 used=set()
 for key in keys:
  indices=[i for i,s in enumerate(rows) if key in s]
  print('KEY',key,'AT',','.join(str(i+1) for i in indices[:10]))
  for i in indices[:2]:
   for k in range(max(0,i-2),min(len(rows),i+10)):
    if k not in used:
     print(f'{k+1}: {rows[k][:270]}')
     used.add(k)
for name in ['app/build.gradle','app/src/main/AndroidManifest.xml']:
 p=Path('/tmp/src')/name
 txt=p.read_text(errors='replace').splitlines()
 print('=== CONFIG',name,'===')
 for i,line in enumerate(txt):
  if re.search(r'jitsi|firebase|kotlin|heap|largeHeap|hardwareAccelerated|application|KingApplication|versionName|crashlytics|targetSdk|compileSdk|activity',line,re.I):
   print(f'{i+1}: {line[:220]}')
print('=== END STATIC AUDIT; DEVICE RUNTIME CRASH NOT VERIFIED ===',flush=True)

# Focused review of delayed tasks, crash handler and native/lifecycle ordering.
for name,ranges in {
 "KingApplication.java":[(1,36)],
 "KingStability.java":[(1,46)],
 "GamePlayActivity.java":[(104,151),(185,207)],
 "RoomGameActivity.java":[(246,274)],
 "PartyActivity.java":[(310,337),(255,275),(1330,1350),(3879,3959),(4035,4052)],
 "MainActivity.java":[(205,231),(1415,1431),(1474,1524),(1673,1691)],
 "KingMultiVideoActivity.java":[(165,183)]
}.items():
 p=pkg/name
 if not p.exists():continue
 rows=p.read_text(errors='replace').splitlines()
 for start,end in ranges:
  print('=== DEEP',name,start,end,'===')
  for index in range(max(0,start-1),min(len(rows),end)):
   print(f'{index+1}: {rows[index][:360]}')
