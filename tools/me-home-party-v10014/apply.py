from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 292;" in version.read_text(),"Exact successful v10.0.13 source required"
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="14bee83e5c0f4010af5c0483f9a8550879cc98e63f8403d238f965622f4ba0b2","Patch integrity mismatch"
p=asset/'changes.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 293;" in version.read_text(),"Wrong target version"
print('PASS v10.0.14 source patch')
