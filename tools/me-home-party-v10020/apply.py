from pathlib import Path
import hashlib,base64,gzip,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 298;' in gradle.read_text(), 'Need signed v10.0.19 source'
asset=Path(__file__).resolve().parent
encoded=''.join((asset/f'patch-chunk{i}.txt').read_text().strip() for i in range(6))
patch=gzip.decompress(base64.b64decode(encoded))
checksum=hashlib.sha256(patch).hexdigest()
assert checksum=='50648fb4023a8c5a8a37761299808bd7b39530f899c08b18b0952c93e5a0120b', 'Patch checksum mismatch: '+checksum
p=asset/'verified-changes.patch'
p.write_bytes(patch)
subprocess.run(['patch','-p1','--batch','--fuzz=0','-i',str(p)],cwd=root,check=True)
assert 'versionCode 299;' in gradle.read_text(), 'Unexpected target'
assert 'muteExistingRoomSeats10020' in (root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
print('PASS KING v10.0.20 full combined source patch verified')
