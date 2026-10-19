from pathlib import Path
root=Path(__file__).resolve().parents[2]
discover=(root/"app/src/main/java/com/kingplus/social/DiscoverActivity.java").read_text()
functions=(root/"functions/v3.js").read_text()
gradle=(root/"app/build.gradle").read_text()
assert "versionCode 361" in gradle
assert "10.0.82-chat-block-sync" in gradle
start=discover.index("private void toggleFollow10061(")
end=discover.index("private void openProfile(",start)
toggle=discover[start:end]
assert "fields.put(\"followerName\",followerName)" in toggle
assert "followerName.length()>60" in toggle
assert "?\"KING User\":displayName.trim()" in toggle
assert "const followerName = cleanText(follow.followerName, 60) || 'KING user';" in functions
print("PASS Discover Follow records preserve a bounded follower name for server notification text")
