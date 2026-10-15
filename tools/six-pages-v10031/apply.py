from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
assert 'versionCode 309;' in (root/'app/build.gradle').read_text(), 'Exact signed v10.0.30 source required'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
sha=hashlib.sha256(patch).hexdigest()
assert sha=='d8483a70b4414b1d0824f9fb83ecb081f7feb570b3ece7b74d6d8349e1349c3a','Bad patch '+sha
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert 'versionCode 310;' in (root/'app/build.gradle').read_text()
assert '10.0.31-six-pages-live-account-reliability-test' in (root/'app/build.gradle').read_text()
print('PASS SHA256 exact 7-file v10.0.31 six-page Android source patch')
