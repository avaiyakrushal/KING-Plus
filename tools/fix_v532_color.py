from pathlib import Path
p=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('0xffd8ffffff','0xd8ffffff')
if '0xffd8ffffff' in s:
    raise SystemExit('v5.3.2 color literal fix failed')
p.write_text(s,encoding='utf-8')
print('v5.3.2 color literal fixed')
