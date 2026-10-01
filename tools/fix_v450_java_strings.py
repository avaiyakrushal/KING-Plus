from pathlib import Path

p = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = p.read_text(encoding='utf-8')

# prepare_party_room_v450.py intentionally generates Java from a Python template.
# Ensure the two Room Battle message separators remain Java escape sequences,
# not literal newlines inside Java string literals.
s = s.replace('"  vs  Guest  "+b+"\n\n"+', '"  vs  Guest  "+b+"\\n\\n"+')
s = s.replace('"  vs  Room  "+compactNumber(guestScore)+"\n\n"+', '"  vs  Room  "+compactNumber(guestScore)+"\\n\\n"+')

p.write_text(s, encoding='utf-8')
print('Fixed v4.5.0 Java string escapes')
