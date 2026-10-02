from pathlib import Path
import re

party_path = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
party = party_path.read_text(encoding='utf-8')

# v5.2 evolved its lobby and room layout. Add stable compatibility anchors plus the requested Home/Create control.
if 'homeCreateV530' not in party:
    header = '        TextView searchIcon = tv("⌕",27,0xff39343e,false); searchIcon.setGravity(Gravity.CENTER); searchIcon.setOnClickListener(v -> showLobbySearch(selected)); head.addView(searchIcon,new LinearLayout.LayoutParams(dp(46),dp(50)));\n'
    home = '''        TextView homeCreateV530 = tv("🏠＋",22,0xff39343e,true); homeCreateV530.setGravity(Gravity.CENTER);\n        homeCreateV530.setOnClickListener(v -> { if(user!=null&&db!=null) createRoomDialog(); else requireSignInForCreate(); });\n        head.addView(homeCreateV530,new LinearLayout.LayoutParams(dp(58),dp(50)));\n'''
    if header not in party:
        raise SystemExit('v5.3 wrapper: current v5.2 lobby header anchor not found')
    party = party.replace(header, header + home, 1)

# The v5.2 room kept addMemberStrip() but stopped calling it. Restore the call at a safe point; the v5.3 stage is inserted after it.
member_call = '        addMemberStrip();\n'
if member_call not in party:
    reaction = '        reactionBanner=tv("",42,Color.WHITE,true);reactionBanner.setGravity(Gravity.CENTER);reactionBanner.setVisibility(View.GONE);reactionBanner.setBackground(bg(0x663c1f58,30));page.addView(reactionBanner,new LinearLayout.LayoutParams(-1,dp(58)));\n'
    if reaction not in party:
        raise SystemExit('v5.3 wrapper: current v5.2 reaction banner anchor not found')
    party = party.replace(reaction, member_call + reaction, 1)

party_path.write_text(party, encoding='utf-8')

src_path = Path('tools/prepare_party_v530.py')
src = src_path.read_text(encoding='utf-8')

# The current lobby already has search; do not abort on the retired pre-v5.2 Create-button anchor.
old_guard = "elif 'this::searchPartyRoomsV530' not in s:\n    raise SystemExit('v5.3: lobby create/header anchor not found')"
new_guard = "elif 'homeCreateV530' not in s:\n    raise SystemExit('v5.3: compatible lobby header/home-create control not found')"
if old_guard in src:
    src = src.replace(old_guard, new_guard, 1)
elif new_guard not in src:
    raise SystemExit('v5.3 wrapper: expected lobby guard not found in prepare script')

# v5.2 renamed the composer EditText to composerBox and already has one emoji button. Rewire that button instead of adding a duplicate.
composer_block = re.compile(r"composer_anchor = 'composer\.addView\(message,new LinearLayout\.LayoutParams\(0,dp\(50\),1\)\);'\ncomposer_add = '''.*?'''", re.S)
replacement = '''composer_anchor = 'TextView emoji=pill("😊",0x0030283f,this::emojiPanel);'\ncomposer_add = ''' + "'''TextView emoji=pill(\"😊\",0x0030283f,this::showLiveEmojiPanelV530);'''"
src, count = composer_block.subn(replacement, src, count=1)
if count != 1 and 'this::showLiveEmojiPanelV530' not in src:
    raise SystemExit('v5.3 wrapper: expected composer compatibility block not found')

# v5.2 openCloudRoom now carries private/password flags.
old_open = 'openCloudRoom(docV530.getId(),str(docV530,"name","Live Party"),docV530.getString("ownerUid"),str(docV530,"ownerName","Host"));'
new_open = 'openCloudRoom(docV530.getId(),str(docV530,"name","Live Party"),docV530.getString("ownerUid"),str(docV530,"ownerName","Host"),Boolean.TRUE.equals(docV530.getBoolean("isPrivate")),Boolean.TRUE.equals(docV530.getBoolean("hasPassword")));'
if old_open in src:
    src = src.replace(old_open, new_open, 1)

exec(compile(src, str(src_path), 'exec'), {'__name__': '__main__', '__file__': str(src_path)})
