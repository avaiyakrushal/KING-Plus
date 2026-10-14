from pathlib import Path
import subprocess, sys, hashlib
root=Path(sys.argv[1]).resolve()
p=root/'app/build.gradle'
assert 'versionCode 271;' in p.read_text(), 'Exact tested v9.9.92 source required'
patch=Path(__file__).resolve().with_name('changes.patch')
assert hashlib.sha256(patch.read_bytes()).hexdigest() == '039d200ea1d1efec621ca1c008c938563a95751a1c15a94813bbf1d801e7eeec', 'Patch checksum mismatch'
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch)],cwd=root,check=True)
assert 'versionCode 272;' in p.read_text()
print('PASS v9.9.93 Me live Gallery, Home search, and Party invite patch')
