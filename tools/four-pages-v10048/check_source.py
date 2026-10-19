from pathlib import Path
src=Path('app/src/main/java/com/kingplus/social')
files={name:(src/name).read_text() for name in ['MainActivity.java','CommunityHubActivity.java','DiscoverActivity.java','ChatActivity.java']}
def check(name,pattern):
 assert pattern in files[name],f'{name} missing {pattern}'
check('MainActivity.java','Activity XP and cosmetics are not account-scoped')
check('MainActivity.java','if(currentGoogleIdentity10045(me))')
assert 'profile.put("xp",progress.xp)' not in files['MainActivity.java']
assert 'profile.put("entranceEffect",KingCosmetics.effect(this))' not in files['MainActivity.java']
check('CommunityHubActivity.java','pendingMomentId10048')
check('CommunityHubActivity.java','ref10048.get(com.google.firebase.firestore.Source.SERVER)')
check('CommunityHubActivity.java','moment_"+java.util.UUID.randomUUID()')
check('DiscoverActivity.java','Set your Hometown in Me')
check('DiscoverActivity.java','orderBy("createdAt",Query.Direction.DESCENDING).limit(60)')
check('ChatActivity.java','latestIncomingMillis10048')
check('ChatActivity.java','readReceiptTarget10048')
check('ChatActivity.java','addOnSuccessListener(done10048')
assert int(__import__('re').search(r'versionCode\s+(\d+)',Path('app/build.gradle').read_text()).group(1)) >= 327
print('PASS v10.0.48 Me, Home, Discover and Messages source invariants')
