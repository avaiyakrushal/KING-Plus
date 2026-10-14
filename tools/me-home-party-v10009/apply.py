from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
conf=(root/'app/build.gradle').read_text()
assert 'versionCode 287;' in conf and '10.0.8-me-home-party-atomic-close-test' in conf,'Exact v10.0.8 input is required'
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=='e6ccccb6fe4a9d609f2a97095b768c2a39fc63daaa566579234c150036e4f0f3', 'Patch SHA mismatch'
subprocess.run(['git','apply','--check','-'],input=patch,cwd=root,check=True)
subprocess.run(['git','apply','-'],input=patch,cwd=root,check=True)
assert 'versionCode 288;' in (root/'app/build.gradle').read_text()
print('PASS KING v10.0.9 patch applied')
