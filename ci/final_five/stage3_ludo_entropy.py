#!/usr/bin/env python3
"""Stage 3: replace modulo-of-millisecond Ludo die with replay-stable,
rejection-sampled SHA256 of Firestore server timestamp nanoseconds and match
revision. Not a server-authoritative RNG; keep paid rewards disabled.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
shutil.copyfile(Path(__file__).with_name('KingLudoDiceEntropy999.java'),root/'KingLudoDiceEntropy999.java')
f=root/'OnlineLudoActivity.java'
s=f.read_text()
needle='int die=(int)(ts.toDate().getTime()%6)+1,turn=i(o.get("turn"))'
repl='int die=KingLudoDiceEntropy999.dice(ts.getSeconds(),ts.getNanoseconds(),s(o.get("code")),i(o.get("revision"))),turn=i(o.get("turn"))'
assert s.count(needle)==1, "Ludo dice timestamp-millisecond anchor not found"
s=s.replace(needle,repl,1)
assert 'tx.set(target,o)' in s and 'FieldValue.serverTimestamp()' in s
assert 'KingLudoTurn962.canSkip(' in s
f.write_text(s)
print('PASS Stage3 Ludo updated: full server timestamp + match code + revision SHA-256, rejection sampling')
print('NOTE: Full game fairness still needs server-side verification and real two-phone acceptance')
