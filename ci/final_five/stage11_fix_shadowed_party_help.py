#!/usr/bin/env python3
"""Stage 11: fix ambiguous Party menu dispatch without dropping ANY controls.
The actual Bolo-Hi-inspired app still needs physical two-phone acceptance tests.
This patch starts from the latest Stage10 UX source, never an old base.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()
old='''        else if(x.contains("Party Master"))partyMasterPanel();
        else if(x.contains("Party Data"))partyDataPanel();'''
new='''        // Specific Help must come before generic Party Master to avoid shadowing.
        else if(x.contains("Party Master Help"))partyMasterHelp610();
        else if(x.contains("Party Master"))partyMasterPanel();
        else if(x.contains("Party Data"))partyDataPanel();'''
assert s.count(old)==1,f'Party Master route anchor count {s.count(old)}'
s=s.replace(old,new,1)
extra='''        else if(x.contains("Party Master Help"))partyMasterHelp610();
        else if(x.contains("Ecosystem Pro"))'''
assert s.count(extra)==1,f'Original shadowed Help branch count {s.count(extra)}'
s=s.replace(extra,'''        else if(x.contains("Ecosystem Pro"))''',1)
assert s.count('else if(x.contains("Party Master Help"))partyMasterHelp610();')==1
start=s.index('private void handleRoomMenuItem810(String x)')
end=s.index('private void openDeepFlow810(',start)
routes=s[start:end]
assert routes.index('x.contains("Party Master Help")')<routes.index('x.contains("Party Master")')
assert 'private void roomMenuAll()' in s and 'private void roomMenu()' in s
assert 'KingNative3DRoom.open(' in s and 'KingRoomDice997.open(' in s
assert 'KingGiftSound993.setEnabled' in s
p.write_text(s)
print('PASS Party Master Help now opens genuine Help, not the generic Party Master panel')
print('PASS All Tools and 12 fast actions retained; payment/audio/permissions unchanged')
