from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
config=(root/'app/build.gradle').read_text()
assert "versionCode 285;" in config and "10.0.6-me-home-party-lifecycle-test" in config, "Exact v10.0.6 source is required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="2e70f2f141bee0f217b92a0351d24f45974245861fa44262c181f4630c9c6aa6", "Patch SHA-256 mismatch"
subprocess.run(['git','apply','--check','-'],input=patch,cwd=root,check=True)
subprocess.run(['git','apply','-'],input=patch,cwd=root,check=True)
assert "versionCode 286;" in (root/'app/build.gradle').read_text()
print('PASS v10.0.7 patch applied to exact v10.0.6 source')
