#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()
old='if(ReferenceEmojiView.parse(emoji)>=3){lp.width=stage.getWidth();lp.height=stage.getHeight();lp.leftMargin=0;lp.topMargin=0;}'
new='if(ReferenceEmojiView.parse(emoji)>=3 || live>=12){lp.width=stage.getWidth();lp.height=stage.getHeight();lp.leftMargin=0;lp.topMargin=0;}'
if old not in s: raise SystemExit('live full-screen marker changed')
p.write_text(s.replace(old,new,1))
print('special live emoji use full room stage')
