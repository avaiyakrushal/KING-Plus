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
print('STAGE14_ROUTER_AUDIT_DONE')
