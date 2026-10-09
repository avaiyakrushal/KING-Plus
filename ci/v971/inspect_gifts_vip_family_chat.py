#!/usr/bin/env python3
"""Audit real v9.7.0 classes for Live Emoji, gifts, VIP, Family, Chat.
Print source neighborhoods and backend/security-dependent feature gaps."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
files=sorted(pkg.glob('*.java'))
targetWords=("gift","emoji","vip","family","chat","billing","wallet","coin","live","level","friend")
print("FILES_TOTAL",len(files))
for p in files:
 if any(w in p.stem.lower() for w in targetWords):
  s=p.read_text(errors="replace")
  print("CLASS",p.name,"LINES",s.count("\n")+1,"BYTES",len(s))
print("=== PARTY FEATURE METHOD HEADINGS ===")
s=(pkg/'PartyActivity.java').read_text(errors="replace")
lines=s.splitlines()
pattern=re.compile(r'(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?(?:void|boolean|String|int|long|View|TextView|LinearLayout|Map<.*?>)\s+([A-Za-z_][\w]*)\s*\(')
found=[]
for i,line in enumerate(lines):
 m=pattern.search(line)
 if m and any(z in m.group(1).lower() for z in targetWords+("react","effect","play","send","purchase","announce","event","room")):
  found.append((i+1,m.group(1),line[:550]))
print("MATCHED_PARTY_METHODS",len(found))
for i,name,line in found[:170]:print("METHOD",i,name,line)
print("=== PARTY CODE SNIPPETS ===")
keys=["private void sendGift", "private void showGift", "private void gift", "private void liveEmoji",
      "private void playLiveEmoji", "private void sendReaction", "private void broadcast",
      "private void watchRoomEvents", "collection(\"events\")", "collection(\"gifts\")",
      "private void showVip", "private void openVip","private void showFamily", "private void openFamily",
      "private void loadGifts","private void renderGift", "private void showLiveEmoji",
      "private void renderLiveEmoji","giftPrice", "vipLevel", "KingGift", "wallet",
      "giftOverlay", "giftHistory", "private void addEvent"]
occupied=set()
for k in keys:
 pos=[i for i,x in enumerate(lines) if k.lower() in x.lower()]
 print("HITS",repr(k),len(pos))
 for p in pos[:4]:
  print("ANCHOR",k,p+1)
  for n in range(max(0,p-3),min(len(lines),p+14)):
   if n in occupied:continue
   print(f"{n+1}: {lines[n][:1250]}")
   occupied.add(n)
print("=== OTHER FEATURE ACTIVITY HEADINGS ===")
for f in files:
 if not any(x in f.stem.lower() for x in ["gift","emoji","vip","family","chat","billing","wallet","level","live","inbox"]):continue
 l=f.read_text(errors="replace").splitlines()
 print("FEATURE",f.name,"TOTAL_LINES",len(l))
 for i,x in enumerate(l):
  if pattern.search(x) or 'collection(' in x or 'SetOptions' in x:
   if len(x)>1200:x=x[:1200]
   print(f"{i+1}: {x}")
print("=== FIRESTORE IMPORTANT RULES ===")
rule=(root/'firestore.rules').read_text().splitlines()
for a,b in [(118,143),(482,565),(577,605)]:
 for i in range(a-1,min(len(rule),b)):
  print(f"RULE {i+1}: {rule[i]}")
