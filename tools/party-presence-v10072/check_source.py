from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]
source=(root/"app/src/main/java/com/kingplus/social/PartyActivity.java").read_text()
gradle=(root/"app/build.gradle").read_text()
assert int(__import__('re').search(r'versionCode\s+(\d+)',gradle).group(1))>=351
cleanup=source[source.index("private void cleanupStaleRoom910()"):source.index("private void setOnlineState900(",source.index("private void cleanupStaleRoom910()"))]
assert "KingRoomPresence967.isStale(last967.toDate().getTime(),now967,300000L)" in cleanup
assert "180000L" not in cleanup
transaction=cleanup[cleanup.index("private void cleanupStaleMember910("):]
assert "db.runTransaction(tx->{" in transaction
assert "DocumentSnapshot current=tx.get(memberRef)" in transaction
assert "seen.toDate().getTime(),checkedAt,300000L" in transaction
assert transaction.index("tx.get(seatRef)") < transaction.index("tx.delete(memberRef)")
assert "candidateUid.equals(seat.getString(\"uid\"))" in transaction
assert "memberRef967.set(memberPayload967,SetOptions.merge())" in source
print("PASS Party stale-member and owned-seat cleanup rechecks fresh presence transactionally after its server scan")
