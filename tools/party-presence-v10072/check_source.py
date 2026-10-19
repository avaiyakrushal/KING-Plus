from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]
source=(root/"app/src/main/java/com/kingplus/social/PartyActivity.java").read_text()
gradle=(root/"app/build.gradle").read_text()
assert "versionCode 351" in gradle
assert "versionName '10.0.72-party-reconnect-grace'" in gradle
cleanup=source[source.index("private void cleanupStaleRoom910()"):source.index("private void setOnlineState900(",source.index("private void cleanupStaleRoom910()"))]
assert "KingRoomPresence967.isStale(last967.toDate().getTime(),now967,300000L)" in cleanup
assert "180000L" not in cleanup
assert "memberRef967.set(memberPayload967,SetOptions.merge())" in source
print("PASS Party presence allows five minutes for mobile-network reconnect before stale-member cleanup")
