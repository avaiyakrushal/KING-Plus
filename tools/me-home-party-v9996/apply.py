from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 274;" in gradle.read_text(), "Exact v9.9.95 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="a69293e88b5092491c4d5690a4701157cacd88ed51df3845b4890bca6bbc00c6", "Patch checksum mismatch"
patch_file=assets/'changes.patch'
patch_file.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch_file)],cwd=root,check=True)
assert "versionCode 275;" in gradle.read_text()
print("PASS v9.9.96 Gallery pagination, issued KING ID and Party music cleanup")
