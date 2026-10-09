#!/usr/bin/env python3
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java';lines=p.read_text().splitlines()
keys=['premiumRoomBackground(', 'renderParty()', 'new Bitmap', 'BitmapFactory', 'setLayerType(', 'postInvalidate', 'JitsiMeetView', 'attachInRoomVoice940(', 'stopInRoomVoice940(', 'clearListeners()', 'attachCloudRoom(', 'attachRoom', 'addSnapshotListener(', 'new Handler(', 'postDelayed(', 'new Thread(', 'photoCache540', 'activeReactions560', 'roomMusicPlayer', 'onTrimMemory', 'onLowMemory']
for k in keys:
 inds=[i for i,l in enumerate(lines) if k in l]
 print('KEY',repr(k),'COUNT',len(inds),'LINES',[i+1 for i in inds[:36]])
 if any(n in k for n in ['premiumRoomBackground','JitsiMeetView','attachInRoomVoice940','stopInRoomVoice940','photoCache540','activeReactions560','onTrimMemory']):
  for i in inds[:8]:
   print(f'WINDOW {i+1}')
   for j in range(max(0,i-2),min(len(lines),i+16)):
    print(f'{j+1}: {lines[j][:950]}')
print('== RESOURCE FILES ==')
for p in (root/'app/src/main/res').rglob('*'):
 if p.is_file() and p.stat().st_size>1024*1024:
  print(str(p.relative_to(root)),p.stat().st_size)
