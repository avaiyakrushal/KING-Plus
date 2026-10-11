#!/usr/bin/env python3
"""Audit real Android navigation stack across home, party, games, profile, and community.
Read-only; prints only relevant source, no user data.
"""
from pathlib import Path
import re
pkg=Path('/tmp/kingui/app/src/main/java/com/kingplus/social')
files=["MainActivity.java","PartyActivity.java","CommunityHubActivity.java","SocialActivity.java","RoomGameActivity.java","KingMultiVideoActivity.java","KingPublicProfileActivity.java","GamePlayActivity.java","InboxActivity.java","ChatActivity.java","KingParityHubActivity.java","KingEcosystemProActivity.java"]
terms=["onBackPressed","onBackInvoked","navigateUp","onSupportNavigateUp","finish()","new AlertDialog.Builder","showBottom","BottomNavigation","setOnClickListener","startActivity(","roomMenu()","renderParty()","void home(","void home()","void lobby(","void login(","private void render","setContentView("]
for fname in files:
 p=pkg/fname
 if not p.exists():continue
 lines=p.read_text(errors="replace").splitlines()
 print("\nNAV_FILE",fname,"LINES",len(lines))
 for term in terms:
  hits=[i for i,l in enumerate(lines) if term in l]
  if hits:print("NAVTERM",term,"COUNT",len(hits),"LINES", [i+1 for i in hits[:25]])
 # relevant snippets no long repeated dumps
 for i,l in enumerate(lines):
  if "onBackPressed" in l or "onBackInvoked" in l or "private void roomMenu()" in l or ("void home(" in l and "private" in l) or "void navigate(" in l or ("setContentView(" in l and fname=="MainActivity.java"):
   print("NAVBLOCK",fname,i+1)
   for j in range(max(0,i-2),min(len(lines),i+14)):print(f"{j+1:05}: {lines[j][:1250]}")
print("\nPROJECT_NAV_AUDIT_DONE")
