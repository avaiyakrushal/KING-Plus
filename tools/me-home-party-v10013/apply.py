from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 291;" in version.read_text(),"Exact successful v10.0.12 source required"
a=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((a/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="7c2cee5e8e73c96f62c4900587f3b875fc2d90d7f2842780c55efc49e7528eba","Patch integrity mismatch"
p=a/'changes.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 292;" in version.read_text(),"Wrong target version"
print('PASS v10.0.13 source patch')
