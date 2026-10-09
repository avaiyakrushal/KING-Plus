from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/"app/src/main/java/com/kingplus/social"
targets={
"PartyActivity.java":[
"private void toggleMic", "private void requestSeat", "private void takeSeat", "private void sit", "private void kick", "private void lockSeat", "private void inviteToSeat", "private void renderSeat", "private void setMic",
"private void seat", "private void attachSeat", "private void listenSeat", "private void clearSeat", "private void openSeat", "private void startInRoomVoice",
"private void stopInRoomVoice", "private void ensureInRoomVoice","private void setVoice", "private void joinVoice", "private void mute",
"private void renderParty","private void clearListeners","private void attachListeners","private void watchSeat","private void setSeat","private void mic",
"seatInvitesListener", "seatListener","seatRequestListener","seatLocksListener","seatInviteListener","inRoomVoice", "roomVoiceOptedIn957",
"seatNo", "micOn", "micEnabled", "joinVoice", "JitsiMeetActivity", "JitsiMeetView", "seat_invites", "seat_requests", "seat_locks", "collection(\"seats\")"],
"KingPartyVoiceActivity.java":["onCreate(","micOn","JitsiMeet","onDestroy("],
"KingPartyRoomActivity.java":["onCreate(","micOn","JitsiMeet"]
}
for fn,terms in targets.items():
 p=pkg/fn
 if not p.exists():print("MISSING",fn);continue
 lines=p.read_text().splitlines()
 print("FILE",fn,"LEN",len(lines))
 found=[]
 for t in terms:
  positions=[(i+1,ln[:850]) for i,ln in enumerate(lines) if t.lower() in ln.lower()]
  print("TERM",repr(t),"HITS",len(positions))
  for i,ln in positions[:12]:
   print(f"SNIP {i}: {ln}")
   if t.startswith("private void") or t.endswith("Listener"):
    found.append(i)
 found=sorted(set(found))
 for i in found[:80]:
  print("AROUND",i)
  for n in range(max(1,i-1),min(len(lines),i+10)+1):
   print(f"{n}: {lines[n-1][:900]}")

print("===== EXACT PARTY VOICE / SEAT METHODS =====")
for start,end in [(942,954),(1251,1347),(1470,1613),(4195,4240),(4270,4345),(4350,4395),(4405,4448)]:
 print(f"===== LINES {start}-{end} =====")
 for n in range(start-1,min(end,len(lines))):
  print(f"{n+1}: {lines[n][:1300]}")
