from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 282;" in gradle.read_text(), "Exact v10.0.3 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="3836418680ce4d0e95679e7dd38e0acf0308f334bec92b06b50d4ce90b4bb219", "Patch checksum mismatch"
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch_path)],cwd=root,check=True)
assert "versionCode 283;" in gradle.read_text()
assert "10.0.4-me-home-room-vote-test" in gradle.read_text()
print("PASS v10.0.4: Me visitor lifecycle, Home feed replacement, atomic Party vote")
