#!/usr/bin/env python3
"""Read-only source audit of integrated KING Plus: login, family/community, notifications, security & stability. NEVER touches production Firebase."""
from pathlib import Path
p=Path('/tmp/kingfeature/app/src/main/java/com/kingplus/social')
files=sorted(p.glob("*.java"))
print("TOTAL JAVA FILES",len(files))
tags=["Login","Auth","Otp","Facebook","Family","Community","Notification","Invite","Message","Crash","Stabil","Privacy","Report","Block","Profile","Home","Main","Social","Gift","Video"]
print("FEATURE FILES")
for f in files:
 if any(tag.lower() in f.name.lower() for tag in tags):
  print(f.name, len(f.read_text(errors="replace").splitlines()))
patterns={
 "MainActivity.java":["setContentView(","onCreate(","Facebook","Sign in","Continue with","Logout","signOut","FirebaseMessaging","Notification","Family","Community","report","block"],
 "LoginActivity.java":["onCreate(","Continue","Facebook","Google","Mobile","Privacy","Terms","setContentView("],
 "SocialActivity.java":["family","community","notification","friend","block","report"],
 "PartyActivity.java":["familyPanel","fanClub","notification","notify","sendNotification","block","report","invite","Family Party","Friend Broadcast"],
 "KingPublicProfileActivity.java":["toggleBlock","report(", "openCommunity","private void share"],
 "RoomGameActivity.java":["ensureMembership900"],
}
for file,terms in patterns.items():
 f=p/file
 if not f.exists():continue
 lines=f.read_text(errors="replace").splitlines()
 print("\nFILE",file,"LINES",len(lines))
 for term in terms:
  hits=[i for i,s in enumerate(lines) if term.lower() in s.lower()]
  print("FIND",term,len(hits),"LINES",[i+1 for i in hits[:16]])
  for i in hits[:2]:
   print(f"{i+1:05} {lines[i][:1100]}")
for f in files:
 if any(x in f.name.lower() for x in ["family","communit","notification","facebook","auth","login","stabil","crash","privacy","moderation"]):
  lines=f.read_text(errors="replace").splitlines()
  print("\nSPECIAL",f.name)
  for i,l in enumerate(lines[:150]):print(f"{i+1:05} {l[:1400]}")
manifest=Path('/tmp/kingfeature/app/src/main/AndroidManifest.xml')
print("MANIFEST_SIGNALS")
if manifest.exists():
 for i,line in enumerate(manifest.read_text().splitlines()):
  if any(t in line for t in ["permission","<activity","<service","queries","usesCleartextTraffic","exported","networkSecurityConfig"]):
   print(i+1,line[:900])
print("AUDIT_END")
