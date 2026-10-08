#!/usr/bin/env python3
"""KING Plus source integrity gate: no phantom functionality or broken routes."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])/'app/src/main'
src=root/'java/com/kingplus/social'
manifest=(root/'AndroidManifest.xml').read_text(errors='replace')
files={p.stem:p for p in src.glob('*.java')}
declared=set(re.findall(r'<activity\b[^>]*android:name="(?:com\.kingplus\.social\.)?\.?(\w+)"',manifest))
fail=[]
def check(label,ok):
    print(('PASS' if ok else 'FAIL')+' '+label)
    if not ok: fail.append(label)
for name in ('MainActivity','PartyActivity','RoomGameActivity','KingLudoLobbyActivity','DiscoverActivity','KingVipVisualActivity','KingPublicProfileActivity'):
    check('Activity source and manifest: '+name,name in files and name in declared)
party=files['PartyActivity'].read_text()
games=files['RoomGameActivity'].read_text()
main=files['MainActivity'].read_text()
for label,source,needle in [
 ('mic permission',party,'Manifest.permission.RECORD_AUDIO'),
 ('room reconnect',party,'Back online • reconnecting Party room'),
 ('gift budget',party,'KingEffectBudget.allow(this,"gift")'),
 ('emoji budget',party,'KingEffectBudget.allow(this,"emoji")'),
 ('KTV retry',party,'KingUiState.error(this,"KTV'),
 ('PK retry',party,'KingUiState.error(this,"PK'),
 ('game history retry',games,'KingUiState.error(this,"Game history'),
 ('Ludo route',main,'KingLudoLobbyActivity.class'),
 ('profile routing',main,'KingPublicProfileActivity.class'),
]:
    check(label,needle in source)
for name,p in files.items():
    t=p.read_text(errors='replace')
    for match in re.finditer(r'new Intent\(this,\s*(\w+)\.class\)',t):
        target=match.group(1)
        if target not in declared and target in files:
            fail.append(f'{name}: activity not in manifest: {target}')
print(f'Checks failed: {len(fail)}')
if fail: raise SystemExit(1)
