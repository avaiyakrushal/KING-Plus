#!/usr/bin/env python3
from pathlib import Path
r=Path("/tmp/gift994/app/src/main/java/com/kingplus/social")
def section(filename,start,end):
 p=r/filename
 lines=p.read_text().splitlines()
 print("\n===== "+filename+" "+str(start)+"-"+str(end)+" =====")
 for n in range(start-1,min(end,len(lines))):print(f"{n+1:05} {lines[n][:400]}")
section("PartyActivity.java",145,177)
section("PartyActivity.java",2488,2555)
section("PartyActivity.java",2550,2635)
section("PartyActivity.java",3035,3090)
section("PartyActivity.java",4260,4310)
section("KingFreeGift973.java",1,100)
section("KingGiftArtView.java",1,140)
for f in ["PartyActivity.java"]:
 s=(r/f).read_text().splitlines()
 for key in ["private void showGiftEffect(", "private void addGiftRecipientChip(", "GiftBurstView fx=", "private void showGiftEffect", "private void sendGiftQuantity(", "giftTargetUid="]:
  idx=[i for i,line in enumerate(s) if key in line]
  print("MATCH",key,[i+1 for i in idx[:20]])
  if key=="private void showGiftEffect(":
   for i in idx:
    for n in range(i,min(i+60,len(s))):print(f"{n+1:05} {s[n][:400]}")
print("GIFT994_INSPECT_DONE")
