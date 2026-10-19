from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / 'app/build.gradle').read_text()
community = (root / 'app/src/main/java/com/kingplus/social/CommunityHubActivity.java').read_text()
flow = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()
firestore_test = (root / 'tests/firestore/liked-moments-v10085.test.mjs').read_text()
rules = (root / 'firestore.rules').read_text()

assert 'versionCode 364' in gradle
assert "10.0.85-liked-moments-profile" in gradle
assert 'else if("Liked".equals(tab))likedMoments10084();' in community
assert 'collectionGroup("likes").whereEqualTo("uid",owner).limit(50)' in community
assert 'like.getReference().getParent().getParent()' in community
assert 'post.get(com.google.firebase.firestore.Source.SERVER)' in community
assert 'Your latest "+posts.size()+" liked Moments' in community
assert '"Liked".equals(selected)&&!liked.exists()' in community
assert 'removeView(card)' in community
assert 'case "detail_profile_liked":openCommunity("Liked");break;' in flow
assert 'Liked Moments' in flow
assert '"♡ Liked"' in main
assert 'momentCardViewCurrent10084' in community
assert 'node tests/firestore/liked-moments-v10085.test.mjs' in workflow
assert "collectionGroup(alice,'likes')" in firestore_test
assert 'users cannot write another account like' in firestore_test
assert 'match /{path=**}/likes/{likeId}' in rules
assert 'allow read: if signedIn() && resource.data.uid == request.auth.uid;' in rules
print('PASS: Me liked-posts page reads the signed-in account likes, loads live source Moments, and removes unlike cards')
