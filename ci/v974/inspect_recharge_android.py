#!/usr/bin/env python3
"""Inspect latest successful v9.7.3 KING Plus Android source for Diamond Recharge and billing hooks."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
java=list(p.glob('*.java'))
print('CLASS_NAMES', ' '.join(x.name for x in java))
print('VERSION',*[x for x in (root/'app/build.gradle').read_text().splitlines() if 'versionCode' in x])
print('DEPENDENCIES', (root/'app/build.gradle').read_text().split('dependencies {')[-1][:2500])
man=root/'app/src/main/AndroidManifest.xml'
if man.exists():print('MANIFEST_ACTIVITY_LINES', '\n'.join(x for x in man.read_text().splitlines() if 'activity' in x.lower())[:7500])
files=[p/'CloudBackend.java',p/'KingVipVisualActivity.java',p/'MainActivity.java',p/'PartyActivity.java',p/'ChatActivity.java']
words=['Recharge','TopUp','Wallet','diamond','coin','VIP','king_coins_','verifyPlayPurchase','BillingClient','CloudBackend','wallet','vipLevel','giftShopPanel','openProfile','showMe','store']
for f in files:
 if not f.exists():print('NOT_FOUND',str(f));continue
 s=f.read_text(errors='replace');lines=s.splitlines()
 print('=== FILE',f.name,'chars',len(s),'lines',len(lines))
 if f.name in ['CloudBackend.java','KingVipVisualActivity.java']:
  for i,x in enumerate(lines):print(f'{i+1}: {x[:1100]}')
  continue
 found=[]
 for i,x in enumerate(lines):
  if any(w.lower() in x.lower() for w in words):
   found.append(i)
 print('MATCHING',len(found))
 for i in found[:85]:
  print(f'{i+1}: {lines[i][:1100]}')
for f in java:
 if f not in files and any(x in f.stem.lower() for x in ['wallet','recharge','bill','coin','diamond']):
  print('RELATED CLASS',f.name)
  for i,row in enumerate(f.read_text(errors='replace').splitlines()[:130]):
   print(f'{i+1}: {row[:1000]}')
