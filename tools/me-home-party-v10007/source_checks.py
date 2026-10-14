from pathlib import Path
import sys
root=Path(sys.argv[1]);app=root/'app/src/main/java/com/kingplus/social'
me=(app/'MainActivity.java').read_text()
home=(app/'CommunityHubActivity.java').read_text()
party=(app/'PartyActivity.java').read_text()
policy=(app/'KingMeHomePartyGuard10007.java').read_text()
assert '++visitorCountGeneration10007' in me
assert 'visitorCountCurrent(' in me
assert 'countTicket10007,visitorCountGeneration10007' in me
assert 'searchDraft10007=s.toString()' in home
assert 'q.setText(searchDraft10007)' in home
assert 'currentMomentPost10007(postingUid10007,postingGeneration10007,postingHost10007)' in home
assert 'momentCallbackCurrent(' in home
assert 'KingMeHomePartyGuard10007.gameIsActive(status,round)' in party
assert 'gameSnapshotReady=true;return;' in party
assert 'The live game listener is authoritative' in party
assert 'versionCode 286;' in (root/'app/build.gradle').read_text()
assert 'public static boolean visitorCountCurrent' in policy
assert 'public static boolean momentCallbackCurrent' in policy
assert 'public static boolean gameIsActive' in policy
print('PASS v10.0.7 source regression (14 checks)')
