from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 294;" in version.read_text(),"Exact successful v10.0.15 source required"
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="136dba27f798be5ba3f4c7fa04db296ece4717826ec0a1af351ecab49c0b6038","Patch integrity mismatch"
p=asset/'changes.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 295;" in version.read_text(),"Wrong target version"
print('PASS KING v10.0.16 Me/Home/Party source patch')
