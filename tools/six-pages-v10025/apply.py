from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
asset=Path(__file__).resolve().parent
gradle=root/'app/build.gradle'
assert "versionCode 303;" in gradle.read_text(), "Require exact signed v10.0.24 source"
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="a426ffc55a17f3e00f1856193fd93883882df03f2a37863a083edbf2f6de1845", "Wrong patch SHA256 "+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
assert "versionCode 304;" in gradle.read_text()
assert "10.0.25-six-page-combined-test" in gradle.read_text()
print("PASS v10.0.25 ONE combined six-page patch, verified SHA256")
