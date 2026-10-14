from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
config=(root/'app/build.gradle').read_text()
assert "versionCode 286;" in config and "10.0.7-me-home-party-game-state-test" in config, "Exact v10.0.7 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="37448f8559eb69924b4047e9e321f01ee04d037867bcf6406112b3ac598c49db", "Patch SHA mismatch"
subprocess.run(['git','apply','--check','-'],input=patch,cwd=root,check=True)
subprocess.run(['git','apply','-'],input=patch,cwd=root,check=True)
assert "versionCode 287;" in (root/'app/build.gradle').read_text()
print('PASS KING v10.0.8 patch applied to exact v10.0.7 source')
