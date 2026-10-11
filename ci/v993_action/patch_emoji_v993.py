#!/usr/bin/env python3
"""Extend 50 original live emoji to 120 unique animation/preview slots.
Original artwork and actions; no third-party assets. Limit RAM in panel.
"""
from pathlib import Path
import re, shutil, sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingEmojiAccess993.java'),pkg/'KingEmojiAccess993.java')
emoji=pkg/'LiveEmojiView.java'
party=pkg/'PartyActivity.java'
EXTRA=[
("Flying Kiss","💋"),("Hug Attack","🤗"),("Heart Cannon","💖"),("Love Shower","💞"),
("Rose Tornado","🌹"),("Wink Blast","😉"),("Dance Party","💃"),("Laugh Explosion","🤣"),
("Cry Flood","😭"),("Facepalm","🤦"),("High Five","🙌"),("Air Punch","👊"),
("Clap Storm","👏"),("Thumb Fire","👍"),("Rainbow Bridge","🌈"),("Bubble Rain","🫧"),
("Selfie Flash","📸"),("Cute Blush","🥰"),("Sleep Cloud","💤"),("Flying Panda","🐼"),
("Cat Dance","🐱"),("Puppy Jump","🐶"),("Tiger Roar","🐯"),("Dragon Flight","🐉"),
("Rocket Ride","🚀"),("Firework Fountain","🎆"),("Meteor Rain","☄️"),("Star Shower","🌠"),
("Diamond Fall","💎"),("Crown Drop","👑"),("Golden Confetti","🎊"),("Rose Parade","🌹"),
("Party Trumpet","🎺"),("Drum Roll","🥁"),("Heartbeat","💓"),("Magic Spell","🪄"),
("Angel Wings","🪽"),("Butterfly Swarm","🦋"),("Disco Ball","🪩"),("Music Jam","🎵"),
("Lion Dance","🦁"),("Rainbow Unicorn","🦄"),("Pirate Surprise","🏴‍☠️"),("Snow Flurry","❄️"),
("Lightning Flash","⚡"),("Storm Cloud","🌩️"),("Water Splash","💦"),("Beach Wave","🌊"),
("Golden Trophy","🏆"),("Heart Proposal","💍"),
("Level Sparkle","✨"),("Level Groove","🕺"),("Level Rose Fall","🌹"),
("Level Fire Burst","🔥"),("Level Royal Clap","👏"),("Level Rocket Lift","🚀"),
("Level Golden Rain","🪙"),("Level Moonwalk","🌙"),("Level Star Gate","🌌"),
("Level Diamond Flight","💎"),
("VIP Night","🌃"),("VIP Dream Car","🏎️"),("VIP Kiss Storm","💋"),
("VIP Royal Entry","👑"),("VIP Golden Dragon","🐲"),("VIP Ocean Palace","🏰"),
("VIP Galaxy Portal","🌌"),("VIP Moon Parade","🌙"),("VIP Flying Throne","🪑"),
("VIP King Universe","✨")
]
assert len(EXTRA)==70
s=emoji.read_text()
if "import android.graphics.Typeface;" not in s:
    s=s.replace("import android.graphics.Paint;", "import android.graphics.Paint;\nimport android.graphics.Typeface;",1)
for name,index in [('LABELS',0),('ICONS',1)]:
    pattern=(r'(public static final String\[\] '+name+r'\s*=\s*\{)(.*?)(\};)' if index==0
       else r'(private static final String\[\] '+name+r'\s*=\s*\{)(.*?)(\};)')
    m=re.search(pattern,s,re.S)
    assert m,name
    assert len(re.findall(r'"[^"]+"',m.group(2)))==50,name
    addition=','.join('"'+x[index]+'"' for x in EXTRA)
    s=s[:m.end(2)]+',\n        '+addition+'\n    '+s[m.end(2):]
    print('PASS',name,'120')
a=re.search(r'if\(token==null\|\|!token\.matches\((.*?)\)\)return -1;',s)
assert a
s=s[:a.start()]+'if(token==null||!token.startsWith("[live:")||!token.endsWith("]")||token.length()>11)return -1;'+s[a.end():]
s=s.replace('''        if(duration>=0)setLayerType(View.LAYER_TYPE_SOFTWARE,null); // Static picker previews do not need a software layer.''',
'''        // No per-view software bitmap layer: native memory protection.''')
old='''        if(expression<12)drawFace(c,t);
        else drawLiveIcon(c,t);
        if(running&&attached&&getWindowVisibility()==VISIBLE&&isShown())postInvalidateDelayed(33);'''
