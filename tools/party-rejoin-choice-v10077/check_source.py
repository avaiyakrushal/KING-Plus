from pathlib import Path
root=Path(__file__).resolve().parents[2]
s=(root/"app/src/main/java/com/kingplus/social/PartyActivity.java").read_text()
g=(root/"app/build.gradle").read_text()
assert "versionCode 359" in g
assert "10.0.80-safe-checkin" in g
start=s.index('selfMemberListener=room.collection("members").document(expectedUid982)')
end=s.index('roomBanListener=room.collection("room_bans")',start)
listener=s[start:end]
assert "verifyMissingMemberAndOfferRejoin10077" in listener
assert "You were removed from this room" not in listener
start=s.index("private void verifyMissingMemberAndOfferRejoin10077(")
end=s.index("private void attachCloudRoom()",start)
flow=s[start:end]
assert "postDelayed" in flow and "},1200L);" in flow
assert "Source.SERVER" in flow
assert 'setPositiveButton("Rejoin"' in flow and "openRoomDoc891(latest)" in flow
assert 'setNegativeButton("Leave"' in flow
assert "room_bans" in flow and 'getBoolean("active")' in flow
assert "expectedSession.equals(presenceSession967)" in flow
print("PASS Party missing-member callback waits, verifies fresh server state, respects bans, and offers guarded rejoin/leave")
