#!/usr/bin/env python3
from pathlib import Path
import re
root=Path('/tmp/king990-inspect/app/src/main')
j=root/'java/com/kingplus/social'
print("JAVA_FILES", len(list(j.glob('*.java'))))
for p in sorted(j.glob('*.java')):
    if re.search('login|gift|emoji|theme|effect|party|main|splash|auth|profile|vip|wallet|home',p.name,re.I):
        print("CLASS",p.name)
for filename in ['PartyActivity.java','MainActivity.java','LoginActivity.java','KingLoginActivity.java','KingGift971.java','KingEmoji970.java','KingGiftAnimation971.java']:
    p=j/filename
    if not p.exists():continue
    lines=p.read_text().splitlines()
    print("\n=====FILE",p.name,"LINES",len(lines))
    matches=[]
    pat=r'gift|emoji|theme|background|login|showGiftEffect|giftShop|effect|renderParty\(|bg\(|Animation|onCreate\('
    for i,line in enumerate(lines):
        if re.search(pat,line,re.I):
            if filename=='PartyActivity.java' and i>4900:continue
            matches.append(i)
    print("MATCHES",len(matches))
    selected=matches[:135] if filename=='PartyActivity.java' else matches[:115]
    for i in selected:print(f"L{i+1}:",lines[i][:260])
print("\n==== JAVA LAYOUT LOGIN ROUTE references ===")
for p in sorted(j.glob('*.java')):
    s=p.read_text()
    if 'Continue with Google' in s or 'Continue with Facebook' in s or 'Continue with Mobile' in s or 'Login with Google' in s:
        print("LOGIN_CLASS",p.name)
        lines=s.splitlines()
        for k,line in enumerate(lines):
            if re.search('Continue with|Login with|setBackground|onCreate|setContentView|login|signIn',line,re.I) and k<260:
                print(f"L{k+1}:",line[:240])
print("\n==== LOGIN XML resources ===")
res=root/'res'
for p in list(res.rglob('*.xml')):
    if re.search('login|signin|splash|party|gift|emoji|theme',p.name,re.I):
        print("RES",str(p.relative_to(root)))
print("INSPECTION_END")
