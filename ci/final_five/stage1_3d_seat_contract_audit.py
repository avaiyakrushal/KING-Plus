#!/usr/bin/env python3
from pathlib import Path
root=Path('/tmp/king-contract')
p=(root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text().splitlines()
v=(root/'app/src/main/java/com/kingplus/social/KingNative3DRoom.java').read_text().splitlines()
rules=(root/'firestore.rules').read_text().splitlines()
for label,ls,patterns in [
 ('PARTY',p,['collection("seats")','collection("seat','seatsListener','seatRef','getString("uid")','getString("userUid")','getString("userId")','getString("name")','seatUids.put','seatNames.put','put("uid"','put("seatNo"']),
 ('3D',v,['collection("seats")','doc.getLong("seatNo")','doc.getString("uid")','doc.getString("name")']),
 ('RULES',rules,['match /seats/{seatId}','allow create, update','request.resource.data.uid','request.resource.data.userUid'])
]:
 print('\nFILE',label)
 for pat in patterns:
  hits=[i for i,x in enumerate(ls) if pat in x]
  print('QUERY',pat,'COUNT',len(hits),'LINES',[i+1 for i in hits[:25]])
  for i in hits[:5]:
   for j in range(max(0,i-1),min(len(ls),i+4)):
    print(f'{j+1:05} {ls[j][:1400]}')
