from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')
src = MAIN.read_text(encoding='utf-8')

# v3.3.0 replaces the simple Game list with a richer KING Plus Games Center.
pattern = re.compile(r'    private void games\(\)\{.*?^    private void discover\(\)\{', re.S | re.M)
replacement = r'''    private void games(){
        incrementMission("mission_game",1);
        renderGamesCategory("Hot");
    }

    private void renderGamesCategory(String selected){
        screen="games"; stopMic();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);

        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(18),dp(14),dp(14),dp(6));
        TextView h=new TextView(this); h.setText("Game"); h.setTextSize(30); h.setTextColor(0xff171717); h.setTypeface(null,Typeface.BOLD);
        head.addView(h,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView stats=new TextView(this); stats.setText("🏆 Stats"); stats.setGravity(Gravity.CENTER); stats.setTextColor(Color.WHITE); stats.setTypeface(null,Typeface.BOLD); stats.setBackground(background(PURPLE,18));
        stats.setOnClickListener(v->kingGameStats()); head.addView(stats,new LinearLayout.LayoutParams(dp(92),dp(40))); root.addView(head);

        TextView note=new TextView(this); note.setText("Quick match • 1v1 • team play • friends • voice game rooms"); note.setTextSize(12); note.setTextColor(0xff77717f); note.setPadding(dp(18),0,dp(18),dp(8)); root.addView(note);

        LinearLayout tabs=new LinearLayout(this); tabs.setPadding(dp(12),0,dp(12),dp(8));
        String[] names={"Hot","LUDO","Party","Team"};
        for(String n:names){
            TextView t=new TextView(this); t.setText(n); t.setGravity(Gravity.CENTER); t.setTypeface(null,Typeface.BOLD);
            t.setTextColor(n.equals(selected)?0xff111111:0xff4e4a55); t.setBackground(background(n.equals(selected)?0xffffe500:0xffefeff5,12));
            t.setOnClickListener(v->renderGamesCategory(n));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1); p.setMargins(dp(3),0,dp(3),0); tabs.addView(t,p);
        }
        root.addView(tabs);

        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(4),dp(14),dp(18)); sv.addView(list);
        cardLine(list,"🕘  Match History","Recent KING Plus game results",this::kingGameHistory);

        if("Hot".equals(selected)){
            kingGameCard(list,"🎲  Ludo Master","2–4 players • quick / team match","Ludo Master");
            kingGameCard(list,"🐑  Sheep Fight","1v1 • quick battle","Sheep Fight");
            kingGameCard(list,"🐺  Werewolf","Group social deduction • voice friendly","Werewolf");
            kingGameCard(list,"🕵  Spy Game","Find the spy • group game","Spy Game");
            kingGameCard(list,"🎨  Draw & Guess","Draw, guess and score","Draw & Guess");
            kingGameCard(list,"🎱  Bingo","Fast number challenge","Bingo");
            kingGameCard(list,"🦁  Crazy Zoo","Casual collection challenge","Crazy Zoo");
            cardLine(list,"🔥  Free Fire MAX Squad Room","Create/join a KING Plus voice team room",()->kingOpenGameRoom("Free Fire MAX"));
        } else if("LUDO".equals(selected)){
            cardLine(list,"🎲  Ludo • Quick Match","Fast local test round",()->kingStartGame("Ludo Master","Quick Match"));
            cardLine(list,"⚔  Ludo • 1 vs 1","Head-to-head test round",()->kingStartGame("Ludo Master","1 vs 1"));
            cardLine(list,"👥  Ludo • 4 Players","Four-player style test round",()->kingStartGame("Ludo Master","4 Players"));
            cardLine(list,"🤝  Ludo • Team 2v2","Team-style match",()->kingStartGame("Ludo Master","Team 2v2"));
            cardLine(list,"🎤  Ludo Voice Room","Open Party center for a live game room",()->kingOpenGameRoom("Ludo Master"));
        } else if("Party".equals(selected)){
            kingGameCard(list,"🐺  Werewolf","4–8 player role game","Werewolf");
            kingGameCard(list,"🕵  Spy Game","Secret-role group game","Spy Game");
            kingGameCard(list,"🎨  Draw & Guess","Creative party game","Draw & Guess");
            kingGameCard(list,"🎱  Bingo","Group number game","Bingo");
        } else {
            kingGameCard(list,"🐑  Sheep Fight","1v1 or team battle","Sheep Fight");
            kingGameCard(list,"🐺  Werewolf Team","Group voice game","Werewolf");
            cardLine(list,"🔥  Free Fire MAX Squad Room","Voice/team room launcher only",()->kingOpenGameRoom("Free Fire MAX"));
            cardLine(list,"🔗  Invite Friends to Game","Share a KING Plus game invite",()->kingInviteGame("KING Plus Game Room"));
        }

        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,1); setContentView(root);
    }

    private void kingGameCard(LinearLayout host,String title,String sub,String game){
        cardLine(host,title,sub,()->kingGameModes(game));
    }

    private void kingGameModes(String game){
        final String[] modes={"⚡ Quick Match","⚔ 1 vs 1","👥 Team Match","🔗 Play with Friends","🎤 Voice Game Room","📜 Rules"};
        new AlertDialog.Builder(this).setTitle(game).setItems(modes,(d,w)->{
            if(w==0)kingStartGame(game,"Quick Match");
            else if(w==1)kingStartGame(game,"1 vs 1");
            else if(w==2)kingStartGame(game,"Team Match");
            else if(w==3)kingInviteGame(game);
            else if(w==4)kingOpenGameRoom(game);
            else kingGameRules(game);
        }).setNegativeButton("Close",null).show();
    }

    private void kingStartGame(String game,String mode){
        String result; int points; boolean win;
        if(game.contains("Ludo")){
            int you=1+(int)(Math.random()*6), rival=1+(int)(Math.random()*6); win=you>=rival; points=win?20:6;
            result="You rolled "+you+" • Rival "+rival+" • "+(win?"WIN":"ROUND LOST");
        } else if(game.contains("Sheep")){
            int you=40+(int)(Math.random()*61), rival=40+(int)(Math.random()*61); win=you>=rival; points=win?18:5;
            result="Power "+you+" vs "+rival+" • "+(win?"WIN":"ROUND LOST");
        } else if(game.contains("Werewolf")){
            String[] roles={"Villager","Werewolf","Seer","Doctor"}; String role=roles[(int)(Math.random()*roles.length)]; win=Math.random()>.45; points=win?22:8;
            result="Private role: "+role+" • "+(win?"Your side survived":"Round finished");
        } else if(game.contains("Spy")){
            String[] words={"Beach","Cinema","Airport","School","Market","Hotel"}; String word=words[(int)(Math.random()*words.length)]; win=Math.random()>.45; points=win?20:7;
            result="Secret clue: "+word+" • "+(win?"Spy round cleared":"Spy escaped");
        } else if(game.contains("Draw")){
            String[] words={"Crown","Tiger","Diamond","Guitar","Rocket","Mango"}; String word=words[(int)(Math.random()*words.length)]; win=true; points=15;
            result="Draw this word: "+word+" • Share clues with friends";
        } else if(game.contains("Bingo")){
            int a=1+(int)(Math.random()*25), b=26+(int)(Math.random()*25), c=51+(int)(Math.random()*25); win=Math.random()>.5; points=win?18:6;
            result="Draw: "+a+", "+b+", "+c+" • "+(win?"BINGO!":"Keep playing");
        } else if(game.contains("Zoo")){
            String[] animals={"Lion","Panda","Elephant","Tiger","Fox","Peacock"}; String animal=animals[(int)(Math.random()*animals.length)]; win=true; points=10+(int)(Math.random()*11);
            result="You found: "+animal+" • Collection +1";
        } else {
            win=Math.random()>.5; points=win?15:5; result=win?"WIN":"ROUND COMPLETE";
        }
        kingRecordGame(game,mode,result,points,win);
        final String finalResult=result; final int finalPoints=points;
        new AlertDialog.Builder(this).setTitle(game+" • "+mode)
            .setMessage(finalResult+"\n\n+"+finalPoints+" Game Points\nNo real-money purchase or billing is used.")
            .setPositiveButton("Play Again",(d,w)->kingStartGame(game,mode))
            .setNeutralButton("Voice Room",(d,w)->kingOpenGameRoom(game))
            .setNegativeButton("Done",null).show();
    }

    private void kingRecordGame(String game,String mode,String result,int points,boolean win){
        SharedPreferences p=getPreferences(0);
        int matches=p.getInt("king_game_matches",0)+1;
        int wins=p.getInt("king_game_wins",0)+(win?1:0);
        int total=p.getInt("king_game_points",0)+Math.max(0,points);
        String old=p.getString("king_game_history","");
        String row="#"+matches+" • "+game+" • "+mode+" • "+result+" • +"+points+" GP";
        String history=row+(old.isEmpty()?"":"\n"+old);
        if(history.length()>7000)history=history.substring(0,7000);
        p.edit().putInt("king_game_matches",matches).putInt("king_game_wins",wins).putInt("king_game_points",total).putString("king_game_history",history).apply();
    }

    private void kingGameHistory(){
        screen="game_history"; base("Game History","KING Plus local/test match history");
        String history=getPreferences(0).getString("king_game_history","");
        if(history.isEmpty())text("No matches played yet.",15,MUTED,false);
        else{
            String[] rows=history.split("\\n");
            for(int i=0;i<Math.min(rows.length,15);i++)if(!rows[i].trim().isEmpty())text(rows[i],13,MUTED,false);
        }
        button("Back to Games",PURPLE,this::games);
    }

    private void kingGameStats(){
        screen="game_stats"; base("Game Stats","KING Plus no-billing game progress");
        SharedPreferences p=getPreferences(0); int matches=p.getInt("king_game_matches",0), wins=p.getInt("king_game_wins",0), points=p.getInt("king_game_points",0);
        int rate=matches==0?0:(wins*100/matches);
        text("🎮 Matches: "+matches,20,Color.WHITE,true);
        text("🏆 Wins: "+wins+" • Win rate: "+rate+"%",18,Color.WHITE,true);
        text("⭐ Game Points: "+points,20,0xffffd768,true);
        text("Game Points are local/test progression only and are not purchased coins or cash value.",13,MUTED,false);
        button("Match History",CARD,this::kingGameHistory);
        button("Back to Games",PURPLE,this::games);
    }

    private void kingGameRules(String game){
        String rules;
        if(game.contains("Ludo"))rules="Roll the dice and race your pieces toward the finish. KING Plus test rounds use a compact dice challenge; live friends can coordinate in a voice room.";
        else if(game.contains("Sheep"))rules="Choose quick, 1v1 or team play. Higher battle power wins the compact test round.";
        else if(game.contains("Werewolf"))rules="Players receive hidden roles. Villagers find the werewolf while special roles help the village. Use a voice room for group discussion.";
        else if(game.contains("Spy"))rules="Most players share a location clue while one player is the spy. Ask questions and identify the spy before the round ends.";
        else if(game.contains("Draw"))rules="One player gets a word to draw while friends try to guess it. Use Play with Friends or a voice game room for a group round.";
        else if(game.contains("Bingo"))rules="Mark called numbers. Complete the target pattern before the other players.";
        else if(game.contains("Zoo"))rules="Collect animals through casual rounds and build your local test collection.";
        else rules="Use KING Plus voice rooms to coordinate with your team. Third-party game software is not embedded in KING Plus.";
        new AlertDialog.Builder(this).setTitle(game+" • Rules").setMessage(rules).setPositiveButton("OK",null).show();
    }

    private void kingInviteGame(String game){
        Intent share=new Intent(Intent.ACTION_SEND); share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,"Join me in KING Plus • "+game+" • open the Game tab and choose Play with Friends.");
        try{startActivity(Intent.createChooser(share,"Invite friends"));}catch(Exception e){Toast.makeText(this,"Share unavailable",Toast.LENGTH_SHORT).show();}
    }

    private void kingOpenGameRoom(String game){
        Intent i=new Intent(this,PartyActivity.class); i.putExtra("displayName",displayName); i.putExtra("requestedGame",game); startActivity(i);
        Toast.makeText(this,"Party center opened • create/join a Game room for "+game,Toast.LENGTH_LONG).show();
    }

    private void discover(){'''
