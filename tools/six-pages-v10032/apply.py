from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 310;" in gradle.read_text(),"Require exact signed v10.0.31 source"
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="4591eb780ddcd0e2ef1ccee3f35ec0dcf3fc5e6853deac199a9aa2c5d55494b5",digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 311;" in gradle.read_text()
assert "10.0.32-six-pages-batched-stale-state-test" in gradle.read_text()
print('PASS KING v10.0.32 Me Home Party Game Discover Messages verified patch')
