#!/usr/bin/env python3
from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
if not p.exists():
    raise SystemExit('PartyActivity.java not found')

s=p.read_text()
broken='shortChatPreview620(pendingReplyText620)+"\n"+text;'
fixed=r'shortChatPreview620(pendingReplyText620)+"\n"+text;'
if broken not in s:
    raise SystemExit('broken send newline marker not found')
s=s.replace(broken,fixed,1)
p.write_text(s)
print('v8.6.1 send newline compile fix applied')
