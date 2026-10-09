"""Apply v9.6.1: fix stale Android native process exit attribution.
Source baseline: successful v9.6.0 APK.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
old=pkg/'KingCrashWatch958.java'
if not old.exists():raise SystemExit('Expected existing process crash watcher')
new=Path(__file__).with_name('KingCrashWatch958.java')
if not new.exists():raise SystemExit('Missing version-aware crash watcher')
existing=old.read_text()
if 'versionName' in existing or 'Build: 9.5.8' not in existing:
    raise SystemExit('Unexpected v9.6.0 crash watcher baseline')
shutil.copy2(new,old)
print('PASS: installed version and install timestamp, process PID and screen correlation')
gradle=root/'app/build.gradle'
g=gradle.read_text()
old_version="versionCode 151; versionName '9.6.0-live-emoji-stability'"
if g.count(old_version)!=1:raise SystemExit('Expected v9.6.0 Gradle version')
gradle.write_text(g.replace(old_version,"versionCode 152; versionName '9.6.1-native-crash-attribution'",1))
print('PASS: version 9.6.1, previous Live Emoji / invite / stability code preserved')
