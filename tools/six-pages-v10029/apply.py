from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 307;' in gradle.read_text(),'Exact signed v10.0.28 source required'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=='20e8039daa9e0166f22d6da6d61152fdb559334ac65da4c316cd804b8def7472','Patch SHA256 invalid: '+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert 'versionCode 308;' in gradle.read_text()
assert '10.0.29-six-page-live-fixes-test' in gradle.read_text()
print('PASS v10.0.29 six-module regression patch against v10.0.28')
