#!/usr/bin/env python3
"""Stage 2: locate real room/seat join defects for cross-phone Firebase sync.
Source inspection only, NO production Firebase access and NO APK publishing.
"""
from pathlib import Path
root=Path('/tmp/kingfinalroom')
party=(root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text().splitlines()
social=(root/'app/src/main/java/com/kingplus/social/SocialActivity.java').read_text().splitlines()
rules=(root/'firestore.rules').read_text().splitlines()
for fn,ls,searches in [
 ("PartyActivity.java",party,["joinCode","shortId(","put(\"join","createRoomDialog(","createCloud","joinCloud","memberSeen","directRoomId","addSnapshotListener(","roomBanned","seatNo","seatInvites"]),
 ("SocialActivity.java",social,["joinFullRoom996(","socialJoinParty992(","whereEqualTo(\"joinCode\"","KingRoomInvite959.parse"]),
 ("firestore.rules",rules,["match /live_rooms/","match /members/","match /seats/","match /roles/","match /room_bans/"])
]:
 print("\nAUDIT_FILE",fn)
 for term in searches:
  hits=[i for i,l in enumerate(ls) if term in l]
  print("SEARCH",term,"FOUND",len(hits),"LINES",[i+1 for i in hits[:20]])
  for i in hits[:3]:
   for j in range(max(0,i-1),min(len(ls),i+3)):
    print(f"{j+1:05}: {ls[j][:620]}")
print("JOIN_CODE_SCHEMA_AUDIT_COMPLETE")
