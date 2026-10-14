from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 288;" in gradle.read_text(), "Exact successful v10.0.9 source is required"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="7ce423d716f28a08bc08db11bd1edb050107f45b5f14fc8744afe6e5131cf8fd","Patch checksum mismatch"
file=assets/'changes.patch';file.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(file)],cwd=root,check=True)
assert "versionCode 289;" in gradle.read_text()
assert "10.0.10-me-home-cp-party-visitor-test" in gradle.read_text()
print("PASS v10.0.10 Me visitors, Home Relationship / CP and Party visitor fixes")
