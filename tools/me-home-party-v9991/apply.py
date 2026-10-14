from pathlib import Path
import subprocess, sys, tempfile
root = Path(sys.argv[1]).resolve()
assets = Path(__file__).resolve().parent
base = root/'app/build.gradle'
assert 'versionCode 269;' in base.read_text(), 'Exact v9.9.90 source required'
patch = (assets/'changes.patch').read_text()
# Archived patch ends with an incomplete PartyActivity hunk; apply complete hunks,
# then safely update the two remaining lines with exact-match assertions.
marker = '@@ -652,8 +653,9 @@'
assert patch.count(marker) == 1, 'Unexpected patch format'
with tempfile.NamedTemporaryFile(mode='w', suffix='.patch') as tmp:
    tmp.write(patch.split(marker)[0])
    tmp.flush()
    subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',tmp.name], cwd=root,check=True)
party = root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s = party.read_text()
old = 'toast("This Party room is unavailable");return;'
new = 'toast("This Party room is unavailable. You stayed in your current room.");return;'
assert s.count(old)==1, 'Room availability guard changed'
s=s.replace(old,new)
old = '                    openRoomDoc891(doc);\n                }).addOnFailureListener(e->{'
new = '                    if(roomId!=null)leaveRoom();\n                    openRoomDoc891(doc);\n                }).addOnFailureListener(e->{'
assert s.count(old)==1, 'Room switch callback changed'
s=s.replace(old,new)
party.write_text(s)
assert 'versionCode 270;' in base.read_text()
print('PASS: applied v9.9.91 source with safe Party invite patch')
