#!/usr/bin/env python3
"""Stage 13: correct one misleading Party option.

Advanced menu currently advertises Lock/Unlock "seats" but the click actually
writes live_rooms/{id}.locked (room entry). Per-seat locks have separate
Firestore documents and can be controlled through their own moderation UI.
Rename this option to Lock/Unlock ROOM so no owner accidentally locks room
entry when trying to lock microphones. Never change actual seat lock rules.
"""
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=f.read_text()
old='items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");'
new='items.add(roomLocked?"🔓 Unlock room":"🔒 Lock room");'
count=s.count(old)
assert count==1,f'Expected one advanced owner room-lock label, got {count}'
s=s.replace(old,new,1)
assert 'else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);' in s
assert 'private void setRoomFlag(String key,boolean value)' in s
assert 'collection("seat_locks")' in s
assert 'KingNative3DRoom.open(' in s and 'KingRoomDice997.open(' in s
assert 'private void roomMenuAll()' in s and 'private void roomMenu()' in s
assert 'else if(x.contains("Party Master Help"))partyMasterHelp610();' in s
f.write_text(s)
print('PASS: Room lock label now matches live_rooms.locked, does not mislead owners as seat lock')
print('PASS: individual seat locking and all original Party tools still present')
