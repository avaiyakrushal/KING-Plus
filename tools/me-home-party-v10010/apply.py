from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
src=(root/'app/build.gradle').read_text()
assert 'versionCode 288;' in src and '10.0.9-me-home-party-atomic-start-test' in src, 'Exact v10.0.9 source required'
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.n0.gz.b64').read_text().strip()))
expected='cb897f672ae6bda8c792f1c09d63de0e1bc0cd31761b46c888a3e8578e83271e'
assert hashlib.sha256(patch).hexdigest()==expected, 'Patch hash mismatch'
subprocess.run(['git','apply','--unidiff-zero','--check','-'],input=patch,cwd=root,check=True)
subprocess.run(['git','apply','--unidiff-zero','-'],input=patch,cwd=root,check=True)
assert 'versionCode 289;' in (root/'app/build.gradle').read_text()
print('PASS v10.0.10 changes applied from exact v10.0.9 source')
