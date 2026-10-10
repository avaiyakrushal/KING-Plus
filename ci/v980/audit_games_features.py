#!/usr/bin/env python3
"""Audit actual v9.7.9 Android game list and feature hooks from last successfully compiled source."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
fs=list(pkg.glob('*.java'))
print('ANDROID GAME CLASS CANDIDATES')
for f in fs:
 s=f.read_text(errors='replace')
 if any(x in f.name.lower() for x in ['game','ludo','tic','carrom','chess','race','slot','dice','rps','frame','vip','gift','emoji']):
  print(f.name,len(s),s.count('\n')+1)
print('VERSION',*[s for s in (root/'app/build.gradle').read_text().splitlines() if 'versionCode' in s])
print('RESOURCE XML GAME MENU')
for file in (root/'app/src/main/res').rglob('*.xml'):
 if any(x in file.stem.lower() for x in ('game','ludo','frame','vip')):
  print(file.relative_to(root))
keys={
'MainActivity.java':[
 'private void gamesPage(', 'private void game', 'private void showGame', 'private void openGames',
 'private void ludo', 'private void mini', 'openGames', 'KingLudoLobbyActivity.class','OfflineLudo',
 'OnlineLudo','Game', 'Frame', 'Gift', 'VIP', 'liveEmoji'],
'PartyActivity.java':[
 'private void openGames','private void showGames','private void game',
 'private void playGame','private void launchGame','private void roomGames',
 'void game','KingLudoLobbyActivity.class','KingOfflineLudoActivity.class',
 'Game', 'Frame', 'Gift', 'VIP'],
'KingLudoLobbyActivity.java':['onCreate(', 'private void', 'Button ', 'launch', 'startActivity('],
'OnlineLudoActivity.java':['onCreate(', 'private void', 'private boolean','transaction','gameType','Firestore','whereEqualTo'],
'KingOfflineLudoActivity.java':['onCreate(', 'private void','rules','dice'],
'KingGameArtView.java':['onDraw(','Game','new Canvas'],
}
for name,words in keys.items():
 file=pkg/name
 if not file.exists():print('MISSING',name);continue
 text=file.read_text(errors='replace')
 lines=text.splitlines()
 print('=== FILE',name,'LINES',len(lines),'BYTES',len(text))
 seen=set()
 for q in words:
  match=[i for i,l in enumerate(lines) if q.lower() in l.lower()]
  print('MATCH',repr(q),'COUNT',len(match),'AT',[i+1 for i in match[:25]])
  for i in match[:10]:
   if i in seen:continue
   seen.add(i)
   print(f'{i+1}: {lines[i][:2600]}')
 print('=== PUBLIC/METHOD HEADINGS ===')
 for i,l in enumerate(lines):
  if re.search(r'^\s*(?:private|public|protected)\s+(?:static\s+)?(?:void|boolean|int|String|long|View|TextView|Button|LinearLayout|float|List<[^>]+>)\s+\w+\(',l):
   if (name not in ('MainActivity.java','PartyActivity.java') or any(x in l.lower() for x in ['game','ludo','frame','gift','vip'])):
    print(f'{i+1}: {l[:1250]}')
