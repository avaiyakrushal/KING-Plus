#!/usr/bin/env python3
from pathlib import Path
import re
p=Path('/tmp/v997/app/src/main/java/com/kingplus/social')
party=(p/'PartyActivity.java').read_text().splitlines()
patterns=['admin','cohost','Moderator','moderator','roomMenu','roomControlCenter','membersDialog','memberProfile','userProfile','seat','profileDialog','charisma','noble','board','Billboard','billboard','game','Game','level','Level','ban','kick','mute','roomRole','setRole','role']
for term in ['roomMenu(', 'roomControlCenter(', 'membersDialog(', 'seatClick', 'seatTap','roomBillboardDialog(', 'showMember','userProfile','charisma','noble','createAdmin','setCoHost','roomAdmin','assignRole']:
 indices=[i for i,x in enumerate(party) if term.lower() in x.lower()]
 print('MATCH',term, len(indices),[i+1 for i in indices[:30]])
 for i in indices[:3]:
  for n in range(max(0,i-1),min(len(party),i+7)):print(f'{n+1:05} {party[n][:340]}')
print('OTHER_CLASSES')
for f in sorted(p.glob('*.java')):
 name=f.name
 if any(k.lower() in name.lower() for k in ['level','charisma','noble','room','role','profile','game','family','admin','medal']):
  print(name)
for filename in ['RoomGameActivity.java','ProfileActivity.java','LevelSystem.java']:
 f=p/filename
 if f.exists():
  lines=f.read_text().splitlines()
  print('FILE',filename,'LINES',len(lines))
  for i,line in enumerate(lines):
   if any(k in line.lower() for k in ['oncreate(', 'void ','long ', 'charisma','noble','game','play','roomid','join']):
    print(f'{i+1:05} {line[:340]}')
print('FIRESTORE_ROLES')
s=Path('/tmp/v997/firestore.rules').read_text().splitlines()
for i,line in enumerate(s):
 if any(k in line for k in ['match /roles/', 'role ==', 'roomModerator(', 'roomCoHost(', 'charisma', 'noble','match /room_bans/']):
  print(f'{i+1:05} {line[:360]}')
