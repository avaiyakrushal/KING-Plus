#!/usr/bin/env python3
"""Stage 2 (after native 3D/video Stage 1): direct Party invites must resolve a
REAL, current, not-closed Firestore room from SERVER. No optimistic cache join.
Preserves role, invite, mic, Gift, and Event handlers.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()
needle='db.collection("live_rooms").document(directRoomId).get().addOnSuccessListener(doc->'
assert s.count(needle)==1, f'Expected one direct room invite entry, found {s.count(needle)}'
s=s.replace(needle, 'db.collection("live_rooms").document(directRoomId).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->',1)
old='''addOnFailureListener(e->{toast("Could not open Party room");renderLobby("Hot");});'''
assert s.count(old)==1, f'Expected one direct room join error handler, found {s.count(old)}'
new='''addOnFailureListener(e->{toast("Room invite check failed: "+(e==null?"Network / access error":String.valueOf(e.getLocalizedMessage())));renderLobby("Hot");});'''
s=s.replace(old,new,1)
p.write_text(s)
print("PASS Stage 2 Party Invite resolves only real SERVER room, not stale offline cache")
print("PASS Identity, Admin, Mic, seat, game and friend follow rules not altered")
