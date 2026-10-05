#!/usr/bin/env python3
from pathlib import Path
import re, sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
java=root/'app/src/main/java/com/kingplus/social'
party=java/'PartyActivity.java'
room_game=java/'RoomGameActivity.java'
main=java/'MainActivity.java'


def must_replace(text, old, new, label):
    if old not in text:
        raise SystemExit(f'PATCH_MISSING: {label}')
    return text.replace(old,new)

# ---------------- PartyActivity: gift catalog, live emoji, keyboard ----------------
s=party.read_text()

gift_data=[
('Gold Rose','🌹','Relationship'),('Love Heart','💗','Relationship'),('Diamond Ring','💍','Relationship'),('Sweet Kiss','💋','Relationship'),
('Lucky Star','🌟','Relationship'),('Birthday Cake','🎂','Relationship'),('Magic Wand','🪄','Relationship'),('Coffee Date','☕','Relationship'),
('Ice Cream','🍦','Relationship'),('Candy Box','🍬','Relationship'),('Teddy Bear','🧸','Relationship'),('Perfume','🌸','Relationship'),
('Gold Teapot','🫖','Activity'),('Gold Mask','🎭','Activity'),('Luxury Bag','👜','Activity'),('Jeweled Box','🎁','Activity'),
('Crystal Swan','🦢','Activity'),('Royal Throne','🪑','Activity'),('Moon Palace','🌙','Activity'),('Golden Horse','🐎','Activity'),
('Golden Eagle','🦅','Classic'),('Royal Crown','👑','Classic'),('Super Car','🏎️','Classic'),('Sport Bike','🏍️','Classic'),
('Yacht','🛥️','Classic'),('Private Jet','✈️','Classic'),('Helicopter','🚁','Classic'),('Rocket','🚀','Classic'),
('Galaxy Ship','🛸','Flying'),('Flying Carpet','🧞','Flying'),('Royal Castle','🏰','Flying'),('Dream Villa','🏡','Flying'),
('Firework','🎆','Flying'),('Party Bus','🚌','Flying'),('Music Stage','🎤','Flying'),('DJ Booth','🎧','Flying'),
('Lucky Fish','🐠','Fame'),('Peacock','🦚','Fame'),('Butterfly','🦋','Fame'),('Unicorn','🦄','Fame'),
('Panda','🐼','Fame'),('Tiger','🐯','Fame'),('Lionheart Glory','🦁','Fame'),('King Lion','🦁','Fame'),
('White Wolf','🐺','Privilege'),('Phoenix','🔥','Privilege'),('King Dragon','🐉','Privilege'),('Ice Dragon','🐲','Privilege'),
('Super Star','⭐','Privilege'),('Diamond Rain','💎','Privilege'),('Heart Rain','💖','Privilege'),('Galaxy','🌌','Privilege'),
('Universe','🪐','Filters'),('Aurora','🌈','Filters'),('Meteor Shower','☄️','Filters'),('Moon Walk','🌙','Filters'),
('VIP Crown','♛','Filters'),('Emperor Crown','👑','Filters'),
('Royal Scepter','⚜️','Parcel'),('Golden Wings','🪽','Parcel'),('Angel Wings','😇','Parcel'),('Fame Trophy','🏆','Parcel'),
('Champion Cup','🥇','Parcel'),('KING Throne','👑','Parcel')]
assert len(gift_data)==64
names=[x[0] for x in gift_data]; icons=[x[1] for x in gift_data]; cats=[x[2] for x in gift_data]
costs=[]
for i in range(len(gift_data)):
    value=int(round(50*(1.17**i)))
    costs.append(min(520000,max(50,value)))
