from pathlib import Path
import sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
targets={
 'SocialActivity.java':['onCreate(','load','renderProfiles','addOnFailureListener','private void'],
 'KingPublicProfileActivity.java':['onCreate(','load','toggleFollow','followers','addOnFailureListener','private void'],
 'CommunityHubActivity.java':['onCreate(','Family','load','addOnFailureListener','private void'],
 'KingVipVisualActivity.java':['onCreate(','private void','VIP','load'],
 'MainActivity.java':['private void profileV600','addProfileRecommendations940','refreshServerWallet','profileVisitors','addOnFailureListener'],
}
for fn,needles in targets.items():
 p=root/fn
 print('\n########',fn,'########')
 if not p.exists(): print('MISSING');continue
 lines=p.read_text(errors='replace').splitlines()
 seen=set()
 for needle in needles:
  hits=[i for i,x in enumerate(lines) if needle in x]
  print('\n###',needle,'hits',hits[:8])
  for i in hits[:3]:
   key=(needle,i)
   if key in seen: continue
   seen.add(key)
   lo=max(0,i-6);hi=min(len(lines),i+45)
   for n in range(lo,hi): print(f'{n+1:05d}: {lines[n]}')
