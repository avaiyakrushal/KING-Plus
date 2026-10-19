package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

/** Two verified KING Party members play on one revisioned Firebase chess board. */
public final class KingLiveChessActivity extends Activity {
  private static final int BG=0xff140e28, GOLD=0xffffd27d;
  private FirebaseFirestore db;private FirebaseUser me;private DocumentReference game;
  private String roomId="",whiteUid="",blackUid="",whiteName="",blackName="",board=KingChessRules.START;
  private String status="waiting",rights="KQkq",lastMove="";
  private char turn='w';private int ep=-1,selected=-1;
  private boolean verified=false,busy=false;
  private ListenerRegistration listener;
  private TextView state,players,network;private final TextView[] cells=new TextView[64];
  private LinearLayout controls;
  private int dp(int n){return(int)(getResources().getDisplayMetrics().density*n+.5f);}
  private GradientDrawable bg(int c){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(8));return d;}
  private String s(String v){return v==null?"":v;}
  private String uid(){return me==null?"":me.getUid();}
  private boolean w(){return uid().equals(whiteUid)&&!uid().isEmpty();}
  private boolean b(){return uid().equals(blackUid)&&!uid().isEmpty();}
  private boolean mine(){return verified&&"active".equals(status)&&(turn=='w'?w():b());}
  private boolean flip(){return b();}
  private boolean alive(){return !isFinishing()&&!isDestroyed();}
  private String displayName(){String n=me==null?"":s(me.getDisplayName()).trim();return n.isEmpty()?"KING Player":n.substring(0,Math.min(n.length(),45));}
  private void toast(String t){if(alive())Toast.makeText(this,t,Toast.LENGTH_SHORT).show();}
  private TextView tv(String text,int size,int color){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);return t;}
  private Button button(String name,Runnable action){Button x=new Button(this);x.setText(name);x.setAllCaps(false);x.setTextColor(Color.WHITE);x.setTextSize(13);x.setBackground(bg(0xff6242a4));x.setOnClickListener(v->{if(verified&&!busy)action.run();else toast("Live room verification is required");});return x;}
  @Override public void onCreate(Bundle state){super.onCreate(state);
    roomId=s(getIntent().getStringExtra("roomId"));
    try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
    build();
    if(db==null||me==null||roomId.isEmpty()){network.setText("Sign in and join KING Party to play Chess");return;}
    final String id=roomId,user=uid();
    db.collection("live_rooms").document(id).get(Source.SERVER).addOnSuccessListener(room->{
      if(!alive())return;
      if(!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){network.setText("Room closed or unavailable");return;}
      db.collection("live_rooms").document(id).collection("members").document(user).get(Source.SERVER).addOnSuccessListener(member->{
        if(!alive())return;
        if(!member.exists()||!user.equals(member.getString("uid"))){network.setText("Join this live Party before Chess");return;}
        verified=true;game=db.collection("live_rooms").document(id).collection("chess_games").document("main");
        network.setText("● Verified live Firebase Party • Shared Chess board");listen();refresh();
      }).addOnFailureListener(e->{if(alive())network.setText("Membership verification failed: "+e.getMessage());});
    }).addOnFailureListener(e->{if(alive())network.setText("Room verification failed: "+e.getMessage());});
  }
  private void build(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
    ScrollView scroll=new ScrollView(this);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
    page.setPadding(dp(11),dp(16),dp(11),dp(20));scroll.addView(page);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
    ViewCompat.setOnApplyWindowInsetsListener(root,(v,in)->{Insets bars=in.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return in;});
    TextView heading=tv("♟ KING Live Chess",24,GOLD);heading.setTypeface(null,Typeface.BOLD);page.addView(heading,new LinearLayout.LayoutParams(-1,dp(46)));
    network=tv("Verifying Party access on SERVER…",11,0xffc7b9d8);page.addView(network,new LinearLayout.LayoutParams(-1,dp(37)));
    players=tv("White —  vs  Black —",15,Color.WHITE);page.addView(players,new LinearLayout.LayoutParams(-1,dp(42)));
    state=tv("Waiting for players",14,GOLD);page.addView(state,new LinearLayout.LayoutParams(-1,dp(62)));
    int screenDp=(int)(getResources().getDisplayMetrics().widthPixels/Math.max(1f,getResources().getDisplayMetrics().density));
    int size=Math.max(28,Math.min(48,(screenDp-28)/8));
    GridLayout grid=new GridLayout(this);grid.setColumnCount(8);grid.setRowCount(8);
    for(int i=0;i<64;i++){
      final int visual=i;TextView cell=tv("",25,Color.BLACK);cell.setTypeface(null,Typeface.BOLD);
      GridLayout.LayoutParams lp=new GridLayout.LayoutParams(GridLayout.spec(i/8),GridLayout.spec(i%8));
      lp.width=dp(size);lp.height=dp(size);grid.addView(cell,lp);
      cell.setOnClickListener(v->tap(visual));cells[i]=cell;
    }
    LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(dp(size*8),dp(size*8));gp.gravity=Gravity.CENTER_HORIZONTAL;page.addView(grid,gp);
    TextView help=tv("Choose your piece, then destination. White moves first.\nCastling, en-passant, queen promotion and checkmate supported.",11,0xffcfc3e2);
    help.setPadding(dp(8),dp(10),dp(8),dp(10));page.addView(help,new LinearLayout.LayoutParams(-1,-2));
    controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);page.addView(controls,new LinearLayout.LayoutParams(-1,-2));
    Button back=new Button(this);back.setText("‹ Back to Party");back.setAllCaps(false);back.setOnClickListener(v->finish());
    page.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
    setContentView(root);refresh();
  }
  private void listen(){
    if(listener!=null)listener.remove();
    listener=game.addSnapshotListener((d,e)->{
      if(!alive())return;
      if(e!=null){network.setText("Chess sync error: "+e.getMessage());return;}
      if(d==null||!d.exists()){
        whiteUid="";blackUid="";whiteName="";blackName="";board=KingChessRules.START;
        status="waiting";turn='w';rights="KQkq";ep=-1;lastMove="";selected=-1;refresh();return;
      }
      String next=s(d.getString("board"));if(next.length()==64)board=next;
      whiteUid=s(d.getString("whiteUid"));blackUid=s(d.getString("blackUid"));
      whiteName=s(d.getString("whiteName"));blackName=s(d.getString("blackName"));
      status=s(d.getString("status"));rights=s(d.getString("rights"));lastMove=s(d.getString("lastMove"));
      turn="b".equals(d.getString("turn"))?'b':'w';Long ept=d.getLong("enPassant");ep=ept==null?-1:ept.intValue();
      selected=-1;refresh();
    });
  }
  private void join(boolean asWhite){
    if(!verified||busy||game==null)return;busy=true;final String id=uid(),name=displayName();
    db.runTransaction(tx->{
      DocumentSnapshot d=tx.get(game);
      if(!d.exists()){
        if(!asWhite)throw new IllegalStateException("White must create the match first");
        Map<String,Object> m=new HashMap<>();m.put("board",KingChessRules.START);
        m.put("whiteUid",id);m.put("whiteName",name);m.put("blackUid","");m.put("blackName","");
        m.put("turn","w");m.put("rights","KQkq");m.put("enPassant",-1);
        m.put("revision",0);m.put("status","waiting");m.put("lastMove","");m.put("winnerUid","");
        m.put("actorUid",id);m.put("updatedAt",FieldValue.serverTimestamp());tx.set(game,m);
      }else{
        String white=s(d.getString("whiteUid")),black=s(d.getString("blackUid"));
        if((asWhite&&id.equals(white))||(!asWhite&&id.equals(black)))return "joined";
        if(asWhite||!black.isEmpty()||id.equals(white)||!"waiting".equals(s(d.getString("status"))))
          throw new IllegalStateException("Seat already occupied");
        Long rev=d.getLong("revision");Map<String,Object> u=new HashMap<>();
        u.put("blackUid",id);u.put("blackName",name);u.put("status","active");
        u.put("actorUid",id);u.put("revision",rev==null?1:rev+1);
        u.put("updatedAt",FieldValue.serverTimestamp());tx.update(game,u);
      }return "joined";
    }).addOnSuccessListener(v->{busy=false;if(alive())toast("Connected to real Chess match");})
      .addOnFailureListener(e->{busy=false;if(alive())toast("Chess join failed: "+e.getMessage());});
  }
  private static String square(int i){return "abcdefgh".charAt(i%8)+String.valueOf(8-i/8);}
  private void tap(int visual){
    if(!mine()||busy)return;int at=flip()?63-visual:visual;
    char p=board.charAt(at);boolean own=p!='.'&&Character.isUpperCase(p)==(turn=='w');
    if(selected<0){if(own){selected=at;refresh();}else toast("Select your own piece");return;}
    if(at==selected){selected=-1;refresh();return;}
    if(own){selected=at;refresh();return;}
    final int from=selected;selected=-1;
    try{KingChessRules.play(board,turn,rights,ep,from,at);}
    catch(IllegalArgumentException ex){toast(ex.getMessage());refresh();return;}
    busy=true;final String actor=uid();
    db.runTransaction(tx->{
      DocumentSnapshot d=tx.get(game);
      if(!d.exists()||!"active".equals(d.getString("status")))throw new IllegalStateException("Match ended");
      String t=s(d.getString("turn"));
      if(!actor.equals("w".equals(t)?d.getString("whiteUid"):d.getString("blackUid")))throw new IllegalStateException("Opponent's turn");
      Long stored=d.getLong("enPassant"),rev=d.getLong("revision");
      KingChessRules.Result m=KingChessRules.play(s(d.getString("board")),t.charAt(0),
          s(d.getString("rights")),stored==null?-1:stored.intValue(),from,at);
      Map<String,Object> u=new HashMap<>();
      u.put("board",m.board);u.put("turn",String.valueOf(m.next));u.put("rights",m.rights);
      u.put("enPassant",m.enPassant);u.put("status",m.status);
      u.put("winnerUid","checkmate".equals(m.status)?actor:"");
      u.put("actorUid",actor);u.put("lastMove",square(from)+" → "+square(at));
      u.put("revision",rev==null?1:rev+1);u.put("updatedAt",FieldValue.serverTimestamp());
      tx.update(game,u);return null;
    }).addOnSuccessListener(v->{busy=false;}).addOnFailureListener(e->{busy=false;if(alive())toast("Move rejected: "+e.getMessage());});
  }
  private void resign(){
    if(!"active".equals(status)||!(w()||b())||busy)return;
    new AlertDialog.Builder(this).setTitle("Resign Chess?").setMessage("The game ends for both players.")
      .setNegativeButton("Cancel",null).setPositiveButton("Resign",(d,x)->{
        busy=true;final String actor=uid();
        db.runTransaction(tx->{
          DocumentSnapshot snap=tx.get(game);
          if(!"active".equals(snap.getString("status"))||!actor.equals(snap.getString("whiteUid"))&&!actor.equals(snap.getString("blackUid")))throw new IllegalStateException("Match ended");
          Long rev=snap.getLong("revision");Map<String,Object> data=new HashMap<>();
          data.put("status","resigned");data.put("winnerUid",actor.equals(snap.getString("whiteUid"))?snap.getString("blackUid"):snap.getString("whiteUid"));
          data.put("lastMove","Resigned");data.put("actorUid",actor);data.put("revision",rev==null?1:rev+1);
          data.put("updatedAt",FieldValue.serverTimestamp());tx.update(game,data);return null;
        }).addOnSuccessListener(v->{busy=false;}).addOnFailureListener(e->{busy=false;toast("Resign rejected: "+e.getMessage());});
      }).show();
  }
  private void rematch(){
    if(!w()||"active".equals(status)||"waiting".equals(status)||blackUid.isEmpty()||busy)return;busy=true;final String actor=uid();
    db.runTransaction(tx->{
      DocumentSnapshot d=tx.get(game);
      if(!d.exists()||!actor.equals(d.getString("whiteUid"))||"active".equals(d.getString("status")))throw new IllegalStateException("Rematch unavailable");
      Long rev=d.getLong("revision");Map<String,Object> u=new HashMap<>();
      u.put("board",KingChessRules.START);u.put("turn","w");u.put("rights","KQkq");u.put("enPassant",-1);
      u.put("status","active");u.put("winnerUid","");u.put("lastMove","");
      u.put("actorUid",actor);u.put("revision",rev==null?1:rev+1);u.put("updatedAt",FieldValue.serverTimestamp());
      tx.update(game,u);return null;
    }).addOnSuccessListener(v->{busy=false;}).addOnFailureListener(e->{busy=false;toast("Rematch rejected: "+e.getMessage());});
  }
  private static String glyph(char c){
    switch(c){
      case 'K':return "♔";case 'Q':return "♕";case 'R':return "♖";case 'B':return "♗";case 'N':return "♘";case 'P':return "♙";
      case 'k':return "♚";case 'q':return "♛";case 'r':return "♜";case 'b':return "♝";case 'n':return "♞";case 'p':return "♟";
      default:return "";
    }
  }
  private void refresh(){
    if(cells[0]==null)return;
    players.setText("♔ "+(whiteName.isEmpty()?"White —":whiteName)+"    vs    ♚ "+(blackName.isEmpty()?"Black —":blackName));
    if(!verified)state.setText("Waiting for live room verification");
    else if("waiting".equals(status))state.setText("Waiting for Black to join");
    else if("active".equals(status))state.setText((mine()?"● YOUR TURN":"● Opponent's turn")+" • "+(turn=='w'?"WHITE":"BLACK")+
        (KingChessRules.inCheck(board,turn)?" • CHECK!":"")+(lastMove.isEmpty()?"":"\nLast: "+lastMove));
    else state.setText("🏁 "+status.toUpperCase(Locale.US)+(lastMove.isEmpty()?"":" • "+lastMove));
    for(int i=0;i<64;i++){
      int at=flip()?63-i:i;char p=board.charAt(at);TextView cell=cells[i];
      cell.setText(glyph(p));GradientDrawable shade=bg(((at/8+at%8)%2==0)?0xfff0dcb4:0xff907196);
      if(selected==at)shade.setStroke(dp(3),GOLD);
      cell.setBackground(shade);cell.setTextColor(Character.isUpperCase(p)?0xfffafaf2:0xff171529);
      cell.setContentDescription(square(at)+" "+(p=='.'?"empty":String.valueOf(p))+(selected==at?" selected":""));
    }
    controls.removeAllViews();if(!verified)return;
    if(whiteUid.isEmpty())controls.addView(button("♔ Create / Play White",()->join(true)),new LinearLayout.LayoutParams(-1,dp(51)));
    else if(blackUid.isEmpty()&&!w())controls.addView(button("♚ Join as Black",()->join(false)),new LinearLayout.LayoutParams(-1,dp(51)));
    if("active".equals(status)&&(w()||b()))controls.addView(button("Resign match",this::resign),new LinearLayout.LayoutParams(-1,dp(43)));
    else if(!"active".equals(status)&&!"waiting".equals(status)&&w())controls.addView(button("♟ Rematch",this::rematch),new LinearLayout.LayoutParams(-1,dp(52)));
  }
  @Override protected void onDestroy(){if(listener!=null)listener.remove();super.onDestroy();}
}