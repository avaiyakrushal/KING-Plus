from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 273;" in gradle.read_text(), "Exact v9.9.94 source required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="812604d9ce9bdec2ae69c9e91484f814376ad734d18969c33425a57ef64caec8", "Patch checksum mismatch"
patchFile=assets/'changes.patch'
patchFile.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patchFile)],cwd=root,check=True)
assert "versionCode 274;" in gradle.read_text()
print("PASS v9.9.95 Me Gallery, Home Search, Party Music improvements")
