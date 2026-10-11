#!/usr/bin/env python3
from pathlib import Path
import re
base=Path("/tmp/king995/app/src/main/java/com/kingplus/social")
print("STABILITY_AUDIT_BEGIN")
for name in ["PartyActivity.java","LiveEmojiView.java","KingEmojiAuraView992.java",
             "GiftBurstView.java","KingGiftArtView.java","GamesActivity.java",
             "KingGamesActivity.java","MainActivity.java"]:
    p=base/name
    if not p.exists():print("FILE_MISSING",name);continue
    lines=p.read_text().splitlines()
    print("\nFILE",name,"LINES",len(lines))
    patterns = {
      "PartyActivity.java":[
       "onCreate(", "onStart(", "onResume(", "onPause(", "onStop(", "onDestroy(",
       "onTrimMemory(", "onLowMemory(", "onBackPressed(",
       "ListenerRegistration", "addSnapshotListener(", "addEventListener(",
       "removeAllViews(", "postDelayed(", "postInvalidateDelayed(",
       "liveEmojiStageV530", "giftFxActive971", "release", "cancel", "FrameLayout",
       "private void stop", "private void cleanup", "endParty", "detach",
       "setOnDismissListener(","void onDetachedFromWindow(", "registerReceiver(", "unregisterReceiver("],
      "LiveEmojiView.java":["onAttachedToWindow(", "onDetachedFromWindow(", "onDraw(", "postInvalidateDelayed(", "onVisibilityChanged(", "onWindowVisibilityChanged(", "setLayerType("],
      "KingEmojiAuraView992.java":["onDraw(", "postInvalidateDelayed(", "onDetachedFromWindow("],
      "GiftBurstView.java":["start(", "onDetachedFromWindow(", "ValueAnimator", "setLayerType("],
      "KingGiftArtView.java":["onDraw(", "postInvalidateDelayed(", "onDetachedFromWindow(", "setLayerType("],
      "GamesActivity.java":["onCreate(", "onDestroy(", "onTrimMemory(", "setBackground("],
      "KingGamesActivity.java":["onCreate(", "onDestroy(", "onTrimMemory(", "setBackground("],
      "MainActivity.java":["onTrimMemory(", "onDestroy(", "onPause("],
    }.get(name,[])
    hits=[]
    for i,line in enumerate(lines):
        if any(pattern in line for pattern in patterns):
            hits.append(i)
    print("HITS",len(hits))
    for i in hits[:115]:
        print(f"L{i+1:05} {lines[i][:390]}")
    if name=="PartyActivity.java":
        # selected method details to understand animation and cleanup ordering
        for key in ["onPause()", "onDestroy()", "onStop()", "showLiveEmojiEffect560(",
                    "showGiftEffect(", "private void cleanup", "private void stop", "private void leave",
                    "onTrimMemory(", "private void renderParty()"]:
            occ=[i for i,x in enumerate(lines) if key in x]
            if not occ:continue
            for i in occ[-2:]:
                print("CONTEXT",key,i+1)
                for j in range(max(0,i-2),min(len(lines),i+22)):
                    print(f"{j+1:05} {lines[j][:460]}")
print("STABILITY_AUDIT_END")

print("\nTARGETED PARTY LIFECYCLE CONTEXT")
p=base/"PartyActivity.java"
li=p.read_text().splitlines()
for lo,hi in [(245,322),(333,408),(1930,2015),(5156,5178)]:
 print("BLOCK",lo,hi)
 for i in range(lo-1,min(len(li),hi)):
  print(f"{i+1:05} {li[i][:600]}")
print("\nTARGETED RENDERERS")
for filename in ["KingRoomStageView.java","KingLoginBackdropView.java","KingPartyMemory969.java"]:
 p=base/filename
 if not p.exists():continue
 li=p.read_text().splitlines()
 print("FILE",filename)
 for i,line in enumerate(li):
  if any(x in line for x in ["postInvalidateDelayed(", "onDraw(", "onDetachedFromWindow(", "onWindowVisibilityChanged(","onLowMemory(", "onTrimMemory("]):
   for j in range(max(0,i-3),min(len(li),i+6)):
    print(f"{j+1:05} {li[j][:600]}")
