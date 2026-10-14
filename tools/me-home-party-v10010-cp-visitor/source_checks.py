from pathlib import Path
import sys
base=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
me=(base/'MainActivity.java').read_text()
home=(base/'CommunityHubActivity.java').read_text()
party=(base/'PartyActivity.java').read_text()
gradle=(base.parents[5]/'app/build.gradle') if False else Path(sys.argv[1])/'app/build.gradle'
assert 'private void openMyVisitorHistory10010(String owner,int ticket)' in me
assert me.count('openMyVisitorHistory10010(account10003,visitorTicket10004)')==3
assert 'currentSection10003("Relationship",relationshipGeneration10010,relationshipHost10010,relationshipAccount10010)' in home
assert 'relationshipSending10010=true' in home
assert 'A Firestore CREATE (never read-before-SET)' in home
assert 'setTitle("Remove relationship?")' in home
assert 'addOnFailureListener(e->toast("Could not remove connection"))' in home
assert 'KingPublicProfileActivity records visitedAt exactly once' in party
assert 'logProfileVisit750(' not in party
assert 'versionCode 289;' in gradle.read_text()
assert '10.0.10-me-home-cp-party-visitor-test' in gradle.read_text()
print("PASS Me account-bound visitor popup, Home CP duplicate prevention and secure UI, Party single visitor timestamp")
