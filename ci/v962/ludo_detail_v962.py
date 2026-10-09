from pathlib import Path
p=Path('/tmp/src/app/src/main/java/com/kingplus/social/OnlineLudoActivity.java')
s=p.read_text();lines=s.splitlines()
for line in [83,84,125,126,127,128,129,130,131,133,135,136,137,138,139,140,141,142,143,144,145,146,147,148]:
 t=lines[line-1]
 print('=== ONLINE_LUDO LINE',line,'LENGTH',len(t),'===')
 for j in range(0,min(len(t),15000),400):
  print(t[j:j+400])
rules=(Path('/tmp/src/firestore.rules').read_text())
i=rules.index('match /ludo_matches')
print('=== FIRESTORE LUDO RULES ===')
print(rules[i:i+1300])
