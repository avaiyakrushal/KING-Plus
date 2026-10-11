#!/usr/bin/env python3
from pathlib import Path
p=Path('/tmp/kinginspect/app/src/main/java/com/kingplus/social')
def out(name,ranges):
 f=p/name
 if not f.exists():print('MISSING_FILE',name);return
 lines=f.read_text(errors='replace').splitlines()
 print('FILE',name,'LINES',len(lines))
 for a,b in ranges:
  print('BLOCK',name,a,b)
  for i in range(a-1,min(len(lines),b)):
   print(f'{i+1:05}: {lines[i][:1100]}')
out('OnlineLudoActivity.java',[(135,174)])
out('RoomGameActivity.java',[(240,260),(330,394)])
out('KingRoomDice997.java',[(1,125)])
out('KingPremiumAccess981.java',[(1,180)])
out('KingGiftSound993.java',[(1,180)])
out('KingPublicProfileActivity.java',[(55,160)])
out('SocialActivity.java',[(195,260)])
