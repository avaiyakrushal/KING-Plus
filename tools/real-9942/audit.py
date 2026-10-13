#!/usr/bin/env python3
"""Inspect actual successful 9.9.41 Android source for app lifecycle + Gift reliability."""
from pathlib import Path
import sys
r=Path(sys.argv[1]);s=r/'app/src/main/java/com/kingplus/social'
def excerpt(name,start,end):
 p=s/name;l=p.read_text().splitlines();print("\n###",name,start,end,"total",len(l))
 for k in range(start-1,min(len(l),end)):
  print(f"{k+1}: {l[k][:1900]}")
p=s/'PartyActivity.java'
ls=p.read_text().splitlines()
for q in ["giftReceiptListener9941","seenGiftReceipts9941","confirmGiftReceived9941(","eventSnapshotReady","processedEventIds","partyVisualForeground995","void onResume","void onPause","private void clearListeners","giftSendPending971"]:
 hits=[i for i,x in enumerate(ls) if q in x]
 print("\n### MATCH",q,"count",len(hits),[i+1 for i in hits[:25]])
 for i in hits[:6]:
  for k in range(max(0,i-3),min(len(ls),i+6)):
   print(f'{k+1}: {ls[k][:1300]}')
excerpt("PartyActivity.java",5645,5755)
excerpt("PartyActivity.java",3170,3225)
excerpt("RoomGameActivity.java",90,150)
excerpt("RoomGameActivity.java",170,230)
