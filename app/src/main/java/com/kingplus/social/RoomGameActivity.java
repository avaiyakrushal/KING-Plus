package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomGameActivity extends Activity {
    private static final int BG=0xff1e1333, CARD=0xff34234f, PURPLE=0xff8a49ed, GOLD=0xffffd768, MUTED=0xffcfc4df;
    private FirebaseFirestore db; private FirebaseUser me;
    private String roomId="",roomName="Live Room",displayName="KING User";
    private boolean moderator;
    private long roundId; private String type="",prompt="",result="",status="closed";
    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener;
    private LinearLayout page,controls,movesBox,readyBox; private TextView stateText,roleText;
    private final List<DocumentSnapshot> currentMoves=new ArrayList<>();

    @Override public void onCreate(Bundle b){super.onCreate(b);try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception e){db=null;me=null;}
        roomId=s(getIntent().getStringExtra("roomId"));roomName=s(getIntent().getStringExtra("roomName"));displayName=s(getIntent().getStringExtra("displayName"));if(roomName.isEmpty())roomName="Live Room";if(displayName.isEmpty())displayName="KING User";
        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;} loadRoleAndListen(); }
    @Override protected void onDestroy(){
        if(me!=null&&db!=null&&!roomId.isEmpty())try{room().collection("game_ready").document(me.getUid()).delete();}catch(Exception ignored){}
        if(stateListener!=null)stateListener.remove();if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();if(moderatorRoleListener!=null)moderatorRoleListener.remove();super.onDestroy();}

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);} private String s(String x){return x==null?"":x.trim();}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String x,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(x);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);v.setPadding(dp(12),dp(8),dp(12),dp(8));return v;}
    private Button button(String x){Button b=new Button(this);b.setText(x);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackground(bg(PURPLE,16));return b;}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}

    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);ScrollView sc=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(12),dp(16),dp(28));sc.addView(page);root.addView(sc,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));LinearLayout hi=new LinearLayout(this);hi.setOrientation(LinearLayout.VERTICAL);hi.addView(tv("🎮 Room Multiplayer",23,Color.WHITE,true));hi.addView(tv(roomName,12,MUTED,false));head.addView(hi,new LinearLayout.LayoutParams(0,dp(62),1));page.addView(head);
        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);
        stateText=tv("Waiting for active round",18,GOLD,true);stateText.setGravity(Gravity.CENTER);stateText.setBackground(bg(CARD,18));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,dp(10));page.addView(stateText,sp);
        controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);page.addView(controls,new LinearLayout.LayoutParams(-1,-2));
        TextView readyTitle=tv("Players ready",16,Color.WHITE,true);readyTitle.setPadding(dp(2),dp(14),0,dp(4));page.addView(readyTitle);readyBox=new LinearLayout(this);readyBox.setOrientation(LinearLayout.VERTICAL);page.addView(readyBox,new LinearLayout.LayoutParams(-1,-2));
        TextView live=tv("Live players / moves",16,Color.WHITE,true);live.setPadding(dp(2),dp(14),0,dp(4));page.addView(live);movesBox=new LinearLayout(this);movesBox.setOrientation(LinearLayout.VERTICAL);page.addView(movesBox,new LinearLayout.LayoutParams(-1,-2));
        setSafe(root); }
    private void setSafe(View root){setContentView(root);ViewCompat.setOnApplyWindowInsetsListener(root,(v,in)->{Insets z=in.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(z.left,z.top,z.right,z.bottom);return in;});ViewCompat.requestApplyInsets(root);}

    private DocumentReference room(){return db.collection("live_rooms").document(roomId);} private DocumentReference state(){return room().collection("game_state").document("current");}
    private void loadRoleAndListen(){room().get().addOnSuccessListener(d->{String owner=s(d.getString("ownerUid"));if(me.getUid().equals(owner)){moderator=true;finishRole();}else{if(moderatorRoleListener!=null)moderatorRoleListener.remove();moderatorRoleListener=room().collection("roles").document(me.getUid()).addSnapshotListener((r,e)->{moderator=e==null&&r!=null&&r.exists()&&"cohost".equals(r.getString("role"));finishRole();});}}).addOnFailureListener(e->{roleText.setText("Room unavailable • reopen from Party");});}
    private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");attach();listenReady750();renderControls();}
    private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener((d,e)->{if(e!=null){stateText.setText("Game state error: "+e.getMessage());return;}if(d==null||!d.exists()){roundId=0;type="";prompt="";result="";status="closed";}else{Long r=d.getLong("roundId");roundId=r==null?0:r;type=s(d.getString("type"));prompt=s(d.getString("prompt"));result=s(d.getString("result"));status=s(d.getString("status"));}renderState();listenMoves();renderControls();});}
    private void listenMoves(){if(movesListener!=null)movesListener.remove();movesListener=room().collection("game_moves").addSnapshotListener((q,e)->{currentMoves.clear();if(e!=null){movesBox.removeAllViews();movesBox.addView(tv("Unable to load moves: "+e.getMessage(),13,MUTED,false));return;}if(q!=null)for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r!=null&&r==roundId)currentMoves.add(d);}renderMoves();renderControls();});}
    private void renderState(){if(roundId==0||!"active".equals(status)){stateText.setText(result.isEmpty()?"No active multiplayer round":"Last result\n"+result);return;}stateText.setText(gameIcon(type)+"  "+prompt+(result.isEmpty()?"":"\n"+result));}
    private String gameIcon(String t){if("rps".equals(t))return "✊";if("dice".equals(t))return "🎲";if("number".equals(t))return "🔢";if("coin".equals(t))return "🪙";if("wheel".equals(t))return "🎡";if("bingo".equals(t))return "🎯";return "🎮";}

    private boolean hasMyMove(){if(me==null)return false;for(DocumentSnapshot d:currentMoves)if(me.getUid().equals(d.getString("uid")))return true;return false;}

    private void renderControls(){
        controls.removeAllViews();
        Button ready=button("✅ Ready / Not Ready");ready.setOnClickListener(v->toggleReady750());controls.addView(ready,new LinearLayout.LayoutParams(-1,dp(50)));
        Button ludo=button("🎲 Online Ludo");ludo.setOnClickListener(v->startActivity(new Intent(this,OnlineLudoActivity.class)));LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);
        if(moderator){
            TextView h=tv("Start a synchronized room round",15,Color.WHITE,true);controls.addView(h);
            String[] n={"✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Number Pick 1–9"};String[] t={"rps","dice","number","coin","wheel","bingo"};
            for(int base=0;base<n.length;base+=3){LinearLayout row=new LinearLayout(this);for(int j=0;j<3&&base+j<n.length;j++){int i=base+j;final String gt=t[i];Button b=button(n[i]);b.setEnabled(!"active".equals(status));b.setOnClickListener(v->startRound(gt));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(b,lp);}controls.addView(row);}
            if(roundId>0&&!"active".equals(status)){LinearLayout rr=new LinearLayout(this);Button rem=button("↻ Rematch");rem.setOnClickListener(v->rematch750());rr.addView(rem,new LinearLayout.LayoutParams(0,dp(50),1));Button reset=button("Clear Ready");reset.setOnClickListener(v->clearReady750());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(50),1);rp.setMargins(dp(6),0,0,0);rr.addView(reset,rp);controls.addView(rr);}
        }
        if(roundId>0&&"active".equals(status)){
            TextView h=tv(hasMyMove()?"Your move is locked ✓":"Your move",15,Color.WHITE,true);controls.addView(h);
            if(!hasMyMove()){if("rps".equals(type))rpsControls();else if("dice".equals(type))diceControls();else if("number".equals(type))numberControls();else if("coin".equals(type))coinControls();else if("wheel".equals(type))wheelControls();else if("bingo".equals(type))bingoControls();}
            else controls.addView(tv("Submitted for this round • wait for the host/co-host to finish",13,MUTED,false));
            if(moderator){Button finish=button("🏁 Finish round & show result");finish.setOnClickListener(v->finishRound());LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54));fp.setMargins(0,dp(8),0,0);controls.addView(finish,fp);}
        }
    }
    private void rpsControls(){LinearLayout row=new LinearLayout(this);String[] m={"Rock","Paper","Scissors"},e={"✊","✋","✌"};for(int i=0;i<3;i++){final String x=m[i];Button b=button(e[i]+" "+x);b.setOnClickListener(v->submit(x,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(b,lp);}controls.addView(row);}
    private void diceControls(){Button b=button("🎲 Roll my dice");b.setOnClickListener(v->{int n=1+(int)(Math.random()*6);submit("Rolled "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}
    private void numberControls(){LinearLayout row=new LinearLayout(this);EditText in=new EditText(this);in.setHint("1–30");in.setTextColor(Color.WHITE);in.setHintTextColor(0xff9e94ad);in.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);in.setSingleLine(true);in.setBackground(bg(CARD,14));in.setPadding(dp(14),0,dp(14),0);row.addView(in,new LinearLayout.LayoutParams(0,dp(52),1));Button b=button("Submit");b.setOnClickListener(v->{try{int n=Integer.parseInt(in.getText().toString().trim());if(n<1||n>30){toast("Choose 1–30");return;}submit(String.valueOf(n),n);}catch(Exception e){toast("Choose 1–30");}});LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(100),dp(52));bp.setMargins(dp(8),0,0,0);row.addView(b,bp);controls.addView(row);}
    private void coinControls(){LinearLayout row=new LinearLayout(this);Button h=button("🪙 Heads");h.setOnClickListener(v->submit("Heads",1));Button t=button("🪙 Tails");t.setOnClickListener(v->submit("Tails",0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(h,lp);row.addView(t,lp);controls.addView(row);}
    private void wheelControls(){Button b=button("🎡 Spin my wheel");b.setOnClickListener(v->{int n=1+(int)(Math.random()*100);submit("Spin "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}
    private void bingoControls(){LinearLayout row=new LinearLayout(this);for(int n=1;n<=9;n++){if(n==4||n==7){controls.addView(row);row=new LinearLayout(this);}final int x=n;Button b=button(String.valueOf(n));b.setOnClickListener(v->submit("Pick "+x,x));row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}controls.addView(row);}



    private void startRound(String game){
        if(!moderator||!"closed".equals(status)){toast("Finish the current round first");return;}
        room().collection("game_ready").get().addOnSuccessListener(q->{
            if(q.size()<2){toast("At least two players must tap Ready");return;}
            if(q.size()>12){toast("This round supports up to 12 ready players");return;}
            startRoundNow750(game);
        }).addOnFailureListener(e->toast("Ready check failed: "+e.getMessage()));
    }
    private void startRoundNow750(String game){
        if(!moderator)return;
        final long previous=roundId;
        room().collection("game_moves").get().addOnSuccessListener(old->{
            long next=Math.max(System.currentTimeMillis(),previous+1);
            String p="rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"Pick 1–9 • closest to a shared draw wins";
            Map<String,Object> value=new HashMap<>();
            value.put("actorUid",me.getUid());value.put("actorName",displayName);value.put("type",game);value.put("prompt",p);value.put("result","");value.put("status","active");value.put("roundId",next);value.put("updatedAt",FieldValue.serverTimestamp());
            db.runTransaction(tx->{
                DocumentSnapshot current=tx.get(state());
                if(current.exists()&&("active".equals(current.getString("status"))||current.getLong("roundId")!=previous))throw new IllegalStateException("Round changed. Reopen the controls.");
                for(DocumentSnapshot move:old.getDocuments())tx.delete(move.getReference());
                tx.set(state(),value);return null;
            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
              .addOnFailureListener(e->toast("Start failed: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Round preparation failed: "+e.getMessage()));
    }
    private void postGameEvent750(String text){Map<String,Object>e=new HashMap<>();e.put("actorUid",me.getUid());e.put("actorName",displayName);e.put("type","play");e.put("text",text);e.put("createdAt",FieldValue.serverTimestamp());room().collection("events").add(e);}
    private void clearOldMoves(long keep){room().collection("game_moves").get().addOnSuccessListener(q->{for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r==null||r!=keep)d.getReference().delete();}});}

    private void submit(String move,int score){
        if(roundId==0||!"active".equals(status)||me==null||!KingNetwork.online(this)){toast("An active round and internet connection are required");return;}
        final long expected=roundId;final String expectedType=type;
        DocumentReference mine=room().collection("game_moves").document(me.getUid());
        db.runTransaction(tx->{
            DocumentSnapshot active=tx.get(state()),existing=tx.get(mine);
            if(!active.exists()||!"active".equals(active.getString("status"))||active.getLong("roundId")!=expected||!expectedType.equals(active.getString("type")))throw new IllegalStateException("Round changed");
            if(existing.exists())throw new IllegalStateException("Your move is already locked");
            Map<String,Object> d=new HashMap<>();d.put("uid",me.getUid());d.put("name",displayName.length()>80?displayName.substring(0,80):displayName);d.put("roundId",expected);d.put("type",expectedType);d.put("move",move);d.put("score",score);d.put("updatedAt",FieldValue.serverTimestamp());
            tx.set(mine,d);return null;
        }).addOnSuccessListener(v->toast("Move locked ✓")).addOnFailureListener(e->toast("Move failed: "+e.getMessage()));
    }

    private void listenReady750(){
        if(readyListener!=null)readyListener.remove();
        readyListener=room().collection("game_ready").addSnapshotListener((q,e)->{
            readyBox.removeAllViews();
            if(e!=null){readyBox.addView(tv("Ready list unavailable",13,MUTED,false));return;}
            List<DocumentSnapshot> docs=q==null?new ArrayList<>():new ArrayList<>(q.getDocuments());
            Collections.sort(docs,(a,b)->s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));
            if(docs.isEmpty()){readyBox.addView(tv("Nobody is ready yet",13,MUTED,false));return;}
            for(DocumentSnapshot d:docs){String name=s(d.getString("name"));TextView row=tv("✅ "+(name.isEmpty()?"Player":name),14,Color.WHITE,true);row.setBackground(bg(CARD,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(44));lp.setMargins(0,dp(3),0,dp(3));readyBox.addView(row,lp);}
        });
    }
    private void toggleReady750(){
        DocumentReference r=room().collection("game_ready").document(me.getUid());
        r.get().addOnSuccessListener(d->{
            if(d.exists())r.delete().addOnSuccessListener(v->toast("Not ready"));
            else{Map<String,Object> m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("ready",true);m.put("updatedAt",FieldValue.serverTimestamp());r.set(m).addOnSuccessListener(v->toast("Ready ✓")).addOnFailureListener(e->toast("Ready failed: "+e.getMessage()));}
        }).addOnFailureListener(e->toast("Ready failed: "+e.getMessage()));
    }
    private void clearReady750(){
        if(!moderator)return;
        room().collection("game_ready").get().addOnSuccessListener(q->{for(DocumentSnapshot d:q.getDocuments())d.getReference().delete();toast("Ready list cleared");});
    }
    private void rematch750(){
        if(!moderator)return;
        String game=type.isEmpty()?"rps":type;
        startRound(game);
    }

    private void renderMoves(){movesBox.removeAllViews();if(currentMoves.isEmpty()){movesBox.addView(tv("Waiting for players…",13,MUTED,false));return;}List<DocumentSnapshot> sorted=new ArrayList<>(currentMoves);Collections.sort(sorted,(a,b)->s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));for(DocumentSnapshot d:sorted){String name=s(d.getString("name")),move=s(d.getString("move"));TextView row=tv("👤 "+(name.isEmpty()?"Player":name)+"  •  "+("rps".equals(type)?"move locked ✓":move),14,Color.WHITE,true);row.setBackground(bg(CARD,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));lp.setMargins(0,dp(3),0,dp(3));movesBox.addView(row,lp);}}
    private void finishRound(){if(!moderator||roundId==0||!"active".equals(status))return;final long expected=roundId;room().collection("game_moves").get().addOnSuccessListener(q->{List<DocumentSnapshot> a=new ArrayList<>();for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r!=null&&r==roundId)a.add(d);}if(expected!=roundId||!"active".equals(status)){toast("Round changed");return;}if(a.size()<2){toast("At least two players must submit");return;}String out=resultFor(a);if(out.length()>300)out=out.substring(0,297)+"...";final String finalResult=out;Map<String,Object>u=new HashMap<>();u.put("actorUid",me.getUid());u.put("result",out);u.put("status","closed");u.put("updatedAt",FieldValue.serverTimestamp());db.runTransaction(tx->{DocumentSnapshot latest=tx.get(state());if(!latest.exists()||latest.getLong("roundId")!=expected||!"active".equals(latest.getString("status")))throw new IllegalStateException("Round already finished");tx.update(state(),u);return null;}).addOnSuccessListener(v->{postGameEvent750("🏁 "+finalResult.replace("\n"," • "));new AlertDialog.Builder(this).setTitle("🏁 Round result").setMessage(finalResult).setPositiveButton("Rematch",(d,w)->rematch750()).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Finish failed: "+e.getMessage()));}).addOnFailureListener(e->toast("Could not load round moves: "+e.getMessage()));}
    private String resultFor(List<DocumentSnapshot>a){if("dice".equals(type)||"wheel".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name")));}else if(v==best)w.add(s(d.getString("name")));}return ("wheel".equals(type)?"🎡 Highest spin: ":"🎲 Highest roll: ")+best+"\n🏆 "+join(w);}if("number".equals(type)||"bingo".equals(type)){int cap="bingo".equals(type)?9:30,target=1+new java.security.SecureRandom().nextInt(cap),best=999;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue(),diff=Math.abs(v-target);if(diff<best){best=diff;w.clear();w.add(s(d.getString("name"))+" ("+v+")");}else if(diff==best)w.add(s(d.getString("name"))+" ("+v+")");}return ("bingo".equals(type)?"🎯 Room number: ":"🔢 Winning number: ")+target+"\n🏆 "+join(w);}if("coin".equals(type)){String win=new java.security.SecureRandom().nextBoolean()?"Heads":"Tails";List<String>w=new ArrayList<>();for(DocumentSnapshot d:a)if(win.equals(s(d.getString("move"))))w.add(s(d.getString("name")));return "🪙 "+win+" wins\n🏆 "+join(w);}Set<String>moves=new HashSet<>();Map<String,List<String>>names=new HashMap<>();for(DocumentSnapshot d:a){String m=s(d.getString("move")),n=s(d.getString("name"));moves.add(m);if(!names.containsKey(m))names.put(m,new ArrayList<>());names.get(m).add(n);}if(moves.size()==1||moves.size()==3)return "🤝 Draw • "+moves.toString();String win=moves.contains("Rock")&&moves.contains("Scissors")?"Rock":moves.contains("Scissors")&&moves.contains("Paper")?"Scissors":"Paper";return "🏆 "+win+" wins\n"+join(names.get(win));}
    private String join(List<String>x){if(x==null||x.isEmpty())return "No winner";StringBuilder b=new StringBuilder();for(String s:x){if(b.length()>0)b.append(", ");b.append(s==null||s.isEmpty()?"Player":s);}return b.toString();}
}

