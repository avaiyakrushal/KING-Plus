from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
version=root/'app/build.gradle'
assert "versionCode 295;" in version.read_text(), "Exact successful v10.0.16 source required"
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="d3232f49586a54ebc8e8f8823d2274c1ab069b6f228fd27cd5e491ecb4d5de38", "Patch integrity mismatch"
p=asset/'changes.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 296;" in version.read_text(), "Wrong target version"
print('PASS v10.0.17 Me/Home/Party exact source patch')
