#!/usr/bin/env python3
"""Diagnose v9.6.9 KING Plus follow, user & room IDs and cross-phone Firebase queries."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
def excerpt(s,pos,before=2,after=17):
    l=s.splitlines(); n=s.count('\n',0,pos)
    return "\n".join(f'{i+1}: {l[i][:740]}' for i in range(max(0,n-before),min(len(l),n+after)))
terms={
"MainActivity.java":[
"KingSocialIdentity965.publicId","private void syncPublicProfile", "void applyCanonicalIdentity931",
"void clearCrossAccountIdentity931", "void loadRealProfileData", "void startProfileFollowUpdates965",
"void onResume()", "void onPause()", "public_profiles", "document(uid)", "follows", "String publicId",
"private void showProfile","private void profile","void showMe","profileVisitors"
],
"SocialActivity.java":[
"private void publishOwnProfile", "private void runSearch", "private void loadFollowing",
"private void loadFollowers", "private void loadFriends", "private void loadDiscover",
"private void toggleFollow", "private void renderProfiles", "private void openProfile",
"private void showLocalDemo", "private void refreshSocialView965", "private void onResume",
"void addPerson", "void followId", "data.put(\"publicId\"", "document(raw)", "whereEqualTo(\"publicId\""
],
"KingPublicProfileActivity.java":[
"private void onCreate(", "private void loadPublicCounts940", "private void migrateLegacyFollow965",
"private void checkFollow", "private void toggleFollow", "void onStop()",
"void onStart()", "private void render", "KingSocialIdentity965.publicId", "send", "share",
"getStringExtra", "private void open"
],
"PartyActivity.java":[
"private String shortId", "private void joinRoomByCode964", "private boolean tryJoinRoomInput959",
"private void renderLobby", "void shareRoom", "void openRoomDoc891", "private void createCloudRoom",
"void createRoom", "joinCode", "String id=", "roomInvite"
]
}
for name,keys in terms.items():
 p=pkg/name
 if not p.exists():print('MISSING',name);continue
 s=p.read_text()
 print(f'\n=== FILE {name} chars={len(s)} lines={s.count(chr(10))+1} ===')
 for q in keys:
  found=[m.start() for m in re.finditer(re.escape(q),s,re.I)]
  print(f'KEY {q!r}: {len(found)}')
  for k in found[:2]:
   print('EXCERPT\n'+excerpt(s,k,2,16))
print("\n=== VERSION ===")
g=(root/'app/build.gradle').read_text()
for row in g.splitlines():
 if 'versionCode' in row or 'versionName' in row:print(row)
