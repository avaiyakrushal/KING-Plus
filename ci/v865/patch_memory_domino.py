from pathlib import Path
import sys
root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
s=p.read_text()

def one(old,new,label):
    global s
    if old not in s:
        raise SystemExit('missing '+label)
    s=s.replace(old,new,1)

one('private long roundId; private String type="",prompt="",result="",status="closed"; private String tttBoard864=".........",tttTurnUid864="",tttXUid864="",tttOUid864="",tttXName864="X",tttOName864="O"; private boolean tttProcessing864;',
    'private long roundId; private String type="",prompt="",result="",status="closed"; private String tttBoard864=".........",tttTurnUid864="",tttXUid864="",tttOUid864="",tttXName864="X",tttOName864="O"; private boolean tttProcessing864; private String memoryLayout865=""; private long memoryRound865; private int memoryFirst865=-1,memoryPairs865,memoryAttempts865,dominoTarget865; private boolean memoryComparing865; private boolean[] memoryDone865=new boolean[8]; private Button[] memoryButtons865=new Button[8]; private long memoryStart865;',
    'fields')
old='private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener((d,e)->{if(e!=null){stateText.setText("Game state error: "+e.getMessage());return;}if(d==null||!d.exists()){roundId=0;type="";prompt="";result="";status="closed";resetTttState864();}else{Long r=d.getLong("roundId");roundId=r==null?0:r;type=s(d.getString("type"));prompt=s(d.getString("prompt"));result=s(d.getString("result"));status=s(d.getString("status"));if("ttt".equals(type)){tttBoard864=s(d.getString("board"));if(tttBoard864.length()!=9)tttBoard864=".........";tttTurnUid864=s(d.getString("turnUid"));tttXUid864=s(d.getString("xUid"));tttOUid864=s(d.getString("oUid"));tttXName864=s(d.getString("xName"));tttOName864=s(d.getString("oName"));}else resetTttState864();}renderState();listenMoves();renderControls();});}'
new='private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener((d,e)->{if(e!=null){stateText.setText("Game state error: "+e.getMessage());return;}if(d==null||!d.exists()){roundId=0;type="";prompt="";result="";status="closed";resetTttState864();resetMemoryState865("",0);dominoTarget865=0;}else{Long r=d.getLong("roundId");roundId=r==null?0:r;type=s(d.getString("type"));prompt=s(d.getString("prompt"));result=s(d.getString("result"));status=s(d.getString("status"));if("ttt".equals(type)){tttBoard864=s(d.getString("board"));if(tttBoard864.length()!=9)tttBoard864=".........";tttTurnUid864=s(d.getString("turnUid"));tttXUid864=s(d.getString("xUid"));tttOUid864=s(d.getString("oUid"));tttXName864=s(d.getString("xName"));tttOName864=s(d.getString("oName"));}else resetTttState864();if("memory".equals(type)){String ml=s(d.getString("memoryLayout"));if(roundId!=memoryRound865||!ml.equals(memoryLayout865))resetMemoryState865(ml,roundId);}else resetMemoryState865("",0);Long dt=d.getLong("dominoTarget");dominoTarget865=dt==null?0:Math.max(0,Math.min(6,dt.intValue()));}renderState();listenMoves();renderControls();});}'
one(old,new,'attach')
one('if("zoo".equals(t))return "🦁";if("ttt".equals(t))return "❌⭕";return "🎮";','if("zoo".equals(t))return "🦁";if("memory".equals(t))return "🧠";if("domino".equals(t))return "🁣";if("ttt".equals(t))return "❌⭕";return "🎮";','icons')
one('String[] n={"❌⭕ Tic Tac Toe","✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Number Pick 1–9","🎰 Slot Clash","⚡ Reaction Tap","🃏 High / Low","🐑 Sheep Race","🦁 Crazy Zoo"};String[] t={"ttt","rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo"};','String[] n={"❌⭕ Tic Tac Toe","✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Number Pick 1–9","🎰 Slot Clash","⚡ Reaction Tap","🃏 High / Low","🐑 Sheep Race","🦁 Crazy Zoo","🧠 Memory Duel","🁣 Domino Match"};String[] t={"ttt","rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo","memory","domino"};','start list')
one('else if("zoo".equals(type))zooControls863();}','else if("zoo".equals(type))zooControls863();else if("memory".equals(type))memoryControls865();else if("domino".equals(type))dominoControls865();}','active controls')
insert='''
    private void resetMemoryState865(String layout,long round){
        memoryLayout865=layout==null?"":layout;memoryRound865=round;memoryFirst865=-1;memoryPairs865=0;memoryAttempts865=0;memoryComparing865=false;memoryDone865=new boolean[8];memoryButtons865=new Button[8];memoryStart865=0;
    }
    private String memoryLayoutForRound865(long seed){
        List<Character>a=new ArrayList<>();for(char c='A';c<='D';c++){a.add(c);a.add(c);}
        java.util.Random r=new java.util.Random(seed^0x6b696e67706c7573L);Collections.shuffle(a,r);StringBuilder b=new StringBuilder(8);for(char c:a)b.append(c);return b.toString();
    }
    private void memoryControls865(){
        if(memoryLayout865==null||memoryLayout865.length()!=8){controls.addView(tv("Memory board unavailable • host can rematch",13,MUTED,false));return;}
        if(memoryStart865==0)memoryStart865=android.os.SystemClock.elapsedRealtime();
        controls.addView(tv("Find all 4 pairs • everyone gets the same shuffled board",13,MUTED,false));
        for(int r=0;r<2;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<4;c++){final int idx=r*4+c;Button b=button(memoryDone865[idx]?String.valueOf(memoryLayout865.charAt(idx)):"?");b.setTextSize(24);memoryButtons865[idx]=b;b.setEnabled(!memoryComparing865&&!memoryDone865[idx]&&!hasMyMove());b.setOnClickListener(v->memoryTap865(idx));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(66),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}controls.addView(row);}
        controls.addView(tv("Attempts: "+memoryAttempts865+"  •  Pairs: "+memoryPairs865+"/4",13,GOLD,true));
    }
    private void memoryTap865(int idx){
        if(memoryComparing865||idx<0||idx>=8||memoryDone865[idx]||hasMyMove()||memoryLayout865.length()!=8)return;Button b=memoryButtons865[idx];if(b==null)return;b.setText(String.valueOf(memoryLayout865.charAt(idx)));b.setEnabled(false);
        if(memoryFirst865<0){memoryFirst865=idx;return;}if(memoryFirst865==idx)return;final int first=memoryFirst865;memoryFirst865=-1;memoryAttempts865++;
        if(memoryLayout865.charAt(first)==memoryLayout865.charAt(idx)){memoryDone865[first]=memoryDone865[idx]=true;memoryPairs865++;if(memoryPairs865>=4){long ms=Math.max(1,android.os.SystemClock.elapsedRealtime()-memoryStart865);int score=(int)Math.max(1,100000-Math.min(85000,ms)-Math.min(12000,memoryAttempts865*600));submit("Cleared in "+memoryAttempts865+" attempts • "+ms+" ms",score);}return;}
        memoryComparing865=true;b.postDelayed(()->{try{if(!memoryDone865[first]&&memoryButtons865[first]!=null){memoryButtons865[first].setText("?");memoryButtons865[first].setEnabled(true);}if(!memoryDone865[idx]&&memoryButtons865[idx]!=null){memoryButtons865[idx].setText("?");memoryButtons865[idx].setEnabled(true);}}finally{memoryComparing865=false;}},650);
    }
    private void dominoControls865(){
        controls.addView(tv("Chain end: [ "+dominoTarget865+" ] • choose the tile whose LEFT side matches",14,GOLD,true));
        int correct=(int)(Math.abs(roundId)%6);for(int base=0;base<6;base+=3){LinearLayout row=new LinearLayout(this);for(int j=0;j<3;j++){final int i=base+j;int left=i==correct?dominoTarget865:(dominoTarget865+i+1)%7;int right=(dominoTarget865*3+i*2+2)%7;final int fleft=left;final String label="["+left+"|"+right+"]";Button b=button(label);b.setTextSize(18);b.setOnClickListener(v->submit(label,fleft==dominoTarget865?100:0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}controls.addView(row);}
    }
'''
anchor='    private void startRound(String game){'
if anchor not in s: raise SystemExit('missing anchor')
s=s.replace(anchor,insert+'\n'+anchor,1)
oldp='String p="rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"bingo".equals(game)?"Pick 1–9 • closest to a shared draw wins":"slot".equals(game)?"Spin once • best reel score wins":"reaction".equals(game)?"Tap as fast as you can when the control appears":"highlow".equals(game)?"Pick High 51–100 or Low 1–50":"sheep".equals(game)?"Run once • highest sheep speed wins":"Release one animal • highest power wins";'
newp='String p="rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"bingo".equals(game)?"Pick 1–9 • closest to a shared draw wins":"slot".equals(game)?"Spin once • best reel score wins":"reaction".equals(game)?"Tap as fast as you can when the control appears":"highlow".equals(game)?"Pick High 51–100 or Low 1–50":"sheep".equals(game)?"Run once • highest sheep speed wins":"memory".equals(game)?"Memory Duel • clear the same 4-pair board":"domino".equals(game)?"Domino Match • match the shared chain end":"Release one animal • highest power wins";'
one(oldp,newp,'prompt')
one('value.put("actorUid",me.getUid());value.put("actorName",displayName);value.put("type",game);value.put("prompt",p);value.put("result","");value.put("status","active");value.put("roundId",next);value.put("updatedAt",FieldValue.serverTimestamp());','value.put("actorUid",me.getUid());value.put("actorName",displayName);value.put("type",game);value.put("prompt",p);value.put("result","");value.put("status","active");value.put("roundId",next);if("memory".equals(game))value.put("memoryLayout",memoryLayoutForRound865(next));if("domino".equals(game))value.put("dominoTarget",new java.security.SecureRandom().nextInt(7));value.put("updatedAt",FieldValue.serverTimestamp());','round extras')
marker='private String resultFor(List<DocumentSnapshot>a){'
if marker not in s: raise SystemExit('missing resultFor')
rep='''private String resultFor(List<DocumentSnapshot>a){if("memory".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "🧠 Memory Duel\\n🏆 "+join(w);}if("domino".equals(type)){List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");if(n!=null&&n.intValue()>=100)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "🁣 Chain end "+dominoTarget865+"\\n🏆 "+join(w);}'''
s=s.replace(marker,rep,1)
p.write_text(s)
print('v8.6.5 memory/domino patch applied')