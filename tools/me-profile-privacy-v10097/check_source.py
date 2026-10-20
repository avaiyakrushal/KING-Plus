from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / 'app/build.gradle').read_text()
privacy = (root / 'app/src/main/java/com/kingplus/social/KingMePrivacy9984Activity.java').read_text()
visibility = (root / 'app/src/main/java/com/kingplus/social/KingPublicMeVisibility9984.java').read_text()
discover = (root / 'app/src/main/java/com/kingplus/social/DiscoverActivity.java').read_text()
social = (root / 'app/src/main/java/com/kingplus/social/SocialActivity.java').read_text()
party = (root / 'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 376' in gradle and "10.0.97-me-privacy-progress" in gradle
assert 'WriteBatch batch=db.batch()' in privacy and 'batch.commit()' in privacy
assert 'KingPublicMeVisibility9984.discoverable(p10022)' in discover
assert 'KingPublicMeVisibility9984.discoverable(p10028)' in discover
assert 'directoryScreen&&!KingPublicMeVisibility9984.discoverable(d)' in social
assert 'me_show_entrance_effects' in privacy and 'me_show_entrance_effects' in party
assert 'showEntrance10098?effect730:"None"' in party
assert 'return profile == null || !Boolean.FALSE.equals(profile.getBoolean("findMe"))' in visibility
assert 'older app versions' in privacy.lower() or 'older app versions' in (root / 'RELEASE-NOTES-v10.0.97.md').read_text().lower()
assert 'KING-Plus-v10.0.97-Me-Profile-TEST.apk' in workflow
assert 'KING-Plus-v10.0.97-Me-Profile-TEST-Source.zip' in workflow
print('PASS: Me find-me and entrance-effect preferences sync and are applied to supported profile surfaces')
