from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 306;' in gradle.read_text(),'Requires exact signed v10.0.27 source'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=='c70d4ca90fcfc9adc949c70c578ec6bc36f3d546a7f7a794b704cf0d83630e2e','Patch digest mismatch: '+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert 'versionCode 307;' in gradle.read_text()
assert '10.0.28-six-pages-functional-test' in gradle.read_text()
print('PASS KING Plus v10.0.28 full Me Home Party Game Discover Messages patch')
