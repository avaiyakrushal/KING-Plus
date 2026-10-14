from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 275;" in gradle.read_text(), "Only the successful v9.9.96 base is supported"
assets=Path(__file__).resolve().parent
patch=gzip.decompress(base64.b64decode((assets/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="292bc396895f8af80aa45aaf257ed616a646fb6e0b706bddd523ada6dedd58f7","Patch checksum mismatch"
patchFile=assets/'changes.patch'
patchFile.write_bytes(patch)
subprocess.run(["patch","--batch","--fuzz=0","-p1","-i",str(patchFile)],cwd=root,check=True)
assert "versionCode 276;" in gradle.read_text()
print("PASS v9.9.97 shared transaction-based Me/Home/Party follow integration")
