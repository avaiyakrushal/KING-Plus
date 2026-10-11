#!/usr/bin/env python3
from pathlib import Path
import re
pkg=Path('/tmp/king990-inspect/app/src/main/java/com/kingplus/social')
def pr(name, start, end=None, limit=130):
 p=pkg/name
 if not p.exists(): print('MISSING',name);return
 lines=p.read_text().splitlines()
 print('\n====',name, len(lines))
 if isinstance(start,int):
  for i in range(start-1,min(len(lines),end or start+limit)):
   print(f'{i+1}: '+lines[i][:450])
 else:
  for match in start:
   hits=[i for i,l in enumerate(lines) if match in l]
   print('KEY',match,'HITS',[i+1 for i in hits[:15]])
   for i in hits[:3]:
    for j in range(max(0,i-2),min(len(lines),i+24)):
     print(f'{j+1}: '+lines[j][:500])
pr('MainActivity.java',286,337)
pr('PartyActivity.java',1400,1532)
pr('PartyActivity.java',['premiumRoomBackground()', 'addRoomBackgroundGlow(', 'atmospherePanel()', 'showGiftEffect(', 'showLiveEmojiEffect560(', 'giftShopPanel()', 'showGiftCenter('])
pr('KingRoomStageView.java',1,135)
pr('KingLoginBackdropView.java',1,130)
pr('KingGiftArtView.java',1,110)
pr('GiftBurstView.java',1,100)
print('INSPECTION_END')
