from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 305;" in gradle.read_text(), "Require exact signed v10.0.26 source"
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(2))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="26e0ca8c7357d3f1e1d1e90e23148f184b5b1b6cdf68b3d60a24450ed38bf198","Patch mismatch "+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 306;" in gradle.read_text()
assert "10.0.27-party-presence-fast-join-test" in gradle.read_text()
print('PASS v10.0.27 Party Room faster join and ghost presence fallback patch')
