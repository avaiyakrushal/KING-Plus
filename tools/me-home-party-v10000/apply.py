from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 278;" in gradle.read_text(), "Exact v9.9.99 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="0b100c2854274d3789b88eae639d6669d03d115d52a68bd1b88d1b535d048ca9", "Patch checksum mismatch"
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch_path)],cwd=root,check=True)
assert "versionCode 279;" in gradle.read_text()
print("PASS v10.0.0 Me/Home/Party live Moments, photo 10MB and Like synchronization patch")
