#!/usr/bin/env python3
"""Stage 14: inspect the actual *Stage13* Party quick-action dispatcher, not older branches."""
from pathlib import Path
p=Path('/tmp/king-stage14/app/src/main/java/com/kingplus/social')
src=(p/'PartyActivity.java').read_text().splitlines()
queries=[
 "private void handleRoomMenuItem810(","private void roomMenu()","private void roomMenuAll()",
 "private void toggleMic","private void refreshMicControl","case \"quick-tool\"",
 "micOn?","private void handleRoomBack","onBackPressed","private void setRoomFlag(","private void openDeepFlow810(",
 "private void openRequestedPanel940(","private void showOccupiedSeatMenuV530(",
]
for q in queries:
 hits=[i for i,l in enumerate(src) if q in l]
 print('FIND',repr(q),'TOTAL',len(hits),'LINES',[i+1 for i in hits[:13]])
 for i in hits[:1]:
  for j in range(max(0,i-3),min(len(src),i+(135 if 'handleRoomMenu' in q else 25))):
   print(f'#{j+1:05}: {src[j][:950]}')
for file,patterns in [
 ('KingPublicProfileActivity.java',['private String blockId','private void toggleBlock','collection("blocks")']),
 ('PartyActivity.java',['private void blockHost','blocked','private void reportHost'])
]:
 lines=(p/file).read_text().splitlines()
 for term in patterns:
  hits=[i for i,x in enumerate(lines) if term in x]
  print('BLOCK_INSPECT',file,repr(term),'TOTAL',len(hits),'LINES',[i+1 for i in hits[:15]])
  for i in hits[:4]:
   for j in range(max(0,i-2),min(len(lines),i+4)):
    print(f'#{j+1:05}: {lines[j][:1100]}')
print('STAGE14_ROUTER_AUDIT_DONE')
