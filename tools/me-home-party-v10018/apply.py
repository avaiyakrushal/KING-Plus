from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root = Path(sys.argv[1]).resolve()
asset = Path(__file__).resolve().parent
version = root / 'app/build.gradle'
assert "versionCode 296;" in version.read_text(), "Expected exact successful v10.0.17 source"
b64 = ''.join((asset / ('batch-chunk' + str(i) + '.txt')).read_text().strip() for i in range(4))
patch = gzip.decompress(base64.b64decode(b64))
digest = hashlib.sha256(patch).hexdigest()
assert digest == "c4c85dd5393ebb9ff0c0f00bc36a5874fd2f773a1bbd86e4f84dd627e330969d", "Unexpected or damaged batch patch: "+digest
patch_file = asset / 'verified-batch.patch'
patch_file.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch_file)],cwd=root,check=True)
assert "versionCode 297;" in version.read_text(), "Wrong batch version"
assert "10.0.18-me-home-party-batch-test" in version.read_text()
print('PASS KING Plus v10.0.18 exact source & complete 3-page batch patch')
