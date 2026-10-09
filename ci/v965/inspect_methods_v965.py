from pathlib import Path
import sys,re
pkg=Path(sys.argv[1])/"app/src/main/java/com/kingplus/social"
for fn,terms in {
"MainActivity.java":["private void loadRealProfileData(", "private void profileV600(", "onResume(", "onPause(", "onStop(", "onDestroy(", "private void base(", "private void loadRealProfileData()"],
"KingPublicProfileActivity.java":["private void load()", "private void loadPublicCounts940()", "private String followId()", "private void checkFollow()", "private void toggleFollow()", "onStart(", "onStop(", "onResume(", "onPause(", "onDestroy("],
"SocialActivity.java":["private void toggleFollow(", "private void openProfile(", "onResume(", "onStop(", "onDestroy(", "private void loadDiscover()"],
"PartyActivity.java":["private void joinMemberThenOpen891(", "Same KING account • syncing this phone"]
}.items():
 p=pkg/fn;s=p.read_text();lines=s.splitlines()
 print("FILE",fn)
 for term in terms:
  found=[m.start() for m in re.finditer(re.escape(term),s)]
  print("====",term,"COUNT",len(found))
  for at in found[:1]:
   k=s.count("\n",0,at)
   stop=k+28 if fn!="MainActivity.java" else k+23
   for i in range(max(0,k-2),min(len(lines),stop)):
    print(str(i+1)+":"+lines[i][:580])
