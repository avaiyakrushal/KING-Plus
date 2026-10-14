from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 300;" in version.read_text(),"Exact successful v10.0.21 source required"
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="ef5d706608ab744ee2a43a3c37592a26b2efc695567e47b9bb6b5186019e00b0","Integrity mismatch: "+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 301;" in version.read_text(),"Wrong target"
assert "10.0.22-six-page-live-fixes-test" in version.read_text()
print('PASS KING Plus v10.0.22 one combined six-page source patch')
