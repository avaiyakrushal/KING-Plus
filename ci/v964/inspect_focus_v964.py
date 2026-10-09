#!/usr/bin/env python3
from pathlib import Path
import sys,re
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
M={
"PartyActivity.java":{
"IDENTITY":["photoCache540","shortId(String id)","String joinCode=shortId","void showLobbySearch","private void openCloudRoom","private void joinMemberThenOpen891","void finishMemberJoin921","void openRoomDoc891","void openRoomByCode", "void openCloudRoom","roomVoiceOptedIn957","getIntent().getStringExtra(\"directRoomId\")"],
"MEMORY":["photoCache540=", "new HashMap<String,Bitmap>","void loadProfilePhoto(", "void renderParty()","setContentView(shell)", "void clearListeners()", "void recordRoomVisit940","KingRoomStageView stageBackdrop880", "BitmapFactory.decodeStream", "void addRoomCard", "roomGrid"],
"MEMBERS":["void checkCrowdCapacity930(","void retryMinimalMember921(","private void openCloudRoom(","private void registerMember(","void attachListeners(","void watchMembers(","void openRequestedPanel940("],
},
"MainActivity.java":{
"IDENTITY":["String publicId(String firebaseUid)", "void syncPublicProfile()", "void restoreCanonicalGoogleProfile931(", "void applyCanonicalIdentity931(", "void clearCrossAccountIdentity931(", "void renderMe", "ID: ", "KingPublicProfileActivity.class", "SocialActivity.class"],
"SEARCH":["public_profiles", "follows", "void showSearch", "void showMe", "void openSocial", "void showProfile", "void copyUid"]
},
"SocialActivity.java":{
"SEARCH":["void runSearch(", "void loadDiscover(", "void renderProfiles(", "void showLocalDemo(", "void sync", "void loadProfileCard(", "void toggleFollow(", "void addPerson(", "shortUid(", "followId("]},
"KingPublicProfileActivity.java":{
"PROFILE":["publicId(", "toggleFollow(", "onCreate(", "follows", "search", "uid", "targetUid"]},
"KingRoomStageView.java":{
"STAGE":["onDraw(", "postInvalidate", "invalidate(", "Bitmap", "setLayerType","onAttachedToWindow","onDetachedFromWindow"]},
"KingRoomCoverView.java":{
"STAGE":["onDraw(", "postInvalidate", "invalidate(", "Bitmap", "setLayerType"]},
}
for name, groups in M.items():
 f=p/name
 print('#### FILE',name,'EXISTS',f.exists())
 if not f.exists():continue
 s=f.read_text(errors="replace")
 print("SIZE",len(s),"LINES",s.count("\n")+1)
 for grp, terms in groups.items():
  print("#### GROUP",grp)
  for term in terms:
   ids=[m.start() for m in re.finditer(re.escape(term),s,re.I)]
   print('TERM',repr(term),'COUNT',len(ids))
   for i in ids[:2]:
    lo=max(0,i-90);hi=min(len(s),i+1350)
    print('MATCH','line='+str(s.count("\n",0,i)+1),'prefix='+s[lo:hi].replace("\n","\\n"))
print("END")
