from pathlib import Path
import re,sys
pkg=Path('/tmp/src/app/src/main/java/com/kingplus/social')
party=(pkg/'PartyActivity.java').read_text(errors='replace').splitlines()
patterns=['liveEmoji','LiveEmoji','SVGA','svga','sendEmoji','emojiEffect','activeReactions','showLive','emojiStage','playEmoji','onEmoji','KingEmoji','reaction','startHeartbeat900','stopHeartbeat900']
for pat in patterns:
 matches=[i for i,s in enumerate(party) if pat in s]
 print('=== PATTERN',pat,'COUNT',len(matches),'AT',','.join(str(i+1) for i in matches[:55]))
 for i in matches[:16]:
  lo=max(0,i-2);hi=min(len(party),i+4)
  print('---',i+1)
  for j in range(lo,hi):print(f'{j+1}: {party[j][:600]}')
print('=== RELEVANT ASSETS ===')
assets=Path('/tmp/src/app/src/main/assets')
if assets.exists():
 for p in sorted(assets.rglob('*')):
  if p.is_file() and any(k in p.name.lower() for k in ['emoji','svga','effect','reac']):
   print(str(p.relative_to(assets)),p.stat().st_size)
print('=== RELEVANT JAVA FILES ===')
for p in sorted(pkg.glob('*.java')):
 if any(k in p.name.lower() for k in ['emoji','reaction','roomstage','gift']):
  print(p.name,len(p.read_text(errors='replace').splitlines()))
print('=== FIRESTORE RULES EMOJI ===')
r=Path('/tmp/src/firestore.rules').read_text(errors='replace').splitlines()
for i,s in enumerate(r):
 if 'emoji' in s.lower() or 'reaction' in s.lower() or 'event' in s.lower() or 'members' in s.lower():
  print(f'{i+1}: {s[:460]}')
