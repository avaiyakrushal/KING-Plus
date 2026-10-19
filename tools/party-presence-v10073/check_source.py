from pathlib import Path
root=Path(__file__).resolve().parents[2]
source=(root/"app/src/main/java/com/kingplus/social/PartyActivity.java").read_text()
gradle=(root/"app/build.gradle").read_text()
assert "versionCode 352" in gradle
assert "versionName '10.0.73-party-cleanup-race-fix'" in gradle
cleanup=source[source.index("private void cleanupStaleRoom910()"):source.index("private void setOnlineState900(",source.index("private void cleanupStaleRoom910()"))]
helper=cleanup[cleanup.index("private void cleanupStaleMember910("):]
assert "KingRoomPresence967.isStale(last967.toDate().getTime(),now967,300000L)" in cleanup
assert "DocumentSnapshot current=tx.get(memberRef)" in helper
assert "seen.toDate().getTime(),checkedAt,300000L" in helper
assert helper.index("tx.get(seatRef)") < helper.index("tx.delete(memberRef)")
assert "candidateUid.equals(seat.getString(\"uid\"))" in helper
print("PASS stale Party cleanup rechecks member timestamp and owned seats transactionally")
