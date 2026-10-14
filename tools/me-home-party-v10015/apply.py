from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 293;" in version.read_text(),"Exact signed v10.0.14 source required"
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="dcdb034491590e42e08bcb7c17cfefa226a5a79a1e7ea99f862a08dac380153f","Patch integrity mismatch"
p=asset/'changes.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 294;" in version.read_text(),"Wrong target version"
print("PASS v10.0.15 exact signed source patch")
