from pathlib import Path
import sys
root=Path(sys.argv[1])
app=root/'app/src/main/java/com/kingplus/social'
me=(app/'MainActivity.java').read_text()
home=(app/'CommunityHubActivity.java').read_text()
party=(app/'PartyActivity.java').read_text()
policy=(app/'KingRoomLifecycle10006.java').read_text()
assert 'if(familyStopped956){familyStopped956=false;render(selected);}' in home
assert '++searchGeneration9994' in home and 'private void loadVisitorPage10005' in home
assert 'if("profile".equals(screen)){' in me
assert 'loadProfileVisitors940(firebaseAuth.getCurrentUser().getUid())' in me
assert '@Override protected void onPause() { ++visitorDialogGeneration10004;' in me
assert 'shell.addView(reactionBanner,bannerLp10006)' in party
assert 'roomRoot.addView(reactionBanner,new LinearLayout.LayoutParams(1,1))' not in party
assert 'bannerLp10006.topMargin=dp(184)' in party
assert 'new KingRoomLifecycle10006()' in party
assert 'roomLifecycle10006.nextBanner()' in party
assert 'roomLifecycle10006.invalidateBanners()' in party
assert 'currentBanner10006(gameBanner10005' in party
assert 'currentBanner10006(entranceBanner10006' in party
assert 'currentBanner10006(giftBanner10006' in party
assert 'currentBanner10006(banner,serial10006' in party
assert 'KingRoomLifecycle10006.acceptsRoomWrite' in party
assert 'db.collection("live_rooms").document(originRoom10006).collection("game_state")' in party
assert 'private void attachGameState' in party
assert 'versionCode 285;' in (root/'app/build.gradle').read_text()
assert 'public boolean ownsBanner(int token)' in policy
print('PASS v10.0.6 Me, Home, Party lifecycle and banner display regressions (18 checks)')
