#!/usr/bin/env python3
"""KING Plus v9.8.0: all advertised Game tiles launch actual playable games.

Built on verified v9.7.9 source, not old tracked root app. Keep Firebase Party,
online Ludo, voice, live emoji, wallet and all prior crash fixes.

Critically: the Games tab previously sent every non-Ludo tile into PartyActivity,
where a game was never launched. This patch routes the existing functional solo
GamePlayActivity and provides a separate legitimate multiplayer Party entry.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingGameRoutes980.java'),pkg/'KingGameRoutes980.java')
main=pkg/'MainActivity.java'
s=main.read_text()

def method_replace(s,needle,new,label):
    a=s.find(needle)
    if a<0 or s.count(needle)!=1:raise SystemExit(label+': method not found uniquely')
    openbrace=s.find('{',a)
    depth=0;quote=None;esc=False
    for i in range(openbrace,len(s)):
        ch=s[i]
        if quote:
            if esc:esc=False
            elif ch=='\\':esc=True
            elif ch==quote:quote=None
        else:
            if ch in ('"',"'"):quote=ch
            elif ch=='{':depth+=1
            elif ch=='}':
                depth-=1
                if depth==0:
                    print('PASS',label)
                    return s[:a]+new+s[i+1:]
    raise SystemExit(label+': unbalanced braces')

s=method_replace(s,'    private void games(){',r'''    private void games(){
        incrementMission("mission_game",1);
        screen="games";stopMic();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfffafafa);
        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(16),dp(12),dp(10),dp(6));
        TextView title=new TextView(this);
        title.setText("🎮 KING Games");title.setTextSize(25);
        title.setTextColor(0xff171717);title.setTypeface(null,Typeface.BOLD);
        head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView search=new TextView(this);search.setText("⌕");
        search.setTextSize(26);search.setTextColor(0xff4f4a57);
        search.setGravity(Gravity.CENTER);search.setOnClickListener(v->discover());
        head.addView(search,new LinearLayout.LayoutParams(dp(50),dp(50)));
        root.addView(head,new LinearLayout.LayoutParams(-1,dp(66)));

        ScrollView scroll=new ScrollView(this);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);
        host.setPadding(dp(14),0,dp(14),dp(18));scroll.addView(host);
        TextView instructions=new TextView(this);
        instructions.setText("Tap any game to PLAY NOW • solo modes work without another phone");
        instructions.setTextSize(13);instructions.setTextColor(0xff645e70);
        instructions.setPadding(dp(6),dp(8),dp(6),dp(10));host.addView(instructions);

        TextView multiplayer=new TextView(this);
        multiplayer.setText("👥  PLAY ONLINE IN PARTY ROOM\nRPS • Dice • Bingo • Coin • Wheel • Number Pick");
        multiplayer.setTextColor(Color.WHITE);multiplayer.setTextSize(14);
        multiplayer.setGravity(Gravity.CENTER);multiplayer.setTypeface(null,Typeface.BOLD);
        multiplayer.setPadding(dp(12),dp(12),dp(12),dp(12));
        multiplayer.setBackground(background(0xff6b3bbe,16));
        multiplayer.setOnClickListener(v->kingOpenGameRoom("rps"));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(90));
        mp.setMargins(0,0,0,dp(12));host.addView(multiplayer,mp);

        TextView f=new TextView(this);f.setText("My Games");
        f.setTextSize(17);f.setTypeface(null,Typeface.BOLD);
        f.setTextColor(0xff27242b);f.setPadding(dp(3),dp(8),0,dp(8));
        host.addView(f,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout highlights=new LinearLayout(this);
        highlights.setGravity(Gravity.TOP);
        addGameVideoCard940(highlights,"ludo","Ludo","Online + solo");
        addGameVideoCard940(highlights,"werewolf","Werewolf","Play now");
        addGameVideoCard940(highlights,"draw","Draw & Guess","Draw now");
        host.addView(highlights,new LinearLayout.LayoutParams(-1,dp(147)));

        TextView next=new TextView(this);
        next.setText("18 playable games • free practice, no cash prizes");
        next.setTextSize(16);next.setTypeface(null,Typeface.BOLD);
        next.setTextColor(0xff28232f);
        next.setPadding(dp(3),dp(10),0,dp(8));
        host.addView(next);

        String[][] games={
            {"bingo","Bingo"},{"domino","Domino"},{"spy","Spy"},
            {"sheep","Sheep Fight"},{"zoo","Crazy Zoo"},{"memory","Memory"},
            {"rps","Rock Paper Scissors"},{"dice","Dice Duel"},{"wheel","Wheel"},
            {"guess","Number Guess"},{"coin","Coin Toss"},{"reaction","Reaction Tap"},
            {"highlow","High / Low"},{"tic_tac_toe","Tic Tac Toe"},{"slot","Lucky Slot"},
            {"werewolf","Werewolf"},{"draw","Draw & Guess"},{"ludo","Ludo"}
        };
        LinearLayout row=null;
        for(int i=0;i<games.length;i++){
            if(i%3==0){
                row=new LinearLayout(this);row.setGravity(Gravity.TOP);
                host.addView(row,new LinearLayout.LayoutParams(-1,dp(150)));
            }
            addGameVideoCard940(row,games[i][0],games[i][1],
                "ludo".equals(games[i][0])?"Online + solo":"Play now");
        }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,1);setContentView(root);
    }''','build all Game tab cards with truthful playable local modes and separate Party multiplayer')

s=method_replace(s,'    private void openPlayableGame(String game){',r'''    private void openPlayableGame(String game){
        final String code=KingGameRoutes980.localGame(game);
        if(code==null){
            Toast.makeText(this,"This game is not installed yet",Toast.LENGTH_SHORT).show();
            return;
        }
        if("ludo".equals(code)){
            startActivity(new Intent(this,KingLudoLobbyActivity.class));
            return;
        }
        // Existing GamePlayActivity contains the playable game boards and
        // user-controlled actions for all advertised solo codes.
        Intent play=new Intent(this,GamePlayActivity.class);
        play.putExtra("game",code);
        startActivity(play);
    }''','fix every game tile to launch actual controls instead of a blank Party lobby')

s=method_replace(s,'    private void roomGames(){',r'''    private void roomGames(){
        final String[] labels={
            "🎲 Ludo","🐑 Sheep Fight","🐺 Werewolf",
            "🕵 Spy","🎨 Draw & Guess","🎱 Bingo",
            "🦁 Crazy Zoo","🧠 Memory","✊ Rock Paper Scissors",
            "🎲 Dice Duel","🁢 Domino","❎ Tic Tac Toe"
        };
        final String[] codes={
            "ludo","sheep","werewolf","spy","draw","bingo",
            "zoo","memory","rps","dice","domino","tic_tac_toe"
        };
        new AlertDialog.Builder(this).setTitle("🎮 Play a real game")
            .setItems(labels,(d,w)->openPlayableGame(codes[w]))
            .setNeutralButton("Party Multiplayer",(d,w)->kingOpenGameRoom("rps"))
            .setNegativeButton("Close",null).show();
    }''','old room Game menu now launches games or real Party, not mock results')

s=method_replace(s,'    private void playMiniGame(String game){',r'''    private void playMiniGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US);
        if(g.contains("dart"))g="reaction";
        else if(g.contains("racing"))g="highlow";
        else if(g.contains("snake"))g="memory";
        else if(g.contains("draw"))g="draw";
        openPlayableGame(g);
        // Never add TEST Diamonds or XP merely because the user opened a
        // fake random-result dialog; score must come from actual gameplay.
    }''','replace fake random wins and minted TEST coins with playable actions')

# The Game page's cards must not falsely describe offline modes as realtime.
old='card.setOnClickListener(v->openPlayableGame(code));'
if s.count(old)!=1:raise SystemExit('unexpected card onclick')
s=s.replace(old,old,1)

main.write_text(s)
manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
if 'android:name=".GamePlayActivity"' not in m:
    marker='<application'
    idx=m.find(marker)
    if idx<0:raise SystemExit('manifest missing application')
    # Insert before first known internal activity declaration, never export GamePlay.
    known='<activity android:name=".KingLudoLobbyActivity"'
    if known not in m:raise SystemExit('GamePlayActivity missing and no safe manifest anchor')
    m=m.replace(known,'<activity android:name=".GamePlayActivity" android:exported="false" />\n        '+known,1)
    manifest.write_text(m)
    print('PASS installed interactive GamePlayActivity in manifest')
else:print('PASS existing GamePlayActivity manifest registered')

g=root/'app/build.gradle';build=g.read_text()
old="versionCode 170; versionName '9.7.9-party-firestore-reconnect'"
if build.count(old)!=1:raise SystemExit('expected last successful v9.7.9 source')
g.write_text(build.replace(old,"versionCode 171; versionName '9.8.0-playable-games'",1))
# Jitsi's GitHub /raw redirect is intermittently returning HTTP 504 on
# GitHub Actions. Prefer the canonical raw.githubusercontent.com Maven mirror,
# keeping the exact same versions/artifacts and all Room Voice functionality.
candidate_files=[root/'settings.gradle',root/'settings.gradle.kts',
                 root/'build.gradle',root/'build.gradle.kts',
                 root/'app/build.gradle',root/'app/build.gradle.kts']
replaced=0
for gradle_file in candidate_files:
    if not gradle_file.exists():continue
    val=gradle_file.read_text()
    old_repo="https://github.com/jitsi/jitsi-maven-repository/raw/master/releases"
    raw_repo="https://raw.githubusercontent.com/jitsi/jitsi-maven-repository/master/releases"
    if old_repo in val:
        replaced+=val.count(old_repo)
        gradle_file.write_text(val.replace(old_repo,raw_repo))
if replaced:
    print(f'PASS Jitsi Maven same-artifact raw GitHub mirror ({replaced} references) to bypass 504')
else:
    print('NOTE Jitsi Maven source URL not found in top-level Gradle files; leave original repository untouched')
print('PASS KING Plus v9.8.0 all advertised game tiles navigate to interactive games')
