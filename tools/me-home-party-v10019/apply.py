from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
asset=Path(__file__).resolve().parent
gradle=root/'app/build.gradle'
assert "versionCode 297;" in gradle.read_text(),"v10.0.18 signed source required"
b64=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(b64))
expected="1c45b1c5ef2e8412f36e01c394b9120ba14335cb995f8e7818f54ca6c72aa9b2"
assert hashlib.sha256(patch).hexdigest()==expected,"v10.0.19 patch checksum failed"
path=asset/'batch.patch'
path.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(path)],cwd=root,check=True)
assert "versionCode 298;" in gradle.read_text(),"Unexpected v10.0.19 target"
print('PASS one-batch Me/Home/Party v10.0.19 source patch')
