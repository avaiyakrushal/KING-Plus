from pathlib import Path
p=Path('/tmp/king996/app/src/main/java/com/kingplus/social')
for name,blocks in [('MainActivity.java',[(101,165),(735,798),(2018,2045)]),('PartyActivity.java',[(1484,1525),(695,735),(2740,2788)]),('SocialActivity.java',[(540,585)]),('ChatActivity.java',[(260,281)])]:
 ls=(p/name).read_text().splitlines()
 for lo,hi in blocks:
  print('BLOCK',name,lo,hi)
  for i in range(lo-1,min(hi,len(ls))):print(f'{i+1:05} {ls[i][:650]}')
