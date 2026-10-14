from pathlib import Path
import base64, gzip, hashlib, subprocess, sys
root=Path(sys.argv[1]).resolve()
base=root/'app/build.gradle'
assert "versionCode 290;" in base.read_text(), 'Expected signed KING Plus v10.0.11 source'
asset=Path(__file__).parent
patch=gzip.decompress(base64.b64decode((asset/'changes.patch.gz.b64').read_text().strip()))
assert hashlib.sha256(patch).hexdigest()=="50bbe9cae06d82627b2bc27dec58bdbed5653ada1df1a5a425ab5ff358a55a20", 'Patch hash mismatch'
patch_file=asset/'changes.patch'
patch_file.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch_file)],cwd=root,check=True)
assert "versionCode 291;" in base.read_text(), 'Version bump failed'
print('KING Plus v10.0.12 Me/Home/Party live safety patch applied')
