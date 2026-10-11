#!/usr/bin/env python3
from pathlib import Path
p=Path('/tmp/king996/app/src/main/java/com/kingplus/social')
for name in ['MainActivity.java','PartyActivity.java','SocialActivity.java','ChatActivity.java']:
 t=(p/name).read_text().splitlines()
 print('FILE',name,'SIZE',len(t))
 pats={'MainActivity.java':['onCreate(', 'onResume(', 'googleLogin(', 'login(', 'firebase', 'FirebaseAuth', 'startActivity(new Intent(this,SocialActivity.class))','showHome','homePage','private void home', 'doLogin'],
 'PartyActivity.java':['createRoom', 'roomData.put(', 'roomData=', 'roomName =', 'memberSeen=true','last_live_room_id_992','private void social', 'private void sendGiftQuantity('],
 'SocialActivity.java':['private void toggleFollow(', 'private void openProfile(', 'private void publishOwnProfile(', 'private boolean currentSearch970(', 'private void runSearch()', 'private void shareKingId(', 'private void openChat(', 'private String publicId('],
 'ChatActivity.java':['private void shareRoom(', 'private void openChat(', 'private void sendText(', 'onCreate(', 'last_live_room_id_992'] }[name]
 for i,l in enumerate(t):
  if any(v in l for v in pats):
   if name=='MainActivity.java' and ('firebase' in l.lower() or 'FirebaseAuth' in l) and len(l)>250:continue
   print(f'{i+1:05} {l[:400]}')
   if name=='SocialActivity.java' and ('private void toggleFollow(' in l or 'private void publishOwnProfile(' in l or 'private boolean currentSearch970(' in l):
    for j in range(i+1,min(len(t),i+45)):print(f'{j+1:05} {t[j][:410]}')
print("FINISHED_AUDIT_DETAIL")
