from pathlib import Path
import sys
p=Path(sys.argv[1]);pkg=p/'app/src/main/java/com/kingplus/social'
me=(pkg/'MainActivity.java').read_text()
home=(pkg/'CommunityHubActivity.java').read_text()
party=(pkg/'PartyActivity.java').read_text()
assert 'followingLoaded965=false;\n                    liveFollowing965.clear();' in me
assert 'followersLoaded965=false;\n                    liveFollowers965.clear();' in me
assert 'if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");' in me
assert 'db.runTransaction(tx->{' in home
assert 'tx.set(ownerRef10009,mem)' in home
assert 'saveCreatedFamily10009(familyUid10009,code,familyName955,' in home
assert 'tx.set(r,f)' in home
assert 'KING_FAMILY_CODE_COLLISION_10009' in home
assert 'r.delete();familyError955("Could not create owner membership"' not in home
assert 'KingMeHomePartyGuard10009.canPublishGame' in party
assert 'KingMeHomePartyGuard10009.nextGameRound' in party
assert 'tx.set(currentGame10009,d)' in party
assert 'if(!isModerator()||!memberSeen)' in party
assert 'versionCode 288;' in (p/'app/build.gradle').read_text()
print('PASS KING v10.0.9 Me, Home, Party source checks (14 cases)')
