#!/usr/bin/env python3
"""Read-only v9.4.2 source audit for the next crash-fix stage."""
from pathlib import Path
import re,sys,json
root=Path(sys.argv[1])/'app/src/main'
java=root/'java/com/kingplus/social'
findings=[]
for p in sorted(java.glob('*.java')):
    lines=p.read_text(errors='replace').splitlines()
    for n,line in enumerate(lines,1):
        if 'getCurrentUser().getUid()' in line:
            before='\n'.join(lines[max(0,n-7):n])
            guarded=bool(re.search(r'getCurrentUser\(\)\s*!=\s*null|getCurrentUser\(\)\s*==\s*null|FirebaseUser\s+\w+\s*=',before))
            findings.append({'file':p.name,'line':n,'guard_nearby':guarded,'source':line.strip()[:240]})
out={'firebase_user_dereferences':findings,'unguarded_candidates':sum(not f['guard_nearby'] for f in findings)}
Path('v943-crash-audit.json').write_text(json.dumps(out,indent=2))
print(json.dumps(out,indent=2))
