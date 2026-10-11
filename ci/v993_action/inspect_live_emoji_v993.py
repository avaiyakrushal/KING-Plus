#!/usr/bin/env python3
from pathlib import Path
import re
pkg=Path("/tmp/king-993-source/app/src/main/java/com/kingplus/social")
targets=[
("PartyActivity.java",["showLiveEmojiPanelV530","showLiveEmojiEffect560","giftShopPanel","showGiftEffect","showGiftEffect(", "premiumRoomBackground","vip","KingEmojiAuraView992","KingGiftArtView"]),
("LiveEmojiView.java",["LABELS","public LiveEmojiView","onDraw","onDetachedFromWindow"]),
("KingPremiumAccess981.java",["class KingPremiumAccess981"]),
("KingGiftSafety971.java",["class KingGiftSafety971"]),
("KingFreeGift973.java",["class KingFreeGift973"]),
("KingGiftArtView.java",["public KingGiftArtView"]),
("GiftBurstView.java",["public GiftBurstView"]),
("KingRecharge974Activity.java",["LIVE_RECHARGE"]),
]
for filename,needles in targets:
 f=pkg/filename
 if not f.exists(): print("MISSING",filename);continue
 src=f.read_text().splitlines()
 print("\n===== FILE",filename,"TOTAL",len(src))
 for needle in needles:
  inds=[i for i,l in enumerate(src) if needle in l]
  print("### KEY",needle,"LINEs",[i+1 for i in inds[:10]])
  if filename=="PartyActivity.java":
   chosen=inds[:2];before=8;after=75 if needle=="giftShopPanel" else (90 if needle=="showLiveEmojiPanelV530" else 55)
  else: chosen=inds[:1];before=5;after=95 if needle=="LABELS" else 35
  for i in chosen:
   print("\n".join(f"{j+1:5} {src[j]}" for j in range(max(0,i-before),min(len(src),i+after))))
print("DONE_993_INSPECT")
