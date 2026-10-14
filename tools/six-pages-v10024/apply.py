from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
asset=Path(__file__).resolve().parent
gradle=root/'app/build.gradle'
assert "versionCode 302;" in gradle.read_text(), "Expected exact signed v10.0.23 source"
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(5))
patch=gzip.decompress(base64.b64decode(encoded))
sha=hashlib.sha256(patch).hexdigest()
assert sha=="050f388d2e620dbc109863288d95ab70f2951be5e7074e518175e993135d1366", "Patch mismatch "+sha
patchpath=asset/'verified.patch'
patchpath.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patchpath)],cwd=root,check=True)
assert "versionCode 303;" in gradle.read_text()
assert "10.0.24-six-page-live-batch-test" in gradle.read_text()
print("PASS exact complete six-page source patch v10.0.24")
