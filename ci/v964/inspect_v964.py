#!/usr/bin/env python3
"""Inspect the actual v9.6.3 Android app source for cross-phone ID, room and memory issues."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
pkg=root/"app/src/main/java/com/kingplus/social"
targets={
 "PartyActivity.java":["private void renderParty(", "private void renderLobby(", "private String shortId(", "private void openRoomDoc891(", "private void joinRoomLinkDialog959(", "private boolean tryJoinRoomInput959(", "private void attachInRoomVoice940(", "private void onDestroy(", "private void leaveRoom(", "private void clearListeners(", "private void renderRoom(", "private void enterRoom(", "void createCloudRoom(", "roomId=", "roomId =","joinCode","roomSearch","loadProfilePhoto", "profilePhoto", "roomId.substring", "BitmapFactory", "GLSurfaceView", "JitsiMeetView","KingRoomCoverView","liveEmojiStage"],
 "MainActivity.java":["private String publicId(", "String publicId(", "private void syncPublicProfile(", "private void restoreCanonicalGoogleProfile931(", "private void applyCanonicalIdentity931(", "private void showMe(", "private void renderMe(", "private void loadProfilePhoto(", "private void fetchRemoteProfile(", "private void openPartyActivity(", "private void routePartyInvite959(", "private void follow(", "Follow", "publicId(", "KING ID", "clearCrossAccountIdentity931", "public_profiles", "firebaseAuth"],
 "KingPublicProfileActivity.java":["publicId(", "follow(", "toggleFollow", "public_profiles","follows","King ID", "onCreate("],
 "SocialActivity.java":["showLocalDemo(", "runSearch(", "loadDiscover(", "toggleFollow(", "followId(", "shortUid(", "sync", "onCreate(", "public_profiles","follows"],
 "KingRoomCoverView.java":["onDraw(", "Bitmap", "Paint", "invalidate", "postInvalidate", "onAttachedToWindow"],
 "KingRoomGridView.java":["onDraw(", "Bitmap", "invalidate"],
 "KingRoomBackdropView.java":["onDraw(", "invalidate"],
}
for f,keys in targets.items():
 p=pkg/f
 print("\n===== FILE",f,"PRESENT",p.exists(),"=====")
 if not p.exists():continue
 s=p.read_text(errors="replace")
 lines=s.splitlines()
 print("LENGTH",len(s),"LINES",len(lines))
 for key in keys:
  hits=[]
  start=0
  while True:
   j=s.lower().find(key.lower(),start)
   if j<0:break
   hits.append(j);start=j+len(key)
  if not hits:continue
  print("KEY",repr(key),"HITS",len(hits))
  for at in hits[:3]:
   lo=max(0,at-130)
   hi=min(len(s),at+1450)
   print("POS",at,"LINE",s.count("\n",0,at)+1,"SOURCE",s[lo:hi].replace("\n","\\n"))
print("\n=== ALL JAVA public IDs and room prefixes ===")
for f in sorted(pkg.glob("*.java")):
 s=f.read_text(errors="replace")
 for term in ["publicId(", "shortId(", "getLastSignedInAccount(", "KING ID:", "roomCode", "roomId ==", "getUid().hashCode(", "getUserId()", "joinRoomByCode"]:
  if term in s:
   print(f.name,term,s.count(term))
print("=== END ===")
