#!/usr/bin/env python3
from pathlib import Path
import sys,re
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
for n in ['CommunityHubActivity.java','ChatActivity.java','KingVipVisualActivity.java','InboxActivity.java']:
 f=p/n;s=f.read_text(errors='replace');l=s.splitlines()
 print('===FILE',n,len(l),'===')
 for i,x in enumerate(l[:115]):print(f'{i+1}: {x[:1000]}')
 print('===KEYS===')
 for key in ['stopFamilyMessages956','familyStopped956','familyMessages956','onStart','onStop','onDestroy','showFamily(','r.collection("members").get()','r.collection("activities").orderBy','sendDirectGift620(','directGiftPanel620(','LevelSystem.read(this)','wallets','private void build()','private void newChatDialog()','private void openChat(']:
  hits=[m.start() for m in re.finditer(re.escape(key),s)]
  print('HIT',key,len(hits))
  for at in hits[:3]:
   pos=s.count('\n',0,at)
   print('POS',pos+1)
   for i in range(max(0,pos-2),min(len(l),pos+8)):
    print(str(i+1)+": "+l[i][:1300])
