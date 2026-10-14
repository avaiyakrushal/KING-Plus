from pathlib import Path
import hashlib,base64,gzip,subprocess,sys
root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert 'versionCode 299;' in gradle.read_text(), 'Exact v10.0.20 signed source required'
here=Path(__file__).parent
encoded=''.join((here/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
assert hashlib.sha256(patch).hexdigest()=='40bc7b7be293445a90f3c2039b8a989e04e9d17bf175ec89ecd0c2cb3e014faa', 'Source patch hash mismatch'
patchpath=here/'verified.patch'
patchpath.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patchpath)],cwd=root,check=True)
game=root/'app/src/main/java/com/kingplus/social/MainActivity.java'
body=game.read_text()
assert 'LinearLayout.LayoutParams(0,dp(168),1)' in body
game.write_text(body.replace('LinearLayout.LayoutParams(0,dp(168),1)','LinearLayout.LayoutParams(0,dp(190),1)',1))
assert 'versionCode 300;' in gradle.read_text()
print('PASS v10.0.21 premium Me/Game/Discover/Messages + unique logo + game card fit')
