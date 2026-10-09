from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
ranges={'ChatActivity.java':[(345,379)],'PartyActivity.java':[(1395,1415),(1465,1495),(1586,1630),(2138,2180),(2598,2625),(4355,4375)],'CommunityHubActivity.java':[(113,170),(215,267)],'KingVipVisualActivity.java':[(7,38)]}
for f,rr in ranges.items():
 s=(p/f).read_text().splitlines()
 print('=== FILE',f,'LINES',len(s))
 for a,b in rr:
  print('=== RANGE',a,b)
  for i in range(a-1,min(b,len(s))):
   print(f'{i+1}: {s[i][:1900]}')
