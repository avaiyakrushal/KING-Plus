from pathlib import Path
root = Path(__file__).resolve().parents[2]
source = root / 'app/src/main/java/com/kingplus/social'
helper = (source / 'KingSafeImageUri10070.java').read_text()
app_sources = '\n'.join(p.read_text(errors='replace') for p in source.glob('*.java'))
gradle = (root / 'app/build.gradle').read_text()
assert 'versionCode 356' in gradle
assert "10.0.77-party-rejoin-choice" in gradle
assert 'ContentResolver resolver' in helper
assert 'openInputStream(uri)' in helper
assert 'catch (Exception | OutOfMemoryError ignored)' in helper
assert 'setImageDrawable(null)' in helper
assert 'KingSafeImageUri10070.show(' in app_sources
assert '.setImageURI(' not in app_sources, 'Deferred content URI decode still present'
print('PASS Android 16 document URI images are decoded eagerly and failures are contained')
print('PASS all app Java image URI call sites use the safe decoder')
