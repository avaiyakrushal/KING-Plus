from pathlib import Path

root = Path(__file__).resolve().parents[2]
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
deep = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 379' in gradle and "10.0.100-party-treasure-hunt-live" in gradle
assert '♡ Liked …' in main and '♡ Liked "+(count>500?"500+":String.valueOf(count))' in main
assert 'collectionGroup("likes")' in main and '.whereEqualTo("uid",account).limit(501)' in main
assert 'addSnapshotListener((snapshot,error)->{' in main
assert 'stopProfileLikes10088();' in main and 'profileLikesListener10088==null' in main
assert 'openCommunityHub700(tab)' in main
assert 'tap to copy' in main and 'ClipData.newPlainText("KING ID",issued)' in main
assert 'if(!expectedScreen.equals(screen)||active==null||!account.equals(active.getUid()))return;' in main
assert 'Upload, sync and manage your profile videos' in deep
assert 'python3 tools/me-profile-parity-v10088/check_source.py' in workflow
assert 'node tests/firestore/me-profile-likes-v10088.test.mjs' in workflow
likes_test = (root / 'tests/firestore/me-profile-likes-v10088.test.mjs').read_text()
assert 'liked-count-alice-v10088' in likes_test
assert "versionCode='379'" in workflow
print('PASS: Me shows a live, signed-in account count for liked Moments and opens the existing Liked feed')