new='''        if(expression<12)drawFace(c,t);
        else if(expression<50)drawLiveIcon(c,t);
        else drawAction993(c,t);
        if(running&&attached&&getWindowVisibility()==VISIBLE&&isShown())
            postInvalidateDelayed(66);'''
assert s.count(old)==1
s=s.replace(old,new)
action=r'''    private void drawAction993(Canvas c,float time){
        float w=getWidth(),h=getHeight();
        if(w<1||h<1)return;
        float cx=w*.5f,cy=h*.5f,size=Math.min(w,h);
        int type=(expression-50)%8;
        int[] hues={0xfff861bf,0xffffc45e,0xff5deafa,0xffb58bff,
            0xff77eab4,0xffff7777,0xff8eb9ff,0xffffe199};
        int hue=hues[(expression-50)%hues.length];
        float beat=(float)Math.sin(time*5.3f),slide=(float)Math.sin(time*3.0f);
        float radius=size*(.18f+.022f*beat);
        color(withAlpha(hue,65));c.drawCircle(cx,cy,radius*2.4f,paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f,size*.016f));
        paint.setColor(withAlpha(hue,205));
        for(int i=0;i<2;i++)c.drawCircle(cx,cy,radius*(1.5f+i*.5f+.11f*slide),paint);
        paint.setStyle(Paint.Style.FILL);
        c.save();
        c.translate(cx+(type==0||type==4?slide*size*.23f:0),
                    cy+(type==1||type==2||type==6?beat*size*.10f:0));
        if(type==3||type==7)c.rotate(slide*14);
        if(type==5)c.rotate(time*25);
        float zoom=1f+.14f*(float)Math.sin(time*4.1);
        c.scale(zoom,zoom);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size*.43f);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(Math.max(1,size*.035f),0,size*.02f,hue);
        c.drawText(ICONS[expression],0,-(paint.ascent()+paint.descent())/2,paint);
        paint.clearShadowLayer();c.restore();
        String trail=type==0?"💋":type==1?"💞":type==2?"✨":type==3?"🌹":
                     type==4?"⚡":type==5?"⭐":type==6?"💎":"🎊";
        paint.setTextSize(size*.10f);
        for(int i=0;i<13;i++){
            float phase=(time*.37f+i*.079f)%1f;
            double angle=i*2.39996+time*.48;
            float r=size*(.14f+.4f*phase);
            float x=cx+(float)Math.cos(angle)*r;
            float y=cy+(float)Math.sin(angle)*r-phase*size*.21f;
            paint.setColor(withAlpha(i%2==0?hue:0xffffffff,(int)(235*(1f-phase))));
            c.drawText(i%3==0?trail:"✦",x,y,paint);
        }
        paint.setColor(Color.WHITE);paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(11,size*.064f));
        paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        c.drawText(LABELS[expression],cx,h*.88f,paint);
        if(expression>=110){
            paint.setColor(0xffffd58c);
            c.drawText("VIP PREVIEW",cx,h*.13f,paint);
        }
    }

'''
anchor='    private static int withAlpha(int color,int alpha){'
assert s.count(anchor)==1
s=s.replace(anchor,action+anchor,1)
emoji.write_text(s)
p=party.read_text()
panel=r'''    private String[] emojiSlice993(int from,int to){
        int[] ids=KingEmojiAccess993.range(from,to);
        String[] out=new String[ids.length];
        for(int i=0;i<ids.length;i++)out[i]=LiveEmojiView.token(ids[i]);
        return out;
    }
    private void showLiveEmojiPanelV530() {
        final LinearLayout sheet=new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackgroundColor(0xff171326);
        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(12),0,dp(8),0);
        TextView title=tv("KING LIVE • 120 ACTION EMOJI",16,Color.WHITE,true);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView close=tv("×",26,Color.WHITE,true);
        close.setGravity(Gravity.CENTER);
        bar.addView(close,new LinearLayout.LayoutParams(dp(48),dp(48)));
        sheet.addView(bar);
        TextView subtitle=tv("50 classic • 50 action • 10 level • 10 VIP preview",
            11,0xffffdc95,true);
        subtitle.setPadding(dp(12),0,dp(8),dp(5));
        sheet.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(25)));
        HorizontalScrollView strip=new HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs=new LinearLayout(this);
        strip.addView(tabs);
        sheet.addView(strip,new LinearLayout.LayoutParams(-1,dp(54)));
        ScrollView scroll=new ScrollView(this);
        LinearLayout grid=new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(7),dp(5),dp(7),dp(7));
        scroll.addView(grid);sheet.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        final String[] groups={"Action 25","Party 25","Classic A","Classic B",
            "Level 10","VIP 10","Stickers","Saved"};
        final AlertDialog dialog=new AlertDialog.Builder(this).setView(sheet).create();
        close.setOnClickListener(v->dialog.dismiss());
        for(int i=0;i<groups.length;i++){
            final int group=i;
            TextView tab=tv(groups[i],12,Color.WHITE,true);
            tab.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(92),dp(44));
            lp.setMargins(dp(2),dp(2),dp(2),dp(2));tabs.addView(tab,lp);
            tab.setOnClickListener(v->{
                String[] pack;
                if(group==0)pack=emojiSlice993(50,75);
                else if(group==1)pack=emojiSlice993(75,100);
                else if(group==2)pack=emojiSlice993(0,25);
                else if(group==3)pack=emojiSlice993(25,50);
                else if(group==4)pack=emojiSlice993(100,110);
                else if(group==5)pack=emojiSlice993(110,120);
                else if(group==6)pack=stickerPack550(0,67);
                else pack=favouriteEmojiPack610();
                fillEmojiGrid610(grid,pack,group==6?6:5,group<6);
                for(int n=0;n<tabs.getChildCount();n++)
                    tabs.getChildAt(n).setBackground(
                        bg(n==group?0xfff5b83e:0xff383047,14));
            });
        }
        tabs.getChildAt(0).performClick();
        dialog.setOnShowListener(v->{
            android.view.Window w=dialog.getWindow();
            if(w!=null){
                w.setBackgroundDrawableResource(android.R.color.transparent);
                w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                w.setGravity(Gravity.BOTTOM);
                int h=Math.min(dp(580),Math.max(dp(390),
                    (int)(getResources().getDisplayMetrics().heightPixels*.64f)));
                w.setLayout(-1,h);
            }
        });
        dialog.show();
    }
    private void previewLockedAction993(int id){
        if(id<0||id>=LiveEmojiView.LABELS.length)return;
        KingEmojiAuraView992 preview=new KingEmojiAuraView992(this,id,3500);
        preview.setBackground(bg(0xff1a1431,16));
        new AlertDialog.Builder(this)
            .setTitle("✨ "+LiveEmojiView.LABELS[id])
            .setMessage(KingEmojiAccess993.lockMessage(id)+
                "\nLOCAL preview only. VIP payments/unlocks are not active.")
            .setView(preview)
            .setPositiveButton("Close",null).show();
    }
'''
start=p.index('    private void showLiveEmojiPanelV530() {')
end=p.index('    private void fillEmojiGrid610(',start)
p=p[:start]+panel+p[end:]
old='''            cell.setOnClickListener(v->sendLiveEmojiV530(token));
            if(!livePack)cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});'''
