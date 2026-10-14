from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
base=root/'app/build.gradle'
assert "versionCode 272;" in base.read_text(), "v9.9.93 source is required"
bundle=Path(__file__).resolve().with_name("changes.patch.gz.b64")
data=gzip.decompress(base64.b64decode(bundle.read_text().strip()))
assert hashlib.sha256(data).hexdigest()=="cfa31d4eca99d1adfdbad3d059cd07f35d50b4ab22a52fb65e210f879b97b552", "Patch checksum mismatch"
patch=Path(__file__).resolve().with_name("changes.patch")
patch.write_bytes(data)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patch)],cwd=root,check=True)
base.write_text(base.read_text().replace("versionCode 272; versionName '9.9.93-me-gallery-party-invite-test'","versionCode 273; versionName '9.9.94-me-home-party-search-test'"))
assert "versionCode 273;" in base.read_text()
print("PASS v9.9.94 Home search, Me Gallery and saved Party Room fixes")
