package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.Timestamp;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Original free, live multiplayer 5x5 Bingo: not the old 1-9 number-pick.
 * Host calls unique balls 1..75; 2-12 live room members join with frozen cards.
 * Both phones subscribe to the same room; bingo claims are verified by
 * Firestore against that immutable card and all actually called balls.
 * NO purchases, money, diamonds, VIP, paid rewards or client-side credit.
 */
public final class KingLiveBingo9980Activity extends Activity {
    private static final int BACKGROUND=0xff1a1532, PANEL=0xff332653;
    private final SecureRandom random=new SecureRandom();
    private FirebaseFirestore db;
    private FirebaseUser me;
    private String roomId="",roomName="Party Room",uid="";
    private DocumentReference roomRef,gameRef,myCardRef,myClaimRef;
    private ListenerRegistration gameListener,cardsListener,claimsListener;
    private boolean host=false, busy=false, hasClaim=false;
    private long roundId=0;
    private String status="none",winner="";
    private List<Integer> called=new ArrayList<>(Arrays.asList(0));
    private List<Integer> myCard=new ArrayList<>();
    private final boolean[] marked=new boolean[25];
    private List<String> participants=new ArrayList<>();
    private int readyCount=0;
    private TextView headline,roundLabel,callsLabel,playersLabel,winnerLabel,helpLabel;
    private GridLayout boardView;
    private Button newGame,joinGame,startGame,callBall,claim,closeGame;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String value,int size,int color) {
        TextView t=new TextView(this);t.setText(value);t.setTextColor(color);
        t.setTextSize(size);t.setPadding(dp(8),dp(8),dp(8),dp(8));
        return t;
    }
    private Button action(String value){
        Button b=new Button(this);b.setText(value);b.setTextSize(14);
        b.setAllCaps(false);return b;
    }
    private void toast(String msg){Toast.makeText(this,msg,Toast.LENGTH_LONG).show();}
    private boolean signed(){return me!=null && !uid.isEmpty() && !roomId.isEmpty();}
    private Map<String,Object> doc(Object...values){
        Map<String,Object> data=new HashMap<>();
        for(int i=0;i+1<values.length;i+=2)data.put((String)values[i],values[i+1]);
        return data;
    }
    private List<Integer> asInts(Object v){
        List<Integer> out=new ArrayList<>();
        if(v instanceof List<?>)for(Object o:(List<?>)v)
            if(o instanceof Number)out.add(((Number)o).intValue());
        return out;
    }
    private List<String> asStrings(Object v){
        List<String> out=new ArrayList<>();
        if(v instanceof List<?>)for(Object o:(List<?>)v)if(o instanceof String)out.add((String)o);
        return out;
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        roomId=getIntent().getStringExtra("roomId");
        String from=getIntent().getStringExtra("roomName");
        if(from!=null&&!from.isEmpty())roomName=from;
        if(roomId==null)roomId="";
        me=FirebaseAuth.getInstance().getCurrentUser();
        if(me!=null)uid=me.getUid();
        db=FirebaseFirestore.getInstance();
        setTitle("KING Plus • Live Bingo");
        createLayout();
        if(!signed()){
            headline.setText("Sign in, then enter a live Party Room to play Bingo.");
            return;
        }
        roomRef=db.collection("live_rooms").document(roomId);
        gameRef=roomRef.collection("live_bingo9980").document("current");
        myCardRef=gameRef.collection("cards").document(uid);
        myClaimRef=gameRef.collection("claims").document(uid);
        roomRef.get(Source.SERVER).addOnSuccessListener(s->{
            if(!s.exists()){headline.setText("This Party Room no longer exists");return;}
            host=uid.equals(s.getString("ownerUid"));
            render();
            watch();
        }).addOnFailureListener(e->toast("Room verification failed: "+e.getMessage()));
    }
    private void createLayout(){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setBackgroundColor(BACKGROUND);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(16),dp(12),dp(40));
        scroll.addView(root);
        headline=label("🎱 KING Plus Live Bingo • "+roomName,21,0xffffda7a);root.addView(headline);
        helpLabel=label("Real 5×5 BINGO: Join with your own B/I/N/G/O card. Host calls 1–75 balls. Mark called numbers and claim a complete row, column or diagonal. FREE play.",13,Color.WHITE);
        root.addView(helpLabel);
        roundLabel=label("Waiting for real Party game…",15,Color.WHITE);root.addView(roundLabel);
        callsLabel=label("Called: none",14,0xffefd0fe);root.addView(callsLabel);
        playersLabel=label("Ready: 0",14,0xffe7defb);root.addView(playersLabel);
        winnerLabel=label("",17,0xffffd86a);root.addView(winnerLabel);
        TextView columns=label("     B           I           N          G           O",16,0xffffd86a);
        root.addView(columns);
        boardView=new GridLayout(this);boardView.setColumnCount(5);
        boardView.setBackgroundColor(PANEL);boardView.setPadding(dp(4),dp(4),dp(4),dp(4));
        root.addView(boardView,new LinearLayout.LayoutParams(-1,-2));
        newGame=action("Host: New Bingo match");
        joinGame=action("Join with my 5×5 card");
        startGame=action("Host: Start with Ready players");
        callBall=action("Host: Call next random ball");
        claim=action("🏆 Claim BINGO!");
        closeGame=action("Host: End this match");
        for(Button btn:new Button[]{newGame,joinGame,startGame,callBall,claim,closeGame})
            root.addView(btn,new LinearLayout.LayoutParams(-1,dp(52)));
        newGame.setOnClickListener(v->newMatch());
        joinGame.setOnClickListener(v->join());
        startGame.setOnClickListener(v->startMatch());
        callBall.setOnClickListener(v->draw());
        claim.setOnClickListener(v->claimWin());
        closeGame.setOnClickListener(v->closeMatch());
        render();
        setContentView(scroll);
    }
    private void watch(){
        gameListener=gameRef.addSnapshotListener((snapshot,error)->{
            if(error!=null){toast("Bingo state: "+error.getMessage());return;}
            if(snapshot==null || !snapshot.exists()){
                roundId=0;status="none";called=new ArrayList<>(Arrays.asList(0));
                participants.clear();myCard.clear();readyCount=0;winner="";hasClaim=false;
                render();return;
            }
            Long id=snapshot.getLong("roundId");
            long next=id==null?0:id;
            if(next!=roundId){
                roundId=next;myCard.clear();
                Arrays.fill(marked,false);hasClaim=false;winner="";
                loadMyCard();
            }
            status=snapshot.getString("status");
            if(status==null)status="none";
            called=asInts(snapshot.get("called"));
            participants=asStrings(snapshot.get("participants"));
            if(called.isEmpty())called=new ArrayList<>(Arrays.asList(0));
            render();
        });
        cardsListener=gameRef.collection("cards").addSnapshotListener((snap,error)->{
            if(snap==null||error!=null)return;
            int count=0;
            for(DocumentSnapshot item:snap.getDocuments()){
                Long rid=item.getLong("roundId");
                if(rid!=null&&rid==roundId)count++;
            }
            readyCount=count;render();
        });
        claimsListener=gameRef.collection("claims").addSnapshotListener((snap,error)->{
            if(snap==null||error!=null)return;
            String first=""; Timestamp firstAt=null; boolean already=false;
            for(DocumentSnapshot item:snap.getDocuments()){
                Long rid=item.getLong("roundId");
                if(rid==null||rid!=roundId)continue;
                if(uid.equals(item.getId()))already=true;
                Timestamp when=item.getTimestamp("createdAt");
                if(when!=null&&(firstAt==null||when.compareTo(firstAt)<0)){
                    firstAt=when;first=item.getString("uid");
                }
            }
            hasClaim=already;winner=first==null?"":first;render();
        });
    }
    private void loadMyCard(){
        if(roundId<=0)return;
        final long requested=roundId;
        myCardRef.get(Source.SERVER).addOnSuccessListener(s->{
            if(roundId!=requested)return;
            Long rid=s.getLong("roundId");
            List<Integer> found=asInts(s.get("numbers"));
            if(rid!=null&&rid==roundId&&KingBingoRules9980.validCard(found)){
                myCard=found;render();
            }
        }).addOnFailureListener(e->toast("Bingo card read failed: "+e.getMessage()));
    }
    private void render(){
        if(roundLabel==null)return;
        roundLabel.setText("Room: "+roomName+" • "+status.toUpperCase()+" • Round "+roundId);
        List<Integer> actual=new ArrayList<>(called);
        actual.remove(Integer.valueOf(0));
        callsLabel.setText("🔔 Called "+actual.size()+"/75: "+actual.toString()+
                (actual.isEmpty()?"":"  • Last: "+actual.get(actual.size()-1)));
        playersLabel.setText("Ready cards: "+readyCount+" • Playing: "+participants.size()+
                (participants.contains(uid)?"  • You are playing":"  • You are watching"));
        winnerLabel.setText(winner.isEmpty()?"":("🏆 Bingo verified: "+(winner.equals(uid)?"YOU":winner)));
        boardView.removeAllViews();
        boolean cardReady=KingBingoRules9980.validCard(myCard);
        for(int i=0;i<25;i++){
            final int cell=i;
            int number=cardReady?myCard.get(i):0;
            boolean available=number==0||called.contains(number);
            boolean lit=cardReady&&(number==0||marked[i]);
            Button tile=action(!cardReady?"·":number==0?"★":String.valueOf(number));
            tile.setTextSize(15);
            tile.setTextColor(lit?Color.BLACK:Color.WHITE);
            tile.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                lit?0xffffda7a:(available?0xff7455c1:PANEL)));
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
            lp.width=0;lp.height=dp(50);
            lp.columnSpec=GridLayout.spec(i%5,1f);
            lp.rowSpec=GridLayout.spec(i/5);
            lp.setMargins(dp(2),dp(2),dp(2),dp(2));
            boardView.addView(tile,lp);
            tile.setEnabled(cardReady&&number!=0&&available);
            tile.setOnClickListener(v->{marked[cell]=!marked[cell];render();});
        }
        boolean waiting="waiting".equals(status),active="active".equals(status);
        newGame.setVisibility(host?View.VISIBLE:View.GONE);
        newGame.setEnabled(host && !busy && !active);
        joinGame.setVisibility(waiting&&!cardReady?View.VISIBLE:View.GONE);
        joinGame.setEnabled(!busy);
        startGame.setVisibility(host&&waiting?View.VISIBLE:View.GONE);
        startGame.setEnabled(host&&!busy&&readyCount>=2&&readyCount<=12);
        callBall.setVisibility(host&&active?View.VISIBLE:View.GONE);
        callBall.setEnabled(host&&!busy&&winner.isEmpty()&&KingBingoRules9980.validCalls(called)
                            &&called.size()<=75);
        claim.setVisibility(active&&cardReady&&participants.contains(uid)?View.VISIBLE:View.GONE);
        claim.setEnabled(!busy&&!hasClaim&&winner.isEmpty()&&
                         KingBingoRules9980.isWinningLine(myCard,called));
        closeGame.setVisibility(host&&active?View.VISIBLE:View.GONE);
        closeGame.setEnabled(!busy);
    }
    private void newMatch(){
        if(!host||busy)return;
        busy=true;render();
        final long next=System.currentTimeMillis();
        db.runTransaction(tx->{
            DocumentSnapshot previous=tx.get(gameRef);
            if(previous.exists()&&"active".equals(previous.getString("status")))
                throw new IllegalStateException("End active Bingo first");
            Long last=previous.getLong("roundId");
            long id=last==null?next:Math.max(next,last+1);
            tx.set(gameRef,doc("roundId",id,"status","waiting",
                "called",new ArrayList<>(Arrays.asList(0)),
                "participants",new ArrayList<String>(),"updatedAt",FieldValue.serverTimestamp()));
            return id;
        }).addOnSuccessListener(v->{busy=false;render();toast("New Bingo ready: join with your card");})
          .addOnFailureListener(e->{busy=false;render();toast("New Bingo failed: "+e.getMessage());});
    }
    private void join(){
        if(busy||roundId<=0||!"waiting".equals(status)||!signed())return;
        busy=true;render();
        final long id=roundId;
        final List<Integer> board=KingBingoRules9980.newCard();
        db.runTransaction(tx->{
            DocumentSnapshot g=tx.get(gameRef);
            DocumentSnapshot mine=tx.get(myCardRef);
            if(!g.exists()||!"waiting".equals(g.getString("status"))||
               g.getLong("roundId")==null||g.getLong("roundId")!=id)
                throw new IllegalStateException("Bingo already started");
            if(mine.exists()){
                Long prev=mine.getLong("roundId");
                if(prev!=null&&prev==id)throw new IllegalStateException("Already joined this Bingo");
            }
            tx.set(myCardRef,doc("uid",uid,"roundId",id,"numbers",board,
                                "createdAt",FieldValue.serverTimestamp()));
            return null;
        }).addOnSuccessListener(v->{busy=false;myCard=board;render();})
          .addOnFailureListener(e->{busy=false;render();toast("Join failed: "+e.getMessage());});
    }
    private void startMatch(){
        if(!host||busy||!"waiting".equals(status))return;
        busy=true;render();
        final long expected=roundId;
        gameRef.collection("cards").get(Source.SERVER).addOnSuccessListener(snapshot->{
            List<String> ready=new ArrayList<>();
            for(DocumentSnapshot item:snapshot.getDocuments()){
                Long rid=item.getLong("roundId");
                if(rid!=null&&rid==expected
                      &&KingBingoRules9980.validCard(asInts(item.get("numbers")))
                      &&item.getString("uid")!=null
                      &&item.getId().equals(item.getString("uid")))
                    ready.add(item.getId());
            }
            Collections.sort(ready);
            if(ready.size()<2||ready.size()>12){busy=false;render();toast("Need 2–12 players who joined with real cards");return;}
            db.runTransaction(tx->{
                DocumentSnapshot game=tx.get(gameRef);
                if(!game.exists()||!"waiting".equals(game.getString("status"))
                  ||game.getLong("roundId")==null||game.getLong("roundId")!=expected)
                    throw new IllegalStateException("Bingo round changed");
                for(String id:ready){
                    DocumentSnapshot member=tx.get(roomRef.collection("members").document(id));
                    DocumentSnapshot card=tx.get(gameRef.collection("cards").document(id));
                    if(!member.exists()||!id.equals(member.getString("uid"))||
                       !card.exists()||!id.equals(card.getString("uid"))||
                       card.getLong("roundId")==null||card.getLong("roundId")!=expected)
                        throw new IllegalStateException("Ready player left the Party");
                }
                tx.update(gameRef,doc("status","active","participants",ready,
                             "updatedAt",FieldValue.serverTimestamp()));
                return null;
            }).addOnSuccessListener(v->{busy=false;render();toast("Live Bingo started for "+ready.size()+" players");})
              .addOnFailureListener(e->{busy=false;render();toast("Start failed: "+e.getMessage());});
        }).addOnFailureListener(e->{busy=false;render();toast("Cannot load Ready players: "+e.getMessage());});
    }
    private void draw(){
        if(!host||busy||!"active".equals(status)||!winner.isEmpty())return;
        busy=true;render();
        final long expected=roundId;
        db.runTransaction(tx->{
            DocumentSnapshot g=tx.get(gameRef);
            if(!g.exists()||!"active".equals(g.getString("status"))||
               g.getLong("roundId")==null||g.getLong("roundId")!=expected)
                throw new IllegalStateException("Bingo round changed");
            List<Integer> now=asInts(g.get("called"));
            List<Integer> left=KingBingoRules9980.remaining(now);
            if(left.isEmpty())throw new IllegalStateException("All 75 Bingo balls called");
            int chosen=left.get(random.nextInt(left.size()));
            now.add(chosen);
            tx.update(gameRef,doc("called",now,"updatedAt",FieldValue.serverTimestamp()));
            return chosen;
        }).addOnSuccessListener(value->{busy=false;render();toast("Called: "+value);})
          .addOnFailureListener(e->{busy=false;render();toast("Ball call failed: "+e.getMessage());});
    }
    private void claimWin(){
        if(busy||!participants.contains(uid)||hasClaim||
           !KingBingoRules9980.isWinningLine(myCard,called))return;
        busy=true;render();
        final long expected=roundId;
        db.runTransaction(tx->{
            DocumentSnapshot g=tx.get(gameRef);
            DocumentSnapshot card=tx.get(myCardRef);
            DocumentSnapshot old=tx.get(myClaimRef);
            if(!g.exists()||!"active".equals(g.getString("status"))||
               g.getLong("roundId")==null||g.getLong("roundId")!=expected||
               !asStrings(g.get("participants")).contains(uid)||
               !card.exists()||card.getLong("roundId")==null||
               card.getLong("roundId")!=expected||
               !KingBingoRules9980.isWinningLine(asInts(card.get("numbers")),asInts(g.get("called"))))
                throw new IllegalStateException("No complete Bingo line on the server");
            if(old.exists()&&old.getLong("roundId")!=null&&old.getLong("roundId")==expected)
                throw new IllegalStateException("Already claimed this round");
            tx.set(myClaimRef,doc("uid",uid,"roundId",expected,
                                  "createdAt",FieldValue.serverTimestamp()));
            return null;
        }).addOnSuccessListener(v->{busy=false;render();toast("🏆 Bingo claimed and checked on server");})
          .addOnFailureListener(e->{busy=false;render();toast("Bingo claim rejected: "+e.getMessage());});
    }
    private void closeMatch(){
        if(!host||busy||!"active".equals(status))return;
        busy=true;render();
        gameRef.update(doc("status","closed","updatedAt",FieldValue.serverTimestamp()))
            .addOnSuccessListener(v->{busy=false;render();})
            .addOnFailureListener(e->{busy=false;render();toast("End match failed: "+e.getMessage());});
    }
    @Override protected void onDestroy(){
        if(gameListener!=null)gameListener.remove();
        if(cardsListener!=null)cardsListener.remove();
        if(claimsListener!=null)claimsListener.remove();
        super.onDestroy();
    }
}
