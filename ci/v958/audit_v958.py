#!/usr/bin/env python3
"""Whole-app static crash risk inventory for KING Plus built APK sources. No user data."""
from pathlib import Path
import re
root=Path('/tmp/src/app/src/main')
pkg=root/'java/com/kingplus/social'
paths=sorted(pkg.glob('*.java'))
print('=== WHOLE APP CRASH AUDIT ===')
print('JAVA_FILES',len(paths),'TOTAL_JAVA_LINES',sum(len(p.read_text(errors='replace').splitlines()) for p in paths))
manifest=(root/'AndroidManifest.xml').read_text(errors='replace')
activities=re.findall(r'<activity\\b[^>]*android:name="([^"]+)"',manifest)
print('MANIFEST_ACTIVITIES',len(activities))
for p in paths:
 s=p.read_text(errors='replace')
 rows=s.splitlines()
 hits=[]
 rules=[
 ('FORCED_EXIT',r'\\bSystem\\.exit\\s*\\(|\\bProcess\\.killProcess\\s*\\('),
 ('AUTH_NULL_RISK',r'getCurrentUser\\(\\)\\.getUid\\(\\)'),
 ('NATIVE_INIT',r'JitsiMeet\\.instantiateReactNative|new org\\.jitsi\\.meet\\.sdk\\.JitsiMeetView|\\.join\\(options'),
 ('GLOBAL_CRASH',r'UncaughtExceptionHandler|setDefaultUncaughtExceptionHandler|Thread\\.setDefaultUncaughtExceptionHandler'),
 ('DELAYED_UI',r'postDelayed\\s*\\('),
 ('BITMAP_DECODE',r'BitmapFactory\\.decode|Bitmap\\.createBitmap'),
 ('SNAPSHOT_LISTENER',r'addSnapshotListener\\s*\\('),
 ('ON_DESTROY',r'\\bonDestroy\\s*\\('),
 ('NATIVE_LOAD',r'System\\.loadLibrary\\s*\\('),
 ]
 for tag,pat in rules:
  ln=[i+1 for i,line in enumerate(rows) if re.search(pat,line)]
  if ln: hits.append(tag+'='+str(len(ln))+'@'+','.join(map(str,ln[:12])))
 if hits: print('FILE',p.name,'LINES',len(rows),' '.join(hits))
for name,keys in {
 'PartyActivity.java':['void onCreate(', 'void renderParty(', 'void attachInRoomVoice940(', 'void stopInRoomVoice940(', 'void leaveRoom(', 'void onResume(', 'void onDestroy(', 'void onStop(', 'void clearListeners(', 'void renderLobby(', 'roomVoiceOptedIn957','partyShell940='],
 'MainActivity.java':['void onCreate(', 'void showGlobalCrashDiagnostic940(', 'void installCrashReport(', 'void onResume(', 'void onDestroy(', 'getCurrentUser().getUid()'],
 'KingApplication.java':['void onCreate(', 'setDefaultUncaughtExceptionHandler','void onTrimMemory('],
 'KingStability.java':['class KingStability','void install(', 'setDefaultUncaughtExceptionHandler','lastCrash','void nonFatal('],
 'KingMultiVideoActivity.java':['void onCreate(','JitsiMeet.instantiateReactNative','void onDestroy(', 'void onResume(']
}.items():
 p=pkg/name
 if not p.exists():
  print('MISSING',name);continue
 rows=p.read_text(errors='replace').splitlines()
 print('=== TARGET',name,'LINE_COUNT',len(rows),'===')
 used=set()
 for key in keys:
  indices=[i for i,s in enumerate(rows) if key in s]
  print('KEY',key,'AT',','.join(str(i+1) for i in indices[:10]))
  for i in indices[:3]:
   for k in range(max(0,i-3),min(len(rows),i+12)):
    if k not in used:print(f'{k+1}: {rows[k][:350]}');used.add(k)
for name in ['app/build.gradle', 'app/src/main/AndroidManifest.xml']:
 p=Path('/tmp/src')/name
 txt=p.read_text(errors='replace').splitlines()
 print('=== CONFIG',name,'===')
 for i,line in enumerate(txt):
  if re.search('jitsi|firebase|kotlin|heap|largeHeap|hardwareAccelerated|application|KingApplication|versionName|crashlytics|targetSdk|compileSdk|activity',line,re.I):
   print(f'{i+1}: {line[:220]}')
print('=== END STATIC AUDIT; DEVICE RUNTIME CRASH NOT VERIFIED ===')
