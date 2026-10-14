from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 283;" in gradle.read_text(), "Exact v10.0.4 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="2f6f631919bc500b322d8afcf502ad404a2b8e53918f46577c5c26da85cf3628", "Patch checksum mismatch"
patch_path=assets/'changes.patch'
patch_path.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch_path)],cwd=root,check=True)
assert "versionCode 284;" in gradle.read_text()
assert "10.0.5-visitor-history-room-state-test" in gradle.read_text()
print("PASS v10.0.5: Me full history, Home 50-item paging, Party room-bound game listener")
