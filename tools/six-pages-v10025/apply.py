from pathlib import Path
import base64,gzip,hashlib,subprocess,sys
root=Path(sys.argv[1]).resolve()
asset=Path(__file__).resolve().parent
gradle=root/'app/build.gradle'
assert "versionCode 303;" in gradle.read_text(), "Require exact signed v10.0.24 source"
encoded=''.join((asset/f'chunk{i}.txt').read_text().strip() for i in range(4))
patch=gzip.decompress(base64.b64decode(encoded))
digest=hashlib.sha256(patch).hexdigest()
assert digest=="a426ffc55a17f3e00f1856193fd93883882df03f2a37863a083edbf2f6de1845", "Wrong patch SHA256 "+digest
p=asset/'verified.patch';p.write_bytes(patch)
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(p)],cwd=root,check=True)
# Keep Firestore's ORIGINAL server query cursor, never the last row after
# client-side Hot/New sorting; otherwise Next 60 skips or repeats profiles.
discover=root/'app/src/main/java/com/kingplus/social/DiscoverActivity.java'
s=discover.read_text()
def replace_exact(old,new):
    global s
    assert s.count(old)==1, "Unexpected public profile paging source: "+old[:50]
    s=s.replace(old,new)
replace_exact('private void loadProfileGrid940(){',
    'private DocumentSnapshot pageLastServerDoc10025;\\n    private void loadProfileGrid940(){'.replace('\\n','\n'))
replace_exact('List<DocumentSnapshot> docs10022=new ArrayList<>(snap.getDocuments());',
    'List<DocumentSnapshot> docs10022=new ArrayList<>(snap.getDocuments());\\n            pageLastServerDoc10025=snap.isEmpty()?null:snap.getDocuments().get(snap.size()-1);'.replace('\\n','\n'))
replace_exact('final DocumentSnapshot last10025=docs10025.get(docs10025.size()-1);',
    'final DocumentSnapshot last10025=pageLastServerDoc10025;\\n        if(last10025==null)return;'.replace('\\n','\n'))
replace_exact('List<DocumentSnapshot> nextDocs10025=new ArrayList<>(snap10025.getDocuments());',
    'List<DocumentSnapshot> nextDocs10025=new ArrayList<>(snap10025.getDocuments());\\n                    pageLastServerDoc10025=snap10025.getDocuments().get(snap10025.size()-1);'.replace('\\n','\n'))
discover.write_text(s)
assert "versionCode 304;" in gradle.read_text()
assert "10.0.25-six-page-combined-test" in gradle.read_text()
print("PASS v10.0.25 ONE combined six-page patch, verified SHA256")
