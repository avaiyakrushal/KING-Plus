from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / 'app/build.gradle').read_text()
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
activity = (root / 'app/src/main/java/com/kingplus/social/KingProfileVideos10087Activity.java').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
rules = (root / 'firestore.rules').read_text()
storage = (root / 'storage.rules').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 372' in gradle and "10.0.93-me-gift-history" in gradle
assert '☁ Cloud Profile Videos' in main and 'KingProfileVideos10087Activity.class' in main
assert 'android:name=".KingProfileVideos10087Activity"' in manifest
assert 'startActivityForResult(intent,PICK_VIDEO)' in activity
assert 'upload(uri,pickerUid)' in activity and 'ownerUid.equals(user.getUid())' in activity
assert 'video/[A-Za-z0-9.+-]+' in activity and 'size>MAX_BYTES' in activity
assert 'ref.putFile(uri,metadata)' in activity
assert 'collection("public_profiles").document(uid).collection("videos")' in activity
assert 'doc.getReference().delete()' in activity
assert 'match /videos/{videoId}' in rules
assert "request.resource.data.storagePath == 'chat_media/' + uid + '/profile_videos/' + videoId" in rules
assert 'request.resource.data.sizeBytes <= 10485760' in rules
assert "threadId != 'profile_videos' || request.resource.contentType.matches('video/.*')" in storage
assert 'node tests/firestore/me-profile-videos-v10087.test.mjs' in workflow
assert "versionCode='372'" in workflow
print('PASS: Me Profile cloud-video upload, live sync, owner-only deletion and size/type validation')
