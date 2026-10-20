from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 311;' in gradle.read_text(), 'Requires exact signed v10.0.32 source'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
sha=hashlib.sha256(patch).hexdigest()
assert sha=='8c16b9d7d0ae93fa6c88c2a30601455defbf04a54bdc937c6905adb1725f2c2f',sha
patch_path=asset/'verified.patch';patch_path.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch_path)],cwd=root,check=True)
assert 'versionCode 312;' in gradle.read_text()
assert '10.0.33-six-pages-rooms-messages-bingo-test' in gradle.read_text()
print('PASS v10.0.33 verified six-page patch')
