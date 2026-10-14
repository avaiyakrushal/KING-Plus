from pathlib import Path
import sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
me=(pkg/'MainActivity.java').read_text()
home=(pkg/'CommunityHubActivity.java').read_text()
party=(pkg/'PartyActivity.java').read_text()
assert '++profileFollowGeneration10008' in me
assert 'followTicket10008!=profileFollowGeneration10008' in me
assert home.count('currentSection10003("Family",ticket10008,familyHost10008,familyAccount10008)')==4
assert 'final String familyAccount10008=me.getUid()' in home
assert 'body==familyBody956 && !familyStopped956 && "Family".equals(selected)' in home
assert 'db.runTransaction(tx->{' in party
assert 'KingMeHomePartyGuard10008.mayCloseCurrentGame' in party
assert 'tx.update(gameRef10008,update10008)' in party
assert 'expectedRound10008=activeGameRound' in party
assert 'versionCode 287;' in (root/'app/build.gradle').read_text()
print('PASS KING 10.0.8 source checks: 10 rules')
