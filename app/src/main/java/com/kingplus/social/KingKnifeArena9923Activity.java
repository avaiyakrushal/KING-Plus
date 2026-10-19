package com.kingplus.social;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

/**
 * Original KING Knife Arena, inspired by the *genre* of the Flying Knife
 * reference. A real interactive rotating-wheel skill game with a live room
 * leaderboard. No assets or proprietary source imported from Bolo Hi.
 */
public final class KingKnifeArena9923Activity extends Activity {
  private static final int DARK=0xff17102b,GOLD=0xffffd079;
  private FirebaseFirestore db;
  private FirebaseUser user;
  private ListenerRegistration leaderboardListener;
  private ListenerRegistration membershipListener9939;
  private String roomId="";
  private DocumentReference scoreDoc;
  private boolean verified=false,busy=false,running=false;
  private long startedAt=0L;
  private int landed=0;
  private float speed=1.95f;
  private final ArrayList<Float> attachedKnives=new ArrayList<>();
  private TextView connection,status,board;
  private Button startButton;
  private WheelView wheel;
  private int dp(int x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
  private boolean alive(){return !isFinishing()&&!isDestroyed();}
  private String safe(String x){return x==null?"":x.trim();}
  private void toast(String x){if(alive())android.widget.Toast.makeText(this,x,android.widget.Toast.LENGTH_SHORT).show();}
  private TextView label(String x,int size,int color){
    TextView t=new TextView(this);t.setText(x);t.setTextSize(size);t.setTextColor(color);
    t.setGravity(Gravity.CENTER);return t;
  }
  private GradientDrawable surface(int color){
    GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(16));return bg;
  }
  @Override public void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    roomId=safe(getIntent().getStringExtra("roomId"));
    try {db=FirebaseFirestore.getInstance();user=FirebaseAuth.getInstance().getCurrentUser();}
    catch(Exception ignored){}
    layout();
    if(db==null||user==null||roomId.isEmpty()){
      connection.setText("Sign in and join a real KING Party to play.");return;
    }
    final String currentUid=user.getUid(),currentRoom=roomId;
    db.collection("live_rooms").document(currentRoom).get(Source.SERVER).addOnSuccessListener(r->{
      if(!alive())return;
      if(!r.exists()||Boolean.TRUE.equals(r.getBoolean("closed"))){
        connection.setText("This Party is closed or unavailable");return;
      }
      db.collection("live_rooms").document(currentRoom).collection("members")
        .document(currentUid).get(Source.SERVER).addOnSuccessListener(m->{
          if(!alive())return;
          if(!m.exists()||!currentUid.equals(m.getString("uid"))){
            connection.setText("You must be a joined Party member");return;
          }
          verified=true;
          scoreDoc=db.collection("live_rooms").document(currentRoom)
            .collection("knife_duel_scores").document(currentUid);
          connection.setText("● Connected to Party • Live score leaderboard");
          startButton.setEnabled(true);
          listenBoard();
          watchLiveMembership9939(currentUid,currentRoom);
        }).addOnFailureListener(e->{if(alive())connection.setText("Membership check failed: "+e.getMessage());});
    }).addOnFailureListener(e->{if(alive())connection.setText("Room connection failed: "+e.getMessage());});
  }
  private void watchLiveMembership9939(String uid,String currentRoom){
    if(membershipListener9939!=null)membershipListener9939.remove();
    membershipListener9939=db.collection("live_rooms").document(currentRoom)
      .collection("members").document(uid)
      .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(member,error)->{
        if(!alive())return;
        boolean joined=error==null&&member!=null&&member.exists()
          &&!member.getMetadata().isFromCache()
          &&uid.equals(member.getString("uid"));
        if(!joined){
          verified=false;running=false;
          startButton.setEnabled(false);
          if(leaderboardListener!=null){leaderboardListener.remove();leaderboardListener=null;}
          connection.setText("● Party membership lost • game paused");
          status.setText("Rejoin the Party Room to play and submit Knife score");
        }else if(!verified){
          verified=true;
          connection.setText("● Live membership verified • Knife game ready");
          startButton.setEnabled(!busy);
          listenBoard();
        }
      });
  }
  private void layout(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(DARK);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
    LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
    page.setPadding(dp(12),dp(20),dp(12),dp(30));scroll.addView(page);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
    TextView heading=label("🔪 KING • Flying Knife Arena",22,GOLD);
    heading.setTypeface(null,android.graphics.Typeface.BOLD);
    page.addView(heading,new LinearLayout.LayoutParams(-1,dp(46)));
    connection=label("Verifying LIVE Party access...",12,0xffc6badd);
    page.addView(connection,new LinearLayout.LayoutParams(-1,dp(50)));
    status=label("Tap Start, then touch the rotating wheel to throw.\nAvoid the knives already stuck in it.",15,Color.WHITE);
    page.addView(status,new LinearLayout.LayoutParams(-1,dp(63)));
    wheel=new WheelView();wheel.setBackground(surface(0xff2a2145));
    int screenDp=(int)(getResources().getDisplayMetrics().heightPixels/
      Math.max(1f,getResources().getDisplayMetrics().density));
    int height=Math.max(230,Math.min(355,screenDp-360));
    page.addView(wheel,new LinearLayout.LayoutParams(-1,dp(height)));
    startButton=new Button(this);startButton.setText("▶ START • 7 KNIVES");
    startButton.setAllCaps(false);startButton.setTextColor(Color.WHITE);
    startButton.setBackground(surface(0xff7651c5));startButton.setEnabled(false);
    LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(51));
    sp.setMargins(0,dp(10),0,dp(6));page.addView(startButton,sp);
    startButton.setOnClickListener(v->begin());
    TextView info=label("Live competition: your best score is shared with other\nverified members of this Party Room.",12,0xffcbc1df);
    page.addView(info,new LinearLayout.LayoutParams(-1,dp(57)));
    board=label("🏆 Room Leaderboard\nNo results yet",14,GOLD);
    board.setPadding(dp(12),dp(12),dp(12),dp(12));board.setBackground(surface(0xff292046));
    page.addView(board,new LinearLayout.LayoutParams(-1,-2));
    Button back=new Button(this);back.setAllCaps(false);back.setText("‹ Back to Party");
    back.setOnClickListener(v->finish());
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(48));bp.setMargins(0,dp(12),0,0);
    page.addView(back,bp);
    setContentView(root);
    getWindow().setStatusBarColor(DARK);getWindow().setNavigationBarColor(DARK);
  }
  private void begin(){
    if(!verified||busy){toast("Live Party verification is required");return;}
    landed=0;attachedKnives.clear();running=true;
    startedAt=SystemClock.elapsedRealtime();
    status.setText("0 / 7 knives landed • tap to throw!");
    startButton.setText("↻ RESTART ROUND");
    wheel.invalidate();
  }
  private float rotation(){
    if(startedAt==0)return 0f;
    // Alternating speed by fixed level gives all accounts the same mechanics.
    return ((SystemClock.elapsedRealtime()-startedAt)/1000f)*speed;
  }
  private void throwKnife(){
    if(!running||!verified)return;
    float local=KingKnifeRules9923.localAngle(rotation());
    if(KingKnifeRules9923.hitsBlade(attachedKnives,local)){
      running=false;
      status.setText("💥 Hit another knife!\nYou landed "+landed+" of 7.");
      saveScore(landed);
    }else{
      attachedKnives.add(local);landed++;
      if(KingKnifeRules9923.roundCleared(landed)){
        running=false;
        status.setText("🏆 PERFECT! 7 of 7 knives landed.\nScore synced to KING Party.");
        saveScore(landed);
      }else status.setText(landed+" / 7 landed • "+(7-landed)+" left! Tap carefully.");
    }
    wheel.invalidate();
  }
  private void saveScore(final int finalScore){
    if(!verified||scoreDoc==null)return;
    busy=true;startButton.setEnabled(false);
    String n=safe(user.getDisplayName());if(n.isEmpty())n="KING Player";
    final String username=n.length()>40?n.substring(0,40):n;
    final String uid=user.getUid();
    db.runTransaction(tx->{
      DocumentSnapshot prior=tx.get(scoreDoc);
      Long oldBest=prior.getLong("bestScore"),oldPlays=prior.getLong("plays");
      int prevBest=oldBest==null?0:oldBest.intValue();
      long plays=oldPlays==null?1:oldPlays+1;
      Map<String,Object> data=new HashMap<>();
      data.put("uid",uid);data.put("name",username);
      data.put("bestScore",Math.max(finalScore,prevBest));
      data.put("lastScore",finalScore);data.put("plays",plays);
      data.put("updatedAt",FieldValue.serverTimestamp());
      tx.set(scoreDoc,data);
      return Math.max(finalScore,prevBest);
    }).addOnSuccessListener(best->{
      busy=false;if(!alive())return;startButton.setEnabled(verified);
      toast("Live Party high score saved: "+best+" / 7");
    }).addOnFailureListener(e->{
      busy=false;if(!alive())return;startButton.setEnabled(verified);
      status.setText("Round ended • "+finalScore+"/7\nScore sync failed: "+e.getMessage());
    });
  }
  private void listenBoard(){
    leaderboardListener=db.collection("live_rooms").document(roomId)
      .collection("knife_duel_scores").orderBy("bestScore",Query.Direction.DESCENDING)
      .limit(12).addSnapshotListener((snap,error)->{
        if(!alive())return;
        if(error!=null){board.setText("Leaderboard unavailable: "+error.getMessage());return;}
        StringBuilder data=new StringBuilder("🏆 LIVE PARTY • KNIFE LEADERBOARD\n\n");
        int rank=1;
        if(snap!=null)for(DocumentSnapshot doc:snap.getDocuments()){
          String name=safe(doc.getString("name"));
          Long best=doc.getLong("bestScore"),plays=doc.getLong("plays");
          data.append(rank++).append(". ").append(name.isEmpty()?"KING Player":name)
             .append("     ").append(best==null?0:best).append(" / 7")
             .append("  (").append(plays==null?0:plays).append(" tries)\n");
        }
        if(rank==1)data.append("Join the first real Party Knife round!");
        board.setText(data.toString());
      });
  }
  final class WheelView extends View {
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    WheelView(){super(KingKnifeArena9923Activity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    @Override protected void onDraw(Canvas canvas){
      super.onDraw(canvas);
      float w=getWidth(),h=getHeight(),cx=w/2f,cy=h*0.46f;
      float radius=Math.min(w*.32f,h*.29f);
      paint.setStyle(Paint.Style.FILL);paint.setColor(0xff4e386e);
      canvas.drawCircle(cx,cy,radius+dp(11),paint);
      paint.setColor(0xffb78643);canvas.drawCircle(cx,cy,radius,paint);
      paint.setColor(0xff81562d);canvas.drawCircle(cx,cy,radius*.79f,paint);
      paint.setColor(GOLD);canvas.drawCircle(cx,cy,radius*.18f,paint);
      float rot=rotation();
      // Rotate visual seams and every successfully inserted knife together.
      paint.setColor(0xffdbb782);paint.setStrokeWidth(dp(3));
      for(int i=0;i<8;i++){
        float a=(float)(rot+i*Math.PI/4);
        canvas.drawLine(cx+(float)Math.cos(a)*radius*.5f,cy+(float)Math.sin(a)*radius*.5f,
          cx+(float)Math.cos(a)*radius*.94f,cy+(float)Math.sin(a)*radius*.94f,paint);
      }
      for(float local:attachedKnives){
        float a=local+rot;
        drawBlade(canvas,cx,cy,radius,a,0xffe6e8f6);
      }
      // Next knife hovers at the six-o'clock throw path.
      paint.setColor(0xff8c67c9);paint.setStrokeWidth(dp(5));
      canvas.drawLine(cx,cy+radius+dp(22),cx,cy+radius+dp(57),paint);
      paint.setColor(Color.WHITE);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(dp(14));
      canvas.drawText(running?"TAP TO THROW":verified?"PRESS START":"VERIFYING ROOM",cx,h-dp(18),paint);
      if(running&&getWindowToken()!=null)postInvalidateDelayed(16);
    }
    private void drawBlade(Canvas c,float cx,float cy,float radius,float angle,int color){
      float u=(float)Math.cos(angle),v=(float)Math.sin(angle);
      paint.setColor(color);paint.setStrokeWidth(dp(5));paint.setStrokeCap(Paint.Cap.ROUND);
      c.drawLine(cx+u*(radius-dp(5)),cy+v*(radius-dp(5)),
          cx+u*(radius+dp(36)),cy+v*(radius+dp(36)),paint);
      paint.setColor(GOLD);paint.setStrokeWidth(dp(9));
      c.drawLine(cx+u*(radius+dp(26)),cy+v*(radius+dp(26)),
          cx+u*(radius+dp(39)),cy+v*(radius+dp(39)),paint);
    }
    @Override public boolean onTouchEvent(MotionEvent e){
      if(e.getAction()==MotionEvent.ACTION_DOWN){throwKnife();return true;}
      return true;
    }
  }
  @Override protected void onDestroy(){
    running=false;
    if(leaderboardListener!=null)leaderboardListener.remove();
    if(membershipListener9939!=null)membershipListener9939.remove();
    super.onDestroy();
  }
}