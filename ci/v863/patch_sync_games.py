from pathlib import Path
import sys
root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
s=p.read_text()

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit('missing '+label)
    s=s.replace(old,new,1)

rep('private String gameIcon(String t){if("rps".equals(t))return "✊";if("dice".equals(t))return "🎲";if("number".equals(t))return "🔢";if("coin".equals(t))return "🪙";if("wheel".equals(t))return "🎡";if("bingo".equals(t))return "🎯";return "🎮";}',
'''private String gameIcon(String t){if("rps".equals(t))return "✊";if("dice".equals(t))return "🎲";if("number".equals(t))return "🔢";if("coin".equals(t))return "🪙";if("wheel".equals(t))return "🎡";if("bingo".equals(t))return "🎯";if("slot".equals(t))return "🎰";if("reaction".equals(t))return "⚡";if("highlow".equals(t))return "🃏";if("sheep".equals(t))return "🐑";if("zoo".equals(t))return "🦁";return "🎮";}''','icons')

rep('String[] n={"✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Number Pick 1–9"};String[] t={"rps","dice","number","coin","wheel","bingo"};',
'''String[] n={"✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Number Pick 1–9","🎰 Slot Clash","⚡ Reaction Tap","🃏 High / Low","🐑 Sheep Race","🦁 Crazy Zoo"};String[] t={"rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo"};''','sync arrays')

rep('if(!hasMyMove()){if("rps".equals(type))rpsControls();else if("dice".equals(type))diceControls();else if("number".equals(type))numberControls();else if("coin".equals(type))coinControls();else if("wheel".equals(type))wheelControls();else if("bingo".equals(type))bingoControls();}',
'''if(!hasMyMove()){if("rps".equals(type))rpsControls();else if("dice".equals(type))diceControls();else if("number".equals(type))numberControls();else if("coin".equals(type))coinControls();else if("wheel".equals(type))wheelControls();else if("bingo".equals(type))bingoControls();else if("slot".equals(type))slotControls863();else if("reaction".equals(type))reactionControls863();else if("highlow".equals(type))highLowControls863();else if("sheep".equals(type))sheepControls863();else if("zoo".equals(type))zooControls863();}''','active controls')

anchor='    private void bingoControls(){LinearLayout row=new LinearLayout(this);for(int n=1;n<=9;n++){if(n==4||n==7){controls.addView(row);row=new LinearLayout(this);}final int x=n;Button b=button(String.valueOf(n));b.setOnClickListener(v->submit("Pick "+x,x));row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}controls.addView(row);}\n'
if anchor not in s: raise SystemExit('missing controls anchor')
extra='''    private void slotControls863(){Button b=button("🎰 Spin once");b.setOnClickListener(v->{int a=(int)(Math.random()*10),c=(int)(Math.random()*10),d=(int)(Math.random()*10);int score=a+c+d+(a==c&&c==d?50:(a==c||c==d||a==d?20:0));submit("Spin "+a+"-"+c+"-"+d,score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}\n    private void reactionControls863(){final long shown=android.os.SystemClock.elapsedRealtime();Button b=button("⚡ TAP NOW");b.setTextSize(20);b.setOnClickListener(v->{long ms=Math.max(1,android.os.SystemClock.elapsedRealtime()-shown);int score=(int)Math.max(1,100000-Math.min(99999,ms));submit("Reaction "+ms+" ms",score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(58)));}\n    private void highLowControls863(){LinearLayout row=new LinearLayout(this);Button h=button("⬆ High 51–100");Button l=button("⬇ Low 1–50");h.setOnClickListener(v->submit("High",1));l.setOnClickListener(v->submit("Low",0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(h,lp);row.addView(l,lp);controls.addView(row);}\n    private void sheepControls863(){Button b=button("🐑 Run my sheep");b.setOnClickListener(v->{int score=40+(int)(Math.random()*61);submit("Sheep speed "+score,score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}\n    private void zooControls863(){LinearLayout row=new LinearLayout(this);String[] animal={"🦁 Lion","🐯 Tiger","🐼 Panda"};for(String a:animal){final String choice=a;Button b=button(a);b.setOnClickListener(v->{int power=25+(int)(Math.random()*76);submit(choice+" power "+power,power);});row.addView(b,new LinearLayout.LayoutParams(0,dp(54),1));}controls.addView(row);}\n'''
s=s.replace(anchor,anchor+extra,1)

old='String p="rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"Pick 1–9 • closest to a shared draw wins";'
new='String p="rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"bingo".equals(game)?"Pick 1–9 • closest to a shared draw wins":"slot".equals(game)?"Spin once • best reel score wins":"reaction".equals(game)?"Tap as fast as you can when the control appears":"highlow".equals(game)?"Pick High 51–100 or Low 1–50":"sheep".equals(game)?"Run once • highest sheep speed wins":"Release one animal • highest power wins";'
rep(old,new,'prompt')

needle='private String resultFor(List<DocumentSnapshot>a){if("dice".equals(type)||"wheel".equals(type)){'
if needle not in s: raise SystemExit('missing resultFor')
replacement='''private String resultFor(List<DocumentSnapshot>a){if("slot".equals(type)||"sheep".equals(type)||"zoo".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}String label="slot".equals(type)?"🎰 Best reel score: ":"sheep".equals(type)?"🐑 Fastest race score: ":"🦁 Highest zoo power: ";return label+best+"\\n🏆 "+join(w);}if("reaction".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "⚡ Fastest reaction\\n🏆 "+join(w);}if("highlow".equals(type)){int draw=1+new java.security.SecureRandom().nextInt(100);String win=draw>=51?"High":"Low";List<String>w=new ArrayList<>();for(DocumentSnapshot d:a)if(win.equals(s(d.getString("move"))))w.add(s(d.getString("name")));return "🃏 Room card: "+draw+" • "+win+"\\n🏆 "+join(w);}if("dice".equals(type)||"wheel".equals(type)){'''
s=s.replace(needle,replacement,1)

p.write_text(s)
print('v8.6.3 sync mini-games patch applied')
