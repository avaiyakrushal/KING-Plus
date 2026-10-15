from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 311;" in gradle.read_text(),"Require exact signed KING v10.0.32 source"
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="005a26ac1c38f290feaca09bdd5bcea88b42cfbd1e47a2a3ca7b7f029a32d5f3",digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 312;" in gradle.read_text()
assert "10.0.33-six-pages-live-actions-test" in gradle.read_text()
print('PASS v10.0.33 Me Home Party Game Discover Messages live actions patch')
