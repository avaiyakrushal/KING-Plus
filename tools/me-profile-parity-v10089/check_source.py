from pathlib import Path

root = Path(__file__).resolve().parents[2]
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 374' in gradle and "10.0.95-owner-console" in gradle
assert 'profileField10043("bio","")' in main
assert 'profileField10043("tags","")' in main
assert 'profileInputV600(body,"Bio",profileField10043("bio",""))' in main
assert 'profileInputV600(body,"Labels",profileField10043("tags",""))' in main
assert 'e.setHint("Bio".equals(label)?' in main
assert 'Add interests such as music or games' in main
assert 'e.setHintTextColor(0xff999999)' in main
assert 'update10036.put("bio",bioValue)' in main and 'update10036.put("tags",tagValue)' in main
assert 'python3 tools/me-profile-parity-v10089/check_source.py' in workflow
assert "versionCode='374'" in workflow
assert 'KING-Plus-v10.0.95-Owner-Console-TEST.apk' in workflow
print('PASS: Me profile editor starts blank instead of saving example bio/interests as account data')
