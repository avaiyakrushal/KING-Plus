#!/usr/bin/env python3
from pathlib import Path
root=Path("/tmp/kpall/app/src/main")
p=root/"java/com/kingplus/social"
for fn in ["AndroidManifest.xml","PartyActivity.java","KingMultiVideoActivity.java","KingPublicProfileActivity.java","MainActivity.java","KingRoomStageView.java","KingVideoRoomActivity.java"]:
 path=root/fn if fn.endswith(".xml") else p/fn
 if not path.exists():
  print("MISSING",fn);continue
 lines=path.read_text(errors="replace").splitlines()
 print("\nFILE",fn,"LINES",len(lines))
 if fn=="AndroidManifest.xml":
  for i,l in enumerate(lines):
   if "<activity " in l or "<uses-permission" in l or "uses-feature" in l:print(f"{i+1}: {l[:380]}")
 elif fn=="KingMultiVideoActivity.java":
  for i,l in enumerate(lines[:235]):print(f"{i+1}: {l[:600]}")
 elif fn=="PartyActivity.java":
  for i,l in enumerate(lines):
   terms=["private void roomMenu","private void openRoomGames","private void renderParty","private void roomControlCenter","KingMultiVideoActivity","Video Room","video seat","Multi Video","3D","Avatar","private TextView pill","void launch","public void onCreate","private LinearLayout roomRoot","onCreate(Bundle"]
   if any(t.lower() in l.lower() for t in terms):
    print(f"{i+1}: {l[:720]}")
 elif fn=="KingRoomStageView.java":
  for i,l in enumerate(lines[:125]):print(f"{i+1}: {l[:600]}")
 elif fn=="MainActivity.java":
  for i,l in enumerate(lines):
   if "KingMultiVideoActivity" in l or "KingPublicProfileActivity" in l:
    print(f"{i+1}: {l[:500]}")
print("APP_GRADLE")
g=Path("/tmp/kpall/app/build.gradle")
if g.exists():
 for i,l in enumerate(g.read_text().splitlines()):
  if "version" in l or "compileSdk" in l or "minSdk" in l or "dependencies" in l or "implementation" in l:print(f"{i+1}: {l[:350]}")

party=(p/"PartyActivity.java").read_text().splitlines()
print("ROOM_MENU_DETAIL")
for i in range(3898,3970):
 print(f"{i+1:05} {party[i][:850]}")
