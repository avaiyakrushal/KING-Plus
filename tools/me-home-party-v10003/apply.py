from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 281;" in gradle.read_text(), "Exact v10.0.2 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="e9a8dcf6bbaa0a8b214f632d7b3725d061841accffbcbf88a3a439bcd704f4ba", "Patch checksum mismatch"
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch_path)],cwd=root,check=True)
assert "versionCode 282;" in gradle.read_text()
print('PASS v10.0.3 live Me/Home visitors and current Party vote round patch')