new='''            final int emojiId993=live;
            final boolean locked993=emojiId993>=0 &&
                !KingEmojiAccess993.canSend(emojiId993,LevelSystem.read(this).level,0);
            if(locked993){
                String badge=KingEmojiAccess993.requiredVip(emojiId993)>0
                    ?"VIP "+KingEmojiAccess993.requiredVip(emojiId993)
                    :"LV "+KingEmojiAccess993.requiredLevel(emojiId993);
                TextView lock=tv("🔒 "+badge,10,0xffffd889,true);
                lock.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                cell.addView(lock,new FrameLayout.LayoutParams(-1,dp(20),Gravity.BOTTOM));
            }
            cell.setOnClickListener(v->{
                if(locked993){previewLockedAction993(emojiId993);return;}
                sendLiveEmojiV530(token);
            });
            cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});'''
assert p.count(old)==1
p=p.replace(old,new,1)
old='''    private void sendLiveEmojiV530(String emoji){
        String visual=stickerFallback610(emoji);'''
new='''    private void sendLiveEmojiV530(String emoji){
        int id993=LiveEmojiView.parse(emoji);
        if(id993>=0&&!KingEmojiAccess993.canSend(id993,LevelSystem.read(this).level,0)){
            toast(KingEmojiAccess993.lockMessage(id993));return;
        }
        String visual=stickerFallback610(emoji);'''
assert p.count(old)==1
p=p.replace(old,new,1)
party.write_text(p)
print('PASS new emoji tabs, 120 previews, level locks, VIP locked, server send guard')
