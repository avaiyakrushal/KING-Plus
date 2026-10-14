from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
config=(root/'app/build.gradle').read_text()
assert "versionCode 284;" in config and "10.0.5-visitor-history-room-state-test" in config, "Exact v10.0.5 source is required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="cc7ca992fdd110f325ce0bfd3322ca787fad7197739ac226e926b7608289b091", "Patch SHA-256 mismatch"
subprocess.run(['git','apply','--check','-'],input=patch,cwd=root,check=True)
subprocess.run(['git','apply','-'],input=patch,cwd=root,check=True)
assert "versionCode 285;" in (root/'app/build.gradle').read_text()
print('PASS KING v10.0.6 patch applied to exact v10.0.5 source')
