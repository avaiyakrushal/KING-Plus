from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 304;" in gradle.read_text(),"Require exact signed v10.0.25 source"
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(3))
patch=gzip.decompress(base64.b64decode(encoded))
assert hashlib.sha256(patch).hexdigest()=="e45655fd9a13b5fffcb65e508b262bdba36100ceb10fcd0a70ee00f4502d5085"
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 305;" in gradle.read_text()
print('PASS v10.0.26 Party Room video-reported issue patch')
