from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 289;" in version.read_text(), "Exact successful v10.0.10 source required"
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="bd0bd2cd1c6cbc32e863b11bc31941c324f9888ad247970b22149d1402093b33", "Integrity mismatch"
p=(asset/'changes.patch');p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
s=version.read_text()
assert s.count("versionCode 289;")==1
s=s.replace('versionCode 289;', 'versionCode 290;')
s=s.replace("versionName '10.0.10-me-home-family-guess-live-test'","versionName '10.0.11-party-moderation-test'")
version.write_text(s)
assert "versionCode 290;" in version.read_text()
print('PASS KING Plus v10.0.11 atomic moderation and co-host revocation')
