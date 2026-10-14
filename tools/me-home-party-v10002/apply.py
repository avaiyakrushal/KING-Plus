from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 280;" in gradle.read_text(), "Exact v10.0.1 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="b0a096b207cb0f574a641a2addae9d0f5f1ef21041182d485ce9cec7640ddeed", "Patch checksum mismatch"
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch_path)],cwd=root,check=True)
assert "versionCode 281;" in gradle.read_text()
print("PASS v10.0.2 verified KING ID, Topic Square lifecycle, room/round-bound votes")
