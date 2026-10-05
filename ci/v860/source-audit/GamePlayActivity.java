package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class GamePlayActivity extends Activity {
    private static final int PURPLE=0xff8a49ed, BG=0xff130a25, CARD=0xff33204f, MUTED=0xffcfc4df;
    private final Random random=new Random();
    private LinearLayout page;
    private final Button[] cells=new Button[9];
    private final char[] board=new char[9];
    private boolean finished;
    private TextView status;
    private int ludoYou=0,ludoBot=0;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        String game=getIntent().getStringExtra("game");
        if(game==null)game="tic_tac_toe";
        if("rps".equals(game))showRps();
        else if("dice".equals(game))showDice();
        else if("guess".equals(game))showGuess();
        else if("ludo".equals(game)){startActivity(new android.content.Intent(this,OnlineLudoActivity.class));finish();}
        else if("slot".equals(game))showSlot();
        else if("coin".equals(game))showCoin();
        else if("memory".equals(game))showMemory();
        else if("reaction".equals(game))showReactionTap();
        else if("highlow".equals(game))showHighLow();
        else if("wheel".equals(game))showWheel();
        else if("sheep".equals(game))showSheepFight();
        else if("werewolf".equals(game))showWerewolf();
        else if("spy".equals(game))showSpy();
        else if("draw".equals(game))showDrawGuess();
        else if("bingo".equals(game))showBingo();
        else if("zoo".equals(game))showZoo();
        else if("domino".equals(game))showDomino();
        else showTicTacToe();
    }

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView label(String text,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);v.setPadding(dp(8),dp(8),dp(8),dp(8));return v;}
    private Button action(String text){Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);b.setTextSize(15);b.setAllCaps(false);b.setBackground(bg(PURPLE,16));return b;}
    private void base(String title,String sub){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(18),dp(18),dp(18),dp(24));android.widget.ScrollView scroll=new android.widget.ScrollView(this);scroll.setFillViewport(true);scroll.addView(page);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=label("‹",34,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(label(title,24,Color.WHITE,true));info.addView(label(sub,12,MUTED,false));head.addView(info,new LinearLayout.LayoutParams(0,dp(62),1));page.addView(head);
        setSafe(root);
    }
    private void setSafe(View root){setContentView(root);final int l=root.getPaddingLeft(),t=root.getPaddingTop(),r=root.getPaddingRight(),b=root.getPaddingBottom();ViewCompat.setOnApplyWindowInsetsListener(root,(v,in)->{Insets bars=in.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(l,t+bars.top,r,b+bars.bottom);return in;});ViewCompat.requestApplyInsets(root);}

    private void showTicTacToe(){
        finished=false;
        base("Tic Tac Toe","Real playable game • You are X");
        for(int i=0;i<9;i++)board[i]=' ';
        status=label("Your turn",17,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setPadding(dp(8),dp(8),dp(8),dp(8));grid.setBackground(bg(CARD,18));
        for(int r=0;r<3;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<3;c++){int idx=r*3+c;Button b=action("");b.setTextSize(30);b.setBackground(bg(0xff4a3268,12));b.setOnClickListener(v->playCell(idx));cells[idx]=b;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(86),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}grid.addView(row);}page.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        Button again=action("New Game");again.setOnClickListener(v->showTicTacToe());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(52));ap.setMargins(0,dp(14),0,0);page.addView(again,ap);
    }
    private void playCell(int idx){if(finished||board[idx]!=' ')return;board[idx]='X';cells[idx].setText("X");if(check('X')){finishTtt("You win 👑");return;}if(full()){finishTtt("Draw");return;}int bot;do{bot=random.nextInt(9);}while(board[bot]!=' ');board[bot]='O';cells[bot].setText("O");if(check('O'))finishTtt("KING Bot wins");else if(full())finishTtt("Draw");else status.setText("Your turn");}
    private boolean full(){for(char c:board)if(c==' ')return false;return true;}
    private boolean check(char p){int[][] w={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[] a:w)if(board[a[0]]==p&&board[a[1]]==p&&board[a[2]]==p)return true;return false;}
    private void finishTtt(String msg){finished=true;status.setText(msg);}

    private void showLudo(){
        base("Ludo Race","Playable KING Plus quick race • first to 30");ludoYou=0;ludoBot=0;status=label(ludoBoard(),18,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(150)));Button roll=action("🎲 Roll & Move");roll.setOnClickListener(v->ludoRoll());page.addView(roll,new LinearLayout.LayoutParams(-1,dp(60)));
    }
    private String raceBar(int n){int filled=Math.min(10,n/3);StringBuilder b=new StringBuilder();for(int i=0;i<10;i++)b.append(i<filled?"■":"□");return b.toString();}
    private String ludoBoard(){return "YOU  "+raceBar(ludoYou)+"  "+ludoYou+"/30\n\nBOT  "+raceBar(ludoBot)+"  "+ludoBot+"/30";}
    private void ludoRoll(){if(ludoYou>=30||ludoBot>=30)return;int you=1+random.nextInt(6),bot=1+random.nextInt(6);ludoYou=Math.min(30,ludoYou+you);ludoBot=Math.min(30,ludoBot+bot);status.setText(ludoBoard()+"\n\nYou rolled "+you+" • Bot "+bot+(ludoYou>=30?"\n🏆 YOU WIN":ludoBot>=30?"\nKING Bot wins":""));}

    private void showSlot(){
        base("Lucky Slot","Playable test-only party slot • no real money");status=label("🍒  ⭐  💎",34,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(120)));Button spin=action("Spin FREE");spin.setOnClickListener(v->{String[] a={"🍒","⭐","💎","👑","🍋"};String x=a[random.nextInt(a.length)],y=a[random.nextInt(a.length)],z=a[random.nextInt(a.length)];status.setText(x+"  "+y+"  "+z+"\n\n"+(x.equals(y)&&y.equals(z)?"JACKPOT ✨":"Try again"));});page.addView(spin,new LinearLayout.LayoutParams(-1,dp(60)));
    }

    private void showCoin(){
        base("Coin Toss","Playable quick challenge");status=label("Choose Heads or Tails",22,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(100)));LinearLayout row=new LinearLayout(this);Button h=action("👑 Heads"),t=action("⭐ Tails");h.setOnClickListener(v->coinPick(true));t.setOnClickListener(v->coinPick(false));row.addView(h,new LinearLayout.LayoutParams(0,dp(58),1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(8),0,0,0);row.addView(t,p);page.addView(row);
    }
    private void coinPick(boolean heads){boolean result=random.nextBoolean();status.setText((result?"👑 HEADS":"⭐ TAILS")+"\n"+(result==heads?"You win ✨":"KING Bot wins"));}

    private void showRps(){
        base("Rock Paper Scissors","Real playable quick round");
        status=label("Choose your move",19,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(58)));
        String[] names={"✊ Rock","✋ Paper","✌ Scissors"};for(int i=0;i<3;i++){final int pick=i;Button b=action(names[i]);b.setOnClickListener(v->playRps(pick));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(7),0,0);page.addView(b,lp);}    }
    private void playRps(int you){int bot=random.nextInt(3);String[] n={"Rock","Paper","Scissors"};String result=you==bot?"Draw":((you-bot+3)%3==1?"You win 👑":"KING Bot wins");status.setText("You: "+n[you]+" • Bot: "+n[bot]+"\n"+result);}

    private void showDice(){
        base("Dice Duel","Real playable dice challenge");
        status=label("Tap Roll Dice",20,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(86)));
        Button roll=action("🎲 Roll Dice");roll.setOnClickListener(v->{int you=1+random.nextInt(6),bot=1+random.nextInt(6);String r=you==bot?"Draw":you>bot?"You win 👑":"KING Bot wins";status.setText("You rolled "+you+" • Bot "+bot+"\n"+r);});page.addView(roll,new LinearLayout.LayoutParams(-1,dp(60)));
    }

    private void showGuess(){
        base("Guess the Number","Real playable puzzle • 1 to 20");
        final int target=1+random.nextInt(20);final boolean[] solved={false};status=label("I picked a number from 1 to 20",18,0xffffd768,true);page.addView(status,new LinearLayout.LayoutParams(-1,dp(58)));
        EditText input=new EditText(this);input.setHint("Enter guess");input.setTextColor(Color.WHITE);input.setHintTextColor(0xff998eaa);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setBackground(bg(CARD,16));input.setPadding(dp(14),0,dp(14),0);page.addView(input,new LinearLayout.LayoutParams(-1,dp(54)));
        Button guess=action("Guess");guess.setOnClickListener(v->{try{if(solved[0])return;int g=Integer.parseInt(input.getText().toString().trim());if(g<1||g>20){Toast.makeText(this,"Enter a number 1–20",Toast.LENGTH_SHORT).show();return;}if(g==target){solved[0]=true;input.setEnabled(false);status.setText("Correct! 👑 Number was "+target);}else status.setText(g<target?"Too low • try higher":"Too high • try lower");}catch(Exception e){Toast.makeText(this,"Enter a number 1–20",Toast.LENGTH_SHORT).show();}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(0,dp(10),0,0);page.addView(guess,lp);
    }
    private void showMemory(){
        base("Memory Match","Find all four pairs");
        String[] vals={"👑","💎","🦁","🌹","👑","💎","🦁","🌹"};
        for(int i=vals.length-1;i>0;i--){int j=random.nextInt(i+1);String q=vals[i];vals[i]=vals[j];vals[j]=q;}
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);final int[] first={-1};final boolean[] done=new boolean[8];final Button[] bs=new Button[8];final int[] pairs={0};final boolean[] comparing={false};
        for(int r=0;r<2;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<4;c++){int k=r*4+c;Button b=action("?");b.setTextSize(26);bs[k]=b;b.setOnClickListener(v->{if(done[k]||comparing[0])return;b.setText(vals[k]);if(first[0]<0){first[0]=k;return;}int f=first[0];if(f==k)return;if(vals[f].equals(vals[k])){done[f]=done[k]=true;pairs[0]++;first[0]=-1;if(pairs[0]==4){Toast.makeText(this,"Memory cleared 👑",Toast.LENGTH_SHORT).show();KingSoundFx.win(this);}}else{comparing[0]=true;b.postDelayed(()->{bs[f].setText("?");bs[k].setText("?");first[0]=-1;comparing[0]=false;},650);}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(74),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}grid.addView(row);}page.addView(grid);
    }


    private void showReactionTap(){
        base("Reaction Tap","Tap only when GO appears");
        final TextView roundStatus=label("Wait…",28,0xffffd768,true);
        roundStatus.setGravity(Gravity.CENTER);page.addView(roundStatus,new LinearLayout.LayoutParams(-1,dp(120)));
        Button tap=action("TAP");final long[] go={0};final boolean[] ended={false};
        final Runnable signal=()->{if(!ended[0]&&!isFinishing()&&!isDestroyed()){roundStatus.setText("GO! ⚡");go[0]=android.os.SystemClock.elapsedRealtime();}};
        tap.setOnClickListener(v->{if(ended[0])return;ended[0]=true;roundStatus.removeCallbacks(signal);tap.setEnabled(false);if(go[0]==0){roundStatus.setText("Too early • try again");return;}long ms=android.os.SystemClock.elapsedRealtime()-go[0];roundStatus.setText(ms+" ms"+(ms<350?" • FAST 👑":" • Keep trying"));});
        page.addView(tap,new LinearLayout.LayoutParams(-1,dp(70)));
        Button retry=action("Try again");retry.setOnClickListener(v->{ended[0]=true;roundStatus.removeCallbacks(signal);showReactionTap();});page.addView(retry,new LinearLayout.LayoutParams(-1,dp(54)));
        roundStatus.postDelayed(signal,1200+random.nextInt(2200));
    }

    private void showHighLow(){
        base("High / Low","Guess the next card");final int[] cur={1+random.nextInt(13)};status=label("Card: "+cur[0],28,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(110)));LinearLayout row=new LinearLayout(this);Button high=action("⬆ Higher"),low=action("⬇ Lower");high.setOnClickListener(v->highLow(cur,true));low.setOnClickListener(v->highLow(cur,false));row.addView(high,new LinearLayout.LayoutParams(0,dp(60),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(60),1);lp.setMargins(dp(8),0,0,0);row.addView(low,lp);page.addView(row);
    }
    private void highLow(int[] cur,boolean higher){int next=1+random.nextInt(13);boolean win=higher?next>=cur[0]:next<=cur[0];status.setText("Next: "+next+" • "+(win?"You win 👑":"Try again"));if(win)KingSoundFx.win(this);cur[0]=next;}

    private void showWheel(){
        base("Lucky Wheel","Free game • no money");status=label("🎡 Ready",30,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(120)));Button spin=action("SPIN");spin.setOnClickListener(v->{String[] r={"🌹 Rose","⭐ Star","👑 Crown","🎉 Party","💎 Diamond","🦁 Lion"};String x=r[random.nextInt(r.length)];status.setText("🎡  "+x);KingSoundFx.win(this);});page.addView(spin,new LinearLayout.LayoutParams(-1,dp(64)));
    }

    private void showSheepFight(){
        base("Sheep Fight","Train your sheep, then battle KING Bot");
        final int[] power={45};status=label("🐑 Power: "+power[0]+"\nTrain before battle",22,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(120)));
        Button train=action("🏋 Train +5 to +15");train.setOnClickListener(v->{int gain=5+random.nextInt(11);power[0]=Math.min(100,power[0]+gain);status.setText("🐑 Power: "+power[0]+"\n+"+gain+" training");});page.addView(train,new LinearLayout.LayoutParams(-1,dp(58)));
        Button fight=action("⚔ Battle");LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(60));fp.setMargins(0,dp(8),0,0);fight.setOnClickListener(v->{int bot=45+random.nextInt(56);boolean win=power[0]>=bot;status.setText("YOU "+power[0]+"  vs  BOT "+bot+"\n"+(win?"🏆 YOU WIN":"Bot wins • train more"));if(win)KingSoundFx.win(this);power[0]=45;});page.addView(fight,fp);
    }

    private void showWerewolf(){
        base("Werewolf","Single-player deduction round");
        final String[] names={"Asha","Ravi","Mira","Kabir"};final int wolf=random.nextInt(names.length);final int[] turns={2};
        status=label("A werewolf is hiding. You have 2 investigations.",18,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(90)));
        for(int i=0;i<names.length;i++){final int k=i;Button b=action("🔍 Investigate "+names[i]);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(5),0,0);b.setOnClickListener(v->{if(turns[0]<=0)return;turns[0]--;boolean suspicious=k==wolf||random.nextBoolean();status.setText(names[k]+(suspicious?" looks suspicious 🐺":" seems calm 🙂")+"\nInvestigations left: "+turns[0]);});page.addView(b,lp);}
        Button vote=action("🗳 Vote for Werewolf");LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(58));vp.setMargins(0,dp(10),0,0);vote.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Final Vote").setItems(names,(d,w)->{boolean win=w==wolf;status.setText((win?"🏆 Correct! ":"Wrong vote • ")+names[wolf]+" was the Werewolf");if(win)KingSoundFx.win(this);}).show());page.addView(vote,vp);
    }

    private void showSpy(){
        base("Spy Game","Find the spy from secret clues");
        final String[] people={"Player 1","Player 2","Player 3","Player 4"};final int spy=random.nextInt(4);final String[] locations={"Airport","Beach","School","Cinema","Market"};final String loc=locations[random.nextInt(locations.length)];
        status=label("Location: "+loc+"\nOne player has no location clue.",18,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(100)));
        for(int i=0;i<4;i++){final int k=i;Button b=action("💬 Ask "+people[i]);b.setOnClickListener(v->{String clue=k==spy?new String[]{"Maybe crowded?","I saw people","Hard to say"}[random.nextInt(3)]:spyClue(loc);status.setText(people[k]+": \""+clue+"\"\nWho is the spy?");});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,0);page.addView(b,lp);}
        Button vote=action("🕵 Identify Spy");LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(58));vp.setMargins(0,dp(9),0,0);vote.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Choose Spy").setItems(people,(d,w)->{boolean win=w==spy;status.setText(win?"🏆 Spy found!":"Spy escaped • it was "+people[spy]);if(win)KingSoundFx.win(this);}).show());page.addView(vote,vp);
    }
    private String spyClue(String loc){if("Airport".equals(loc))return "Flights and luggage";if("Beach".equals(loc))return "Sand and waves";if("School".equals(loc))return "Books and classes";if("Cinema".equals(loc))return "Big screen and popcorn";return "Shops and bargaining";}

    private void showDrawGuess(){
        base("Draw & Guess","Draw the shown word • pass phone to a friend to guess");
        final String[] words={"Crown","Tiger","Diamond","Guitar","Rocket","Mango","Flower","House"};final String[] target={words[random.nextInt(words.length)]};
        status=label("DRAW: "+target[0],19,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(54)));
        KingDrawPadView pad=new KingDrawPadView(this);page.addView(pad,new LinearLayout.LayoutParams(-1,dp(260)));
        LinearLayout row=new LinearLayout(this);Button clear=action("Clear"),newWord=action("New Word");clear.setOnClickListener(v->pad.clear());newWord.setOnClickListener(v->{target[0]=words[random.nextInt(words.length)];status.setText("DRAW: "+target[0]);pad.clear();});row.addView(clear,new LinearLayout.LayoutParams(0,dp(54),1));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,dp(54),1);np.setMargins(dp(8),0,0,0);row.addView(newWord,np);page.addView(row);
        Button guessed=action("✅ Friend Guessed It");LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,dp(56));gp.setMargins(0,dp(8),0,0);guessed.setOnClickListener(v->{status.setText("🏆 Correct: "+target[0]);KingSoundFx.win(this);});page.addView(guessed,gp);
    }

    private void showBingo(){
        base("Bingo","Tap CALL, then mark matching numbers • complete a row");
        final int[] nums=new int[25];java.util.ArrayList<Integer> pool=new java.util.ArrayList<>();for(int i=1;i<=75;i++)pool.add(i);java.util.Collections.shuffle(pool);for(int i=0;i<25;i++)nums[i]=pool.get(i);
        final java.util.HashSet<Integer> called=new java.util.HashSet<>();final boolean[] won={false};final boolean[] marked=new boolean[25];final Button[] bs=new Button[25];status=label("Press CALL to draw a number",17,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(55)));
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);for(int r=0;r<5;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<5;c++){int k=r*5+c;Button b=action(String.valueOf(nums[k]));b.setTextSize(12);bs[k]=b;b.setOnClickListener(v->{if(won[0]||!called.contains(nums[k]))return;marked[k]=!marked[k];b.setText(marked[k]?"✓"+nums[k]:String.valueOf(nums[k]));if(bingoWin(marked)){won[0]=true;status.setText("BINGO! 🏆");KingSoundFx.win(this);}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(b,lp);}grid.addView(row);}page.addView(grid);
        final java.util.ArrayList<Integer> draws=new java.util.ArrayList<>();for(int n=1;n<=75;n++)draws.add(n);java.util.Collections.shuffle(draws);Button call=action("🎱 CALL NUMBER");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(58));cp.setMargins(0,dp(8),0,0);call.setOnClickListener(v->{if(won[0]||draws.isEmpty()){call.setEnabled(false);return;}int n=draws.remove(draws.size()-1);called.add(n);for(int k=0;k<25;k++)if(nums[k]==n)bs[k].setBackground(bg(0xff775626,12));status.setText("CALLED: "+n+" • "+called.size()+"/75 • mark highlighted numbers");if(draws.isEmpty())call.setEnabled(false);});page.addView(call,cp);
    }
    private boolean bingoWin(boolean[] m){for(int r=0;r<5;r++){boolean ok=true;for(int c=0;c<5;c++)ok&=m[r*5+c];if(ok)return true;}for(int c=0;c<5;c++){boolean ok=true;for(int r=0;r<5;r++)ok&=m[r*5+c];if(ok)return true;}boolean a=true,b=true;for(int i=0;i<5;i++){a&=m[i*5+i];b&=m[i*5+4-i];}return a||b;}

    private void showZoo(){
        base("Crazy Zoo","Explore until you collect all animals");
        final String[] animals={"🦁 Lion","🐼 Panda","🐘 Elephant","🐯 Tiger","🦊 Fox","🦚 Peacock"};final java.util.HashSet<String> found=new java.util.HashSet<>();status=label("Collection 0/6",21,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(100)));Button explore=action("🌿 Explore Zoo");explore.setOnClickListener(v->{String a=animals[random.nextInt(animals.length)];boolean fresh=found.add(a);status.setText(a+(fresh?" collected!":" already found")+"\nCollection "+found.size()+"/6");if(found.size()==animals.length){status.setText("🏆 ZOO COMPLETE!\nAll 6 animals collected");KingSoundFx.win(this);}});page.addView(explore,new LinearLayout.LayoutParams(-1,dp(64)));
    }

    private void showDomino(){
        base("Domino Match","Match the left number to the chain");
        final int[] end={random.nextInt(7)};final int[] score={0};status=label("Chain end: "+end[0]+" • Score 0",21,0xffffd768,true);status.setGravity(Gravity.CENTER);page.addView(status,new LinearLayout.LayoutParams(-1,dp(90)));
        for(int i=0;i<3;i++){Button b=action("Draw Tile");b.setOnClickListener(v->{int a=random.nextInt(7),z=random.nextInt(7);new android.app.AlertDialog.Builder(this).setTitle("Tile "+a+" | "+z).setMessage("Chain end is "+end[0]).setPositiveButton("Play",(d,w)->{if(a==end[0]||z==end[0]){end[0]=a==end[0]?z:a;score[0]++;status.setText("Matched! • Chain end: "+end[0]+" • Score "+score[0]);if(score[0]>=5){status.setText("🏆 DOMINO WIN • Score "+score[0]);KingSoundFx.win(this);}}else status.setText("No match • Chain end: "+end[0]+" • Score "+score[0]);}).setNegativeButton("Pass",null).show();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(0,dp(7),0,0);page.addView(b,lp);}
    }

}

