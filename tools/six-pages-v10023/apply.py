from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 301;' in gradle.read_text(), 'Exact v10.0.22 signed source needed'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=='610f82564778303f8be1ffe19ad2c9331b6e6c666ca01089a8aa79068ae6cee4','Invalid combined patch: '+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert 'versionCode 302;' in gradle.read_text()
print('PASS KING Plus v10.0.23 one combined Me/Home/Party/Game/Discover/Messages source patch')
