from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 279;' in gradle.read_text(), 'Exact v10.0.0 source required'
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="46a30480c0b1ad91bea2bafc8b2001db1eecac22b124d7af30caa1661fb8b577", 'Patch checksum mismatch'
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch_path)],cwd=root,check=True)
assert 'versionCode 280;' in gradle.read_text()
print('PASS v10.0.1 Me Gallery all photos, lazy Home photos, safe Party voting')
