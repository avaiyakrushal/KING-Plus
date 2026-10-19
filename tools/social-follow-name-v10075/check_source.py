from pathlib import Path
root=Path(__file__).resolve().parents[2]
social=(root/"app/src/main/java/com/kingplus/social/SocialActivity.java").read_text()
gradle=(root/"app/build.gradle").read_text()
assert "versionCode 360" in gradle
assert "10.0.81-profile-report-history" in gradle
start=social.index("private void toggleFollow(String uid, String name, boolean following)")
end=social.index("private void shareKingId(",start)
toggle=social[start:end]
assert "final String boundedFollowerName970=followerName970;" in toggle
assert "if(followerName970.length()>60)" in toggle
assert 'values970.put("followerName",boundedFollowerName970);' in toggle
assert 'ownPrefs970.getString("name","")' in toggle
print("PASS People/Social Follow records preserve a bounded, account-local follower name")
