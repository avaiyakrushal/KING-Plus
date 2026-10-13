#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]); s=r/'app/src/main/java/com/kingplus/social'
for fn in ['KingGiftSound993.java','KingGiftDeliveryQueue9944.java','GiftBurstView.java','PartyActivity.java']:
 p=s/fn
 if not p.exists():continue
 a=p.read_text().splitlines()
 print('\n# FILE',fn,'LINES',len(a))
 if fn in ('KingGiftSound993.java','KingGiftDeliveryQueue9944.java'):
  for i,l in enumerate(a):print(f'{i+1}: {l[:1450]}')
 else:
  look=['void showGiftEffect','showGiftEffectConfirmed9944','KingGiftSound993.play','afterVisualComplete9944','drainPendingGifts9944','giftFxActive971','new GiftBurstView','void start(','onDestroy','onPause','onResume','sound','audio']
  for word in look:
   hits=[i for i,x in enumerate(a) if word in x]
   print('SEARCH',word,[x+1 for x in hits[:20]])
   for pos in hits[:6]:
    for j in range(max(0,pos-2),min(len(a),pos+8)):print(f'{j+1}: {a[j][:1300]}')