src,count = pattern.subn(lambda m: replacement,src,count=1)
if count != 1:
    raise SystemExit('Could not replace Game page for v3.3.0')

# Keep the in-room Game picker aligned with the Games Center.
pattern = re.compile(r'    private void roomGames\(\)\{.*?^    private void playMiniGame\(String game\)\{', re.S | re.M)
replacement = r'''    private void roomGames(){
        final String[] gameNames={"🎲 Ludo Master","🐑 Sheep Fight","🐺 Werewolf","🕵 Spy Game","🎨 Draw & Guess","🎱 Bingo","🦁 Crazy Zoo"};
        new AlertDialog.Builder(this).setTitle("🎮 Room Games").setItems(gameNames,(d,w)->{
            String raw=gameNames[w]; String game=raw.substring(raw.indexOf(' ')+1);
            kingGameModes(game);
        }).setNegativeButton("Close",null).show();
    }
    private void playMiniGame(String game){'''
src,count2 = pattern.subn(lambda m: replacement,src,count=1)
if count2 != 1:
    raise SystemExit('Could not update room Game picker for v3.3.0')

MAIN.write_text(src,encoding='utf-8')

gradle=BUILD.read_text(encoding='utf-8')
gradle=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'","versionCode 47; versionName '3.3.0'",gradle)
BUILD.write_text(gradle,encoding='utf-8')
print('Prepared KING Plus v3.3.0 Games Center')
