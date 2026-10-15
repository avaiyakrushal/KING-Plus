from pathlib import Path
import gzip,base64,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
assert 'versionCode 308;' in (root/'app/build.gradle').read_text(), 'Requires the exact signed v10.0.29 source'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
sha=hashlib.sha256(patch).hexdigest()
assert sha=='8388e1eb94a495963471e018657c025c81850ba9ad315d30e5ec00a61cdb441c', 'Patch integrity mismatch '+sha
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert 'versionCode 309;' in (root/'app/build.gradle').read_text()
assert '10.0.30-six-pages-session-integrity-test' in (root/'app/build.gradle').read_text()
print('PASS SHA256 verified full six-page patch v10.0.30')