vips=[0 if i<12 else min(12,1+(i-12)//5) for i in range(len(gift_data))]

def jstr(values):
    return '{'+','.join('"'+str(v).replace('\\','\\\\').replace('"','\\"')+'"' for v in values)+'}'
def jint(values): return '{'+','.join(str(int(v)) for v in values)+'}'

patterns={
    r'private final String\[\] giftNames = \{.*?\};':'private final String[] giftNames = '+jstr(names)+';',
    r'private final String\[\] giftIcons = \{.*?\};':'private final String[] giftIcons = '+jstr(icons)+';',
    r'private final int\[\] giftCosts = \{.*?\};':'private final int[] giftCosts = '+jint(costs)+';',
    r'private final String\[\] giftCategories = \{.*?\};':'private final String[] giftCategories = '+jstr(cats)+';',
    r'private final int\[\] giftVipRequired = \{.*?\};':'private final int[] giftVipRequired = '+jint(vips)+';'
}
for pat,repl in patterns.items():
    s,n=re.subn(pat,repl,s,count=1)
    if n!=1: raise SystemExit('PATCH_MISSING gift array '+pat)

s=must_replace(s,'private String giftCategory = "Activity";','private String giftCategory = "All";','gift default field')
s=must_replace(s,'giftQuantity=1; giftCategory="Activity"; giftSelectedIndex=0;','giftQuantity=1; giftCategory="All"; giftSelectedIndex=0;','gift default panel')
s=must_replace(s,'String[] cs={"Activity","Classic","Flying","Relationship","Fame","Privilege","Filters","Parcel"};','String[] cs={"All","Relationship","Activity","Classic","Flying","Fame","Privilege","Filters","Parcel"};','gift tabs')
s=must_replace(s,'if(!giftCategory.equals(giftCategories[i]))continue;','if(!"All".equals(giftCategory)&&!giftCategory.equals(giftCategories[i]))continue;','gift all filter')
s=s.replace('w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.58f));','w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.72f));',1)

# 50 animated originals on the first tab, six bundled SVGA effects on second tab, 67 stickers on third.
s=must_replace(s,'String[] labels={"Live","More","Classic","Faces","Saved"};','String[] labels={"Live 50","Effects","Stickers","Faces","Saved"};','emoji tabs')
s=must_replace(s,'String[] live=new String[12];for(int i=0;i<live.length;i++)live[i]=LiveEmojiView.token(i);','String[] live=new String[LiveEmojiView.LABELS.length];for(int i=0;i<live.length;i++)live[i]=LiveEmojiView.token(i);','emoji live count')
old='String[] pack=k==0?reference:(k==1?live:(k==2?stickerPack550(0,67):(k==3?faces:favouriteEmojiPack610())));\n                fillEmojiGrid610(grid,pack,k==2||k==3?6:4,k<2);'
new='String[] pack=k==0?live:(k==1?reference:(k==2?stickerPack550(0,67):(k==3?faces:favouriteEmojiPack610())));\n                fillEmojiGrid610(grid,pack,k==2||k==3?6:4,k<2);'
s=must_replace(s,old,new,'emoji tab mapping')

# Close Android IME after a valid message send is initiated. Keep unsent text on cloud failure.
old='String outgoing=text;if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\\n"+text;final String finalOutgoing=outgoing;'
new=old+'\n        hideRoomKeyboard(box);'
s=must_replace(s,old,new,'chat keyboard call')
marker='    private void seedLocalChat(){if(cloudRoom)return;'
helper='''    private void hideRoomKeyboard(EditText box){\n        if(box==null)return;\n        try{\n            box.clearFocus();\n            android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);\n            if(imm!=null)imm.hideSoftInputFromWindow(box.getWindowToken(),0);\n        }catch(Exception ignored){}\n    }\n'''
if marker not in s: raise SystemExit('PATCH_MISSING chat helper marker')
s=s.replace(marker,helper+marker,1)
party.write_text(s)

# ---------------- RoomGameActivity: every named game launches from inside Party Room ----------------
r=room_game.read_text()
needle='Button ludo=button("🎲 Online Ludo");ludo.setOnClickListener(v->startActivity(new Intent(this,OnlineLudoActivity.class)));LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);'
insert=needle+'''\n        TextView allGames=tv("All playable Party games",15,Color.WHITE,true);allGames.setPadding(dp(2),dp(10),0,dp(5));controls.addView(allGames);\n        String[] localCodes={"tic_tac_toe","rps","dice","guess","slot","coin","memory","reaction","highlow","wheel","sheep","werewolf","spy","draw","bingo","zoo","domino"};\n        String[] localNames={"❌⭕ Tic Tac Toe","✊ RPS","🎲 Dice","🔢 Guess","🎰 Slot","🪙 Coin","🧠 Memory","⚡ Reaction","🃏 High/Low","🎡 Wheel","🐑 Sheep","🐺 Werewolf","🕵 Spy","🎨 Draw & Guess","🎱 Bingo","🦁 Crazy Zoo","🁣 Domino"};\n        for(int base=0;base<localCodes.length;base+=3){\n            LinearLayout row=new LinearLayout(this);\n            for(int j=0;j<3&&base+j<localCodes.length;j++){int k=base+j;final String code=localCodes[k];Button g=button(localNames[k]);g.setTextSize(11);g.setOnClickListener(v->openPlayableRoomGame(code));LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(50),1);gp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(g,gp);}\n            controls.addView(row);\n        }'''
r=must_replace(r,needle,insert,'room all games launcher')
helper_marker='    private void rpsControls(){'
helper2='''    private void openPlayableRoomGame(String code){\n        Intent i=new Intent(this,GamePlayActivity.class);\n        i.putExtra("game",code);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",displayName);\n        startActivity(i);\n    }\n'''
if helper_marker not in r: raise SystemExit('PATCH_MISSING room helper marker')
r=r.replace(helper_marker,helper2+helper_marker,1)
room_game.write_text(r)

# ---------------- MainActivity: make the complete playable catalog visible, not search-only ----------------
m=main.read_text()
needle='''            addGameGridRow(list,new String[][]{{"🦁","Crazy Zoo","Collect & play","🦁 Crazy Zoo"},{"🁣","Domino","Playable matching","🁣 Domino"},{"🎤","Voice Room","Play & talk","Game Room"}});'''
extra=needle+'''\n            addGameGridRow(list,new String[][]{{"🧠","Memory Match","Playable pairs","🧠 Memory Match"},{"⚡","Reaction Tap","Playable reflex","⚡ Reaction Tap"},{"🃏","High / Low","Playable cards","🃏 High / Low"}});\n            addGameGridRow(list,new String[][]{{"🎡","Lucky Wheel","Playable spin","🎡 Lucky Wheel"},{"🎰","Lucky Slot","Playable slot","🎰 Lucky Slot"},{"🪙","Coin Toss","Playable toss","🪙 Coin Toss"}});\n            addGameGridRow(list,new String[][]{{"✊","RPS","Playable duel","✊ Rock Paper Scissors"},{"🎲","Dice Duel","Playable dice","🎲 Dice Duel"},{"🔢","Guess Number","Playable puzzle","🔢 Guess Number"}});'''
m=must_replace(m,needle,extra,'main visible game catalog')
main.write_text(m)

print('v8.6.0 patches applied successfully')
